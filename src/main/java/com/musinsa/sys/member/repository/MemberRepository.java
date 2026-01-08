package com.musinsa.sys.member.repository;

import com.musinsa.sys.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    public Member findByMemberId(Long memberId);

}

