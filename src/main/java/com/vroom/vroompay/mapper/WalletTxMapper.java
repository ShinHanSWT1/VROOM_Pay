package com.vroom.vroompay.mapper;

import com.vroom.vroompay.vo.WalletTransactionVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletTxMapper {

    // 거래 기록
    void insertTransaction(WalletTransactionVO vo);
}
