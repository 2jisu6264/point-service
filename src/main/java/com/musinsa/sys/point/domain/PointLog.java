package com.musinsa.sys.point.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "point_log")
public class PointLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Convert(converter = PointLogTypeConverter.class)
    @Column(name = "log_type", length = 2, nullable = false)
    private PointLogType logType;

    @Column(name = "log_at", length = 8, nullable = false)
    private String logAt;

    @Column(name = "order_no", length = 20, nullable = false)
    private String orderNo;

    @Column(name = "issued_amount", nullable = false)
    private Long issuedAmount;

    @Column(name = "cancel_type", length = 1)
    private String cancelType;

    @Column(name = "original_log_id")
    private Long originalLogId;

    @Column(name = "original_log_at", length = 8)
    private String originalLogAt;

    @Column(name = "cancel_log_id")
    private Long cancelLogId;

    @Column(name = "cancel_log_at", length = 8)
    private String cancelLogAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
