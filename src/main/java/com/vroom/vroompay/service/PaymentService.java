package com.vroom.vroompay.service;


import com.vroom.vroompay.vo.WalletTransactionVO;
import com.vroom.vroompay.vo.WalletVO;
import com.vroom.vroompay.mapper.WalletMapper;
import com.vroom.vroompay.mapper.WalletTxMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private WalletMapper walletMapper; // 결제 상태 machine gun...ㅎ

    @Autowired
    private WalletTxMapper txMapper; // 지갑 결과값

    @Transactional // 장부 기록과 잔액 변경 묶을게유
    public WalletVO charge (WalletTransactionVO reqVO){

        // 트랜잭션 활성화 여부 체크
        log.info("==== 트랜잭션 활성 상태 보기 : {}", TransactionSynchronizationManager.isActualTransactionActive());


        // 사용자(지갑) 체크 (WalletMapper)
        if (walletMapper.checkWalletExist(reqVO.getUserId()) == 0) {
            throw new RuntimeException("존재하지 않는 사용자입니다. ID: " + reqVO.getUserId());
        }

        // 비관적 락 걸기!! 이 줄 실행되면 다른거 요청 대기
        WalletVO lockedWallet = walletMapper.getWalletForUpdate(reqVO.getUserId());
        log.info("==== 사용자 충전 전 잔액: {} ====", lockedWallet.getBalance());

        // 잔액 업데이트
        WalletVO walletUpdate = new WalletVO();
        walletUpdate.setUserId(reqVO.getUserId());
        walletUpdate.setBalance(reqVO.getAmount()); // 충전할 금액

        walletMapper.chargeBalance(walletUpdate);

        // 장부 기록을 위한 최종 금액 계산
        BigDecimal afterBalance = lockedWallet.getBalance().add(reqVO.getAmount());

        // 장부 기록
        reqVO.setTxnType("CHARGE");
        reqVO.setMemo("충전");
        reqVO.setBalanceSnapshot(afterBalance);

        txMapper.insertTransaction(reqVO);

        // 최종 결과 리턴
        return  walletMapper.getWallet(reqVO.getUserId());
    }
}
