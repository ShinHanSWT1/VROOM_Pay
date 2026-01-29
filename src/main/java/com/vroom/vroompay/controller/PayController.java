package com.vroom.vroompay.controller;

import com.vroom.vroompay.service.PaymentOrderService;
import com.vroom.vroompay.service.PaymentService;
import com.vroom.vroompay.vo.PaymentOrderVO;
import com.vroom.vroompay.vo.WalletTransactionVO;
import com.vroom.vroompay.vo.WalletVO;
import lombok.extern.slf4j.Slf4j; // 로깅 라이브러리 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@Slf4j // 로그 기능 활성화
@RestController
@RequestMapping(value = "/pay")
public class PayController {

    @Autowired
    private PaymentOrderService paymentOrderService;

    @Autowired
    private PaymentService paymentService;


    // 충전
    @PostMapping("/charge")
    public WalletVO charge(@RequestBody WalletTransactionVO requestVO) {

        // System.out.println 대신 log.info 사용 (속도도 빠르고 관리도 쉬움)
        log.info("[VroomPay] 충전 요청 도착! ID: {}, 금액: {}",
                requestVO.getUserId(), requestVO.getAmount());

        // pgLogId가 들어왔는지도 확인해보면 좋겠죠?
        log.info("연동된 PG 로그 ID: {}", requestVO.getPgLogId());

        return paymentService.charge(requestVO);
    }

    // 출금
    @PostMapping(value = "/withdraw")
    public WalletVO withdraw(@RequestBody WalletTransactionVO requestVO) {

        log.info("[VroomPay] 출금 요청: ID={}, 금액={}", requestVO.getUserId(), requestVO.getAmount());

        return paymentService.withdraw(requestVO);
    }


    // 지갑 생성
    @PostMapping("/new")
    public WalletVO createWallet(@RequestBody WalletVO walletVO) {
        log.info("[VroomPay] 지갑 생성 요청: ID={}, 이름={}", walletVO.getUserId(), walletVO.getUsername());

        return paymentService.createWallet(walletVO);
    }

    // 계좌 조회
    @GetMapping("/wallet/{userId}")
    public WalletVO getWallet(@PathVariable long userId) {
        log.info("[VroomPay] 계좌 조회 요청: ID={}", userId);

        return paymentService.getWallet(userId);
    }

    // 결제 주문 생성 (INIT)
    @PostMapping("/order")
    public PaymentOrderVO createOrder(@RequestBody PaymentOrderVO orderVO) {
        log.info("[VroomPay] 결제 주문 생성 요청 - 의뢰자: {}, 금액: {}",
                orderVO.getUserId(), orderVO.getAmount());

        return paymentOrderService.createOrder(orderVO);
    }

    // 금액 홀드 (INIT → PENDING)
    @PostMapping("/order/hold/{orderId}")
    public PaymentOrderVO holdAmount(@PathVariable Long orderId) {
        log.info("[VroomPay] 금액 홀드 요청 - 주문ID: {}", orderId);

        return paymentOrderService.holdAmount(orderId);
    }

    // 결제 완료 (PENDING → COMPLETED)
    @PostMapping("/order/complete/{orderId}")
    public PaymentOrderVO completeOrder(@PathVariable Long orderId) {
        log.info("[VroomPay] 결제 완료 요청 - 주문ID: {}", orderId);

        return paymentOrderService.completeOrder(orderId);
    }

    // 결제 취소 (PENDING → CANCELED)
    @PostMapping("/order/cancel/{orderId}")
    public PaymentOrderVO cancelOrder(@PathVariable Long orderId) {
        log.info("[VroomPay] 결제 취소 요청 - 주문ID: {}", orderId);

        return paymentOrderService.cancelOrder(orderId);
    }
}