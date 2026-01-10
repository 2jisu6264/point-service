package com.musinsa.sys.point.repository;

import com.musinsa.sys.point.entity.PointPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointPolicyRepository extends JpaRepository<PointPolicy, String> {
    Optional<PointPolicy> findByPolicyKey(String policyKey);
}

