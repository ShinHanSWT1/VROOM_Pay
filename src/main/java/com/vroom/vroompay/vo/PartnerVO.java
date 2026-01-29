package com.vroom.vroompay.vo;

import lombok.Data;

@Data
public class PartnerVO {
    private Long id; // pk
    private String partnerName; // vroom 같은 회사
    private String clientId; // 클라이언트 ID
    private String apiKey; // API 키 생성 인증하려고
    private String isActive; // 활성화 여부
    private String createdAt; // 생성 일시
}