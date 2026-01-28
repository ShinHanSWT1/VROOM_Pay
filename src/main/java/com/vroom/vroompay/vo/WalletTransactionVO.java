package com.vroom.vroompay.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

// WALLET_TRANSACTIONS, 거래 장부 VO
@Data
public class WalletTransactionVO {
    // 식별
    private Long id; // 아이디


    // 거래 정보
    private String txnType; // 거래 유형 CHARGE(충전), HOLD(심부름 매칭 시 금액 보류), RELEASE(보류 해제), PAYOUT(정산), WITHDRAW(출금), REFUND(환불)
    private String memo; // 메모

    // 금액 관련 내용
    private BigDecimal amount; // 금액
    private BigDecimal balanceSnapshot; // 거래 후 잔액


    private Long userId; // 사용자 아이디
    private Long pgLogId; // 외부 결제 로그 ID
    private Date createdAt; // 생성일


}
