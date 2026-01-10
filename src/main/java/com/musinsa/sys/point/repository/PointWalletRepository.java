package com.musinsa.sys.point.repository;

import com.musinsa.sys.point.entity.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {

}

