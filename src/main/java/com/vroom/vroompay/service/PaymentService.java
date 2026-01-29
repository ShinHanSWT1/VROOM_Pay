package com.vroom.vroompay.service;


import com.vroom.vroompay.type.TransactionType;
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
import java.util.Random;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private WalletMapper walletMapper; // 결제 상태 machine gun...ㅎ

    @Autowired
    private WalletTxMapper txMapper; // 지갑 결과값


    // 충전
    @Transactional // 장부 기록과 잔액 변경 묶을게유
    public WalletVO charge (WalletTransactionVO reqVO){
        validateAmount(reqVO.getAmount());

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
        reqVO.setTxnType(TransactionType.CHARGE.name());
        reqVO.setMemo("충전");
        reqVO.setBalanceSnapshot(afterBalance);

        txMapper.insertTransaction(reqVO);

        // 최종 결과 리턴
        return  walletMapper.getWallet(reqVO.getUserId());
    }


    // 출금
    @Transactional
    public WalletVO withdraw(WalletTransactionVO reqVO) {

        // - 방어
        validateAmount(reqVO.getAmount());

        // 사용자 체크
        if (walletMapper.checkWalletExist(reqVO.getUserId()) == 0) {
            throw new RuntimeException("존재하지 않는 사용자입니다. ID: " + reqVO.getUserId());
        }

        // 비관적 락 걸기 (동시성 제어)
        WalletVO lockedWallet = walletMapper.getWalletForUpdate(reqVO.getUserId());
        BigDecimal currentBalance = lockedWallet.getBalance();
        BigDecimal withdrawAmount = reqVO.getAmount();

        log.info("==== 출금 요청: {}원 / 현재 잔액: {}원 ====", withdrawAmount, currentBalance);


        // (가진 돈 < 출금할 돈) 이면 에러
        if (currentBalance.compareTo(withdrawAmount) < 0) {
            throw new RuntimeException("잔액이 부족합니다! (현재 잔액: " + currentBalance + "원)");
        }

        // 잔액 차감 (DB 반영)
        WalletVO walletUpdate = new WalletVO();
        walletUpdate.setUserId(reqVO.getUserId());
        walletUpdate.setBalance(withdrawAmount); // 뺄 금액

        walletMapper.deductBalance(walletUpdate); // 차감 쿼리 실행

        // 장부 기록용 스냅샷 계산
        BigDecimal afterBalance = currentBalance.subtract(withdrawAmount);

        // 장부 기록
        reqVO.setTxnType(TransactionType.WITHDRAW.name()); // ENUM 사용!
        reqVO.setMemo("출금"); // 나중엔 "친구 송금" 등으로 변경 가능
        reqVO.setBalanceSnapshot(afterBalance);

        txMapper.insertTransaction(reqVO);

        // 최종 결과 리턴
        return walletMapper.getWallet(reqVO.getUserId());
    }

    // 지갑 생성
    @Transactional
    public WalletVO createWallet(WalletVO reqVO){
        if (walletMapper.checkWalletExist(reqVO.getUserId()) > 0) {
            log.warn("이미 지갑이 존재하는 사용자입니다. ID: {}", reqVO.getUserId());
            return walletMapper.getWallet(reqVO.getUserId());
        }

        // 이 코드를 그 자리에 넣으세요!
        if (reqVO.getUsername() == null || reqVO.getUsername().isEmpty()) {
            // DB 에러 안 나게 임시 이름이라도 넣어주기
            reqVO.setUsername("VROOM_USER_" + reqVO.getUserId());
        }

        // 계좌번호 자동 생성
        reqVO.setRealAccount(generateAccountNumber());

//        지갑 생성
        walletMapper.createWallet(reqVO);
        log.info("새로운 지갑 생성 완료! ID: {}, 이름: {}", reqVO.getUserId(), reqVO.getUsername());

        // 생성된 지갑 정보 리턴
        return walletMapper.getWallet(reqVO.getUserId());
    }

    // 계좌 조회
    public WalletVO getWallet(Long userId) {
        if (walletMapper.checkWalletExist(userId) == 0) {
            throw new RuntimeException("존재하지 않는 사용자입니다. ID: " + userId);
        }
        return walletMapper.getWallet(userId);
    }






    // 랜덤 계좌번호 생성 (예: 110-2834-59271)
    private String generateAccountNumber() {
        Random random = new Random();
        int part1 = 100 + random.nextInt(900);          // 3자리
        int part2 = 1000 + random.nextInt(9000);         // 4자리
        int part3 = 10000 + random.nextInt(90000);       // 5자리
        return part1 + "-" + part2 + "-" + part3;
    }

    // 혹시 충전금액은 - 로 적을 수 있으니까 예외처리
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new RuntimeException("금액(amount)이 입력되지 않았습니다.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("금액은 0원보다 커야 합니다. (입력된 금액: " + amount + ")");
        }
    }
}
