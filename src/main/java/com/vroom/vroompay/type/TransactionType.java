package com.vroom.vroompay.type;

public enum TransactionType {
    CHARGE,     // 충전
    WITHDRAW,   // 출금 (송금)
    PAYMENT,    // 결제
    REFUND,     // 환불
    HOLD,       // 홀드 (금액 잠금)
    RELEASE,    // 릴리즈 (홀드 해제)
    PAYOUT      // 지급 (수행자에게 송금)
}