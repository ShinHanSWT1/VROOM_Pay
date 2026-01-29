package com.vroom.vroompay.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;


@Data
public class WalletVO {
    private long userId;         // 사용자 고유 번호

    @JsonProperty("nickname")
    private String username;     // 사용자 이름

    private BigDecimal balance;       // 총 잔액
    private BigDecimal availBalance;  // 사용 가능 잔액

    private String realAccount;  // 실제 연결된 계좌 번호
    private Date joinedAt;       // 가입 일시
    private Date updatedAt;      // 정보 수정 일시
}
