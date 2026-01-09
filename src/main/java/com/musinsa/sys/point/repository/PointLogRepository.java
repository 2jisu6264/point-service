package com.musinsa.sys.point.repository;

import com.musinsa.sys.point.domain.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointLogRepository extends JpaRepository<PointLog, Long> {

    // 회원별 거래 내역 조회
    PointLog findByMemberId(Long memberId);

    PointLog findByOrderNo(String orderNo);

}

