package com.vroom.vroompay.controller;

import com.vroom.vroompay.service.PaymentService;
import com.vroom.vroompay.vo.WalletTransactionVO;
import com.vroom.vroompay.vo.WalletVO;
import lombok.extern.slf4j.Slf4j; // 로깅 라이브러리 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j // 로그 기능 활성화
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/charge")
    public WalletVO charge(@RequestBody WalletTransactionVO requestVO) {

        // System.out.println 대신 log.info 사용 (속도도 빠르고 관리도 쉬움)
        log.info("[VroomPay] 충전 요청 도착! ID: {}, 금액: {}",
                requestVO.getUserId(), requestVO.getAmount());

        // pgLogId가 들어왔는지도 확인해보면 좋겠죠?
        log.info("연동된 PG 로그 ID: {}", requestVO.getPgLogId());

        return paymentService.charge(requestVO);
    }
}