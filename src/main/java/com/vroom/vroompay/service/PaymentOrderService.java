package com.vroom.vroompay.service;

import com.vroom.vroompay.mapper.PaymentOrderMapper;
import com.vroom.vroompay.mapper.WalletMapper;
import com.vroom.vroompay.mapper.WalletTxMapper;
import com.vroom.vroompay.type.TransactionType;
import com.vroom.vroompay.vo.PaymentOrderVO;
import com.vroom.vroompay.vo.WalletTransactionVO;
import com.vroom.vroompay.vo.WalletVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class PaymentOrderService {

    @Autowired
    private PaymentOrderMapper orderMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private WalletTxMapper txMapper;

    /**
     * 1. 결제 주문 생성 (INIT)
     * - 심부름 등록할 때 호출
     */
    @Transactional
    public PaymentOrderVO createOrder(PaymentOrderVO reqVO) {
        // 주문 생성 (상태: INIT)
        orderMapper.insertOrder(reqVO);
        log.info("결제 주문 생성 완료 - ID: {}, 금액: {}", reqVO.getId(), reqVO.getAmount());

        return orderMapper.findById(reqVO.getId());
    }

    /**
     * 2. 금액 홀드 (INIT → PENDING)
     * - 의뢰자의 avail_balance에서 금액 차감
     */
    @Transactional
    public PaymentOrderVO holdAmount(Long orderId) {
        // 주문 조회
        PaymentOrderVO order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다. ID: " + orderId);
        }
        if (!"INIT".equals(order.getStatus())) {
            throw new RuntimeException("홀드할 수 없는 상태입니다. 현재 상태: " + order.getStatus());
        }

        // 의뢰자 지갑 조회 (비관적 락)
        WalletVO wallet = walletMapper.getWalletForUpdate(order.getUserId());
        BigDecimal availBalance = wallet.getAvailBalance();
        BigDecimal holdAmount = order.getAmount();

        // 사용 가능 잔액 체크
        if (availBalance.compareTo(holdAmount) < 0) {
            throw new RuntimeException("사용 가능 잔액이 부족합니다. (가용 잔액: " + availBalance + ")");
        }

        // avail_balance 차감
        WalletVO updateVO = new WalletVO();
        updateVO.setUserId(order.getUserId());
        updateVO.setAvailBalance(holdAmount);
        walletMapper.holdBalance(updateVO);

        // 장부 기록 (HOLD)
        WalletTransactionVO txVO = new WalletTransactionVO();
        txVO.setUserId(order.getUserId());
        txVO.setAmount(holdAmount);
        txVO.setTxnType(TransactionType.HOLD.name());
        txVO.setMemo("심부름 결제 홀드 - 주문ID: " + orderId);
        txVO.setBalanceSnapshot(availBalance.subtract(holdAmount));
        txMapper.insertTransaction(txVO);

        // 상태 업데이트 (INIT → PENDING)
        order.setStatus("PENDING");
        orderMapper.updateStatus(order);

        log.info("금액 홀드 완료 - 주문ID: {}, 금액: {}", orderId, holdAmount);
        return orderMapper.findById(orderId);
    }

    /**
     * 3. 결제 완료 (PENDING → COMPLETED)
     * - 의뢰자 balance 차감
     * - 수행자 balance에 금액 지급
     */
    @Transactional
    public PaymentOrderVO completeOrder(Long orderId) {
        // 주문 조회
        PaymentOrderVO order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다. ID: " + orderId);
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("완료할 수 없는 상태입니다. 현재 상태: " + order.getStatus());
        }

        BigDecimal amount = order.getAmount();

        // 의뢰자 balance 차감 (비관적 락)
        WalletVO userWallet = walletMapper.getWalletForUpdate(order.getUserId());
        WalletVO userUpdate = new WalletVO();
        userUpdate.setUserId(order.getUserId());
        userUpdate.setBalance(amount);
        walletMapper.deductBalance(userUpdate);

        // 의뢰자 장부 기록 (PAYOUT)
        WalletTransactionVO userTx = new WalletTransactionVO();
        userTx.setUserId(order.getUserId());
        userTx.setAmount(amount);
        userTx.setTxnType(TransactionType.PAYOUT.name());
        userTx.setMemo("심부름 결제 완료 - 주문ID: " + orderId);
        userTx.setBalanceSnapshot(userWallet.getBalance().subtract(amount));
        txMapper.insertTransaction(userTx);

        // 수행자 balance, avail_balance 증가 (비관적 락)
        WalletVO erranderWallet = walletMapper.getWalletForUpdate(order.getErranderId());
        WalletVO erranderUpdate = new WalletVO();
        erranderUpdate.setUserId(order.getErranderId());
        erranderUpdate.setBalance(amount);
        walletMapper.chargeBalance(erranderUpdate);

        // 수행자 장부 기록 (CHARGE - 수입)
        WalletTransactionVO erranderTx = new WalletTransactionVO();
        erranderTx.setUserId(order.getErranderId());
        erranderTx.setAmount(amount);
        erranderTx.setTxnType(TransactionType.CHARGE.name());
        erranderTx.setMemo("심부름 수행 완료 - 주문ID: " + orderId);
        erranderTx.setBalanceSnapshot(erranderWallet.getBalance().add(amount));
        txMapper.insertTransaction(erranderTx);

        // 상태 업데이트 (PENDING → COMPLETED)
        order.setStatus("COMPLETED");
        orderMapper.updateStatus(order);
        orderMapper.updatePaidAt(orderId);

        log.info("결제 완료 - 주문ID: {}, 의뢰자: {}, 수행자: {}, 금액: {}",
                orderId, order.getUserId(), order.getErranderId(), amount);
        return orderMapper.findById(orderId);
    }

    /**
     * 4. 결제 취소 (PENDING → CANCELED)
     * - 홀드된 금액을 avail_balance로 복구
     */
    @Transactional
    public PaymentOrderVO cancelOrder(Long orderId) {
        // 주문 조회
        PaymentOrderVO order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("존재하지 않는 주문입니다. ID: " + orderId);
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("취소할 수 없는 상태입니다. 현재 상태: " + order.getStatus());
        }

        BigDecimal amount = order.getAmount();

        // avail_balance 복구 (비관적 락)
        WalletVO wallet = walletMapper.getWalletForUpdate(order.getUserId());
        WalletVO updateVO = new WalletVO();
        updateVO.setUserId(order.getUserId());
        updateVO.setAvailBalance(amount);
        walletMapper.releaseBalance(updateVO);

        // 장부 기록 (RELEASE)
        WalletTransactionVO txVO = new WalletTransactionVO();
        txVO.setUserId(order.getUserId());
        txVO.setAmount(amount);
        txVO.setTxnType(TransactionType.RELEASE.name());
        txVO.setMemo("심부름 결제 취소 - 주문ID: " + orderId);
        txVO.setBalanceSnapshot(wallet.getAvailBalance().add(amount));
        txMapper.insertTransaction(txVO);

        // 상태 업데이트 (PENDING → CANCELED)
        order.setStatus("CANCELED");
        orderMapper.updateStatus(order);

        log.info("결제 취소 완료 - 주문ID: {}, 금액: {} 복구됨", orderId, amount);
        return orderMapper.findById(orderId);
    }
}