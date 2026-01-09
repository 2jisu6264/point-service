package com.musinsa.sys.point.repository;

import com.musinsa.sys.point.domain.PointLog;
import com.musinsa.sys.point.domain.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {
    PointWallet findByMemberIdAndWalletId(Long memberId, Long walletId);
}

