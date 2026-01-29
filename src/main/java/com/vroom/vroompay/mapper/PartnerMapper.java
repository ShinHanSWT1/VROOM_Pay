package com.vroom.vroompay.mapper;


import com.vroom.vroompay.vo.PartnerVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartnerMapper {
    // api 키로 파트너 조회하기
    PartnerVO findByApiKey(String apiKey);
}
