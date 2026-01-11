package com.musinsa.sys.point.entity;

import com.musinsa.sys.common.constants.Val;
import com.musinsa.sys.common.util.DateUtil;
import com.musinsa.sys.point.dto.PointSavingApprovalReq;
import com.musinsa.sys.point.enums.WalletSourceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "point_wallet")
public class PointWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "issued_amount", nullable = false)
    private Long issuedAmount;

    @Column(name = "used_amount")
    private Long usedAmount;

    @Column(name = "expired_amount")
    private Long expiredAmount;

    @Column(name = "wallet_status", length = 2, nullable = false)
    private String walletStatus;

    @Column(name = "expire_date", length = 8, nullable = false)
    private LocalDate expireDate;

    @Convert(converter = WalletSourceTypeConverter.class)
    @Column(name = "source_type", nullable = false)
    private WalletSourceType sourceType;

    @Column(name = "created_at", length = 8, nullable = false)
    private LocalDateTime createdAt;

    public static PointWallet from(Long memberId, WalletSourceType sourceType, Long amount, LocalDate expireDt) {
        return PointWallet.builder()
                .memberId(memberId)
                .walletStatus(Val.NORMAL)
                .sourceType(sourceType)
                .issuedAmount(amount)
                .usedAmount(0L)
                .expiredAmount(0L)
                .expireDate(expireDt)
                .createdAt(DateUtil.getLocalDateTimeWithNano())
                .build();
    }

}
