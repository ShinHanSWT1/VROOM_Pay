package com.vroom.vroompay.mapper;

import com.vroom.vroompay.vo.PaymentOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentOrderMapper {

    // 결제 주문 생성
    void insertOrder(PaymentOrderVO vo);

    // 주문 ID로 조회
    PaymentOrderVO findById(Long id);

    // merchantUid로 조회
    PaymentOrderVO findByMerchantUid(String merchantUid);

    // 상태 업데이트 (INIT -> PENDING -> COMPLETED or CANCELED)
    void updateStatus(PaymentOrderVO vo);

    // 결제 완료 시간 업데이트
    void updatePaidAt(Long id);

    void updateErrander(
            @Param("orderId") Long orderId,
            @Param("erranderId") Long erranderId);

}
