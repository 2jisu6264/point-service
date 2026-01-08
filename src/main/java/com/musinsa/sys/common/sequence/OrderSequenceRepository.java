package com.musinsa.sys.common.sequence;

import com.musinsa.sys.member.domain.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderSequenceRepository extends JpaRepository<OrderSequenceLog, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from order_sequence_log o where o.orderDate = :orderDate")
    Optional<OrderSequenceLog> findForUpdate(String orderDt);
}
