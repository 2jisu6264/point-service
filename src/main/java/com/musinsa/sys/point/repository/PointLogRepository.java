package com.musinsa.sys.point.repository;

import com.musinsa.sys.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PointLogRepository extends JpaRepository<PointLog, Long> {

    // 회원별 거래 내역 조회
    PointLog findByMemberId(Long memberId);

    PointLog findByOrderNo(String orderNo);

}

