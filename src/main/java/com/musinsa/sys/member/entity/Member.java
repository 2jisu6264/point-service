package com.musinsa.sys.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Data
@NoArgsConstructor
@DynamicUpdate
@AllArgsConstructor
@Builder
@Entity(name="member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_name", length = 50)
    private String memberName;

    @Column(name = "point_balance")
    private Long pointBalance;

    @Column(name = "created_at", nullable = false, length = 8)
    private String createdDate;


}
