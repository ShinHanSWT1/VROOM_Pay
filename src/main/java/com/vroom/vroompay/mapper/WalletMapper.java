package com.vroom.vroompay.mapper;

import com.vroom.vroompay.vo.WalletVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletMapper {

    // 1. 비관적 락 걸고 조회
    WalletVO getWalletForUpdate(Long userId);

    // 2. 잔액 업데이트 (충전/차감)
    void chargeBalance(WalletVO vo);

    // 3. 단순 잔액 조회
    WalletVO getWallet(Long userId);

    // 4. 회원(지갑) 존재 확인
    int checkWalletExist(Long userId);

    // 그래야 MEMBERS 테이블의 'username'도 같이 저장할 수 있습니다!
    void createWallet(WalletVO vo);
}