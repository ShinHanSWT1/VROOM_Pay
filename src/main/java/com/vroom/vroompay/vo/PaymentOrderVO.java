package com.vroom.vroompay.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 결제 주문 VO
 * PAYMENT_ORDERS 테이블과 매핑
 */
@Data
public class PaymentOrderVO {

    private Long id;                    // PK
    private String merchantUid;         // 주문 고유 ID (예: "ORDER_20260128_001")
    private BigDecimal amount;          // 결제 금액
    private String status;              // 상태: INIT, PENDING, COMPLETED, CANCELED
    private String createdAt;           // 생성일시
    private String paidAt;              // 결제 완료일시
    private Long errandsId;             // 심부름 ID
    private Long userId;                // 의뢰자 ID (돈 내는 사람)
    private Long erranderId;            // 수행자 ID (돈 받는 사람)
}