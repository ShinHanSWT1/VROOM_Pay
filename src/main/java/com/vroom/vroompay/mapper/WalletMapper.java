package com.vroom.vroompay.mapper;

import com.vroom.vroompay.vo.WalletVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletMapper {

    // 회원(지갑) 존재 확인
    int checkWalletExist(Long userId);

    // 단순 잔액 조회
    WalletVO getWallet(Long userId);

    // 지갑 생성(회원 가입)
    void createWallet(WalletVO vo);

    // 비관적 락 걸고 조회
    WalletVO getWalletForUpdate(Long userId);

    // 잔액 업데이트 (충전)
    void chargeBalance(WalletVO vo);

    // 잔액 업데이트 (차감)
    void deductBalance(WalletVO vo);

    // 홀드 - avail_balance 차감 (심부름 등록 시)
    void holdBalance(WalletVO vo);

    // 릴리즈 - avail_balance 복구 (심부름 취소 시)
    void releaseBalance(WalletVO vo);

    // avail_balance 증가 (수행자가 돈 받을 때)
    void chargeAvailBalance(WalletVO vo);

}