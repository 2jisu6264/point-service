package com.musinsa.sys.common.sequence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;

@Component
public class OrderNoGenerator {

    private final OrderSequenceRepository orderSeqRepository;

    public OrderNoGenerator(OrderSequenceRepository orderSeqRepository) {
        this.orderSeqRepository = orderSeqRepository;
    }

    /**
     * 주문번호 생성
     * yyyyMMddHHmmss + 4자리 시퀀스
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo() {

        LocalDateTime now = LocalDateTime.now();

        String orderDateTime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        OrderSequenceLog orderSequenceLog = orderSeqRepository.findForUpdate(orderDateTime)
                .orElseGet(() -> new OrderSequenceLog(orderDateTime));

        orderSequenceLog.increase();

        if (orderSequenceLog.getSeq() > 9999) {
            throw new IllegalStateException("초당 주문번호 한도(9999) 초과");
        }

        orderSeqRepository.save(orderSequenceLog);

        return orderDateTime + String.format("%04d", orderSequenceLog.getSeq());
    }

}
