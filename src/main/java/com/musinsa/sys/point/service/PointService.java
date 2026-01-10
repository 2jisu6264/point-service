package com.musinsa.sys.point.service;

import com.musinsa.sys.common.enums.ProcessCode;
import com.musinsa.sys.member.entity.Member;
import com.musinsa.sys.member.repository.MemberRepository;
import com.musinsa.sys.order.component.OrderNoGenerator;
import com.musinsa.sys.point.dto.*;
import com.musinsa.sys.point.entity.PointLog;
import com.musinsa.sys.point.entity.PointPolicy;
import com.musinsa.sys.point.entity.PointUseDetail;
import com.musinsa.sys.point.entity.PointWallet;
import com.musinsa.sys.point.enums.PointLogType;
import com.musinsa.sys.point.enums.PointPolicyKey;
import com.musinsa.sys.point.repository.PointLogRepository;
import com.musinsa.sys.point.repository.PointPolicyRepository;
import com.musinsa.sys.point.repository.PointUseDetailRepository;
import com.musinsa.sys.point.repository.PointWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointLogRepository pointLogRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointWalletRepository pointWalletRepository;
    private final OrderNoGenerator orderNoGenerator;
    private final MemberRepository memberRepository;
    private final PointUseDetailRepository pointUseDetailRepository;

    @Transactional
    public PointResp savingApproval(PointSavingApprovalReq pointSavingApprovalReq) {

        Long memberId = pointSavingApprovalReq.getMemberId();
        Long amount = pointSavingApprovalReq.getAmount();

        //회원 여부 확인
        Member member = getMember(memberId);
        //한도체크 ( 1회 충전금액, 총보유금액, 만료일)
        validateSavingAmount(amount);
        validateBalanceLimit(member.getPointBalance(), amount);
        validateExpireDate(pointSavingApprovalReq.getExpireDate());

        //주문번호 체크
/*        orderNo = orderNoGenerator.generateOrderNo();
        pointLog.setOrderNo(orderNo);*/

        //pointLog 생성 후 save
        pointLogRepository.save(PointLog.from(memberId, amount, PointLogType.SAVING_APPROVAL, pointSavingApprovalReq.getLogAt()));

        //member 포인트 추가 후 save
        member.addPointBalance(amount);
        memberRepository.save(member);

        // 포인트 지갑 생성 후 save
        PointWallet pointWallet = PointWallet.from(memberId, pointSavingApprovalReq);
        pointWalletRepository.save(pointWallet);

        return new PointResp(memberId, pointWallet.getWalletId(), amount);
    }

    @Transactional
    public PointResp savingCancel(PointSavingCancelReq pointSavingCancelReq) {
        Long memberId = pointSavingCancelReq.getMemberId();
        Long amount = pointSavingCancelReq.getAmount();
        Long walletId = pointSavingCancelReq.getWalletId();
        //회원 여부 확인
        Member member = getMember(memberId);
        validatePointBalance(member, amount);

        //pointLog 생성 후 save
        pointLogRepository.save(PointLog.from(memberId, amount, PointLogType.SAVING_CANCEL, pointSavingCancelReq.getLogAt()));

        member.subsPointBalance(amount);
        memberRepository.save(member);

        //취소할 거래 조회 (세분화)
        PointWallet cancelWallet = getCancelWallet(memberId, walletId);
        cancelWallet.setWalletStatus("10");
        pointWalletRepository.save(cancelWallet);

        return new PointResp(memberId, walletId, amount);
    }

    @Transactional
    public PointResp useApproval(PointUseApprovalReq pointUseApprovalReq) {
        Long memberId = pointUseApprovalReq.getMemberId();
        Long amount = pointUseApprovalReq.getAmount();

        //회원 여부 확인
        Member member = getMember(memberId);
        validatePointBalance(member, amount);

        PointLog pointLog = PointLog.from(memberId, amount, PointLogType.USE_APPROVAL, pointUseApprovalReq.getLogAt());
        //주문번호 체크
        pointLog.setOrderNo(orderNoGenerator.generateOrderNo());
        //사용처리
        usePoint(pointLog);
        pointLogRepository.save(pointLog);

        member.subsPointBalance(amount);
        memberRepository.save(member);

        return new PointResp(memberId, member.getPointBalance());
    }

    @Transactional
    public PointResp useCancel(PointUseCancelReq pointUseCancelReq) {

        Long memberId = pointUseCancelReq.getMemberId();
        Long amount = pointUseCancelReq.getAmount();

        //회원 여부 확인
        Member member = getMember(memberId);
        validatePointBalance(member, amount);
        //취소할 거래 조회 (세분화)
   /*     PointWallet cancelWallet = pointWalletRepository.findByMemberIdAndWalletId(memberId, walletId);
        if (cancelWallet == null) {
            throw new ServiceException("HCO006");
        } else if (cancelWallet.getUsedAmount() > 0) {
            throw new ServiceException("HCO008");
        } else if (!cancelWallet.getWalletStatus().equals("00")) {
            throw new ServiceException("HCO009");
        }*/

        pointLogRepository.save(PointLog.from(memberId, amount, PointLogType.SAVING_CANCEL, pointUseCancelReq.getLogAt()));
        member.subsPointBalance(amount);
        memberRepository.save(member);
/*
        cancelWallet.setWalletStatus("10");
        pointWalletRepository.save(cancelWallet);
*/

        return new PointResp(memberId);
    }

    private Member getMember(Long memberId) {
        Member member = memberRepository.findByMemberIdForUpdate(memberId);
        if (member == null)
            throw new ServiceException(ProcessCode.HMB001.getProcCd());
        return member;
    }

    private PointWallet getCancelWallet(Long memberId, Long walletId) {
        PointWallet cancelWallet = pointWalletRepository.findByMemberIdAndWalletId(memberId, walletId);
        if (cancelWallet == null) {
            throw new ServiceException("HCO006");
        } else if (cancelWallet.getUsedAmount() > 0) {
            throw new ServiceException("HCO008");
        } else if (!cancelWallet.getWalletStatus().equals("00")) {
            throw new ServiceException("HCO009");
        }
        return cancelWallet;
    }

    private void validateSavingAmount(long amount) {

        //null값 체크하기
        PointPolicy minPolicy = pointPolicyRepository.findByPolicyKey(PointPolicyKey.POINT_SAVING_MIN.name());
        PointPolicy maxPolicy = pointPolicyRepository.findByPolicyKey(PointPolicyKey.POINT_SAVING_MAX.name());

        long min = minPolicy.getPolicyValue();
        long max = maxPolicy.getPolicyValue();

        if (amount < min || amount > max) {
            throw new ServiceException("HCO003"); // 적립금액 범위 초과
        }
    }

    private void validateBalanceLimit(long currentBalance, long earnAmount) {
        PointPolicy maxBalancePolicy = pointPolicyRepository.findByPolicyKey(PointPolicyKey.POINT_BALANCE_MAX.name());
        long maxBalance = maxBalancePolicy.getPolicyValue();

        if (currentBalance + earnAmount > maxBalance) {
            throw new ServiceException("HCO003"); // 보유한도 초과
        }
    }

    private void validateExpireDate(LocalDate expireDate) {
        LocalDate today = LocalDate.now();

        if (expireDate.isBefore(today.plusDays(1))) {
            throw new ServiceException("HCO004");
        }
        if (!expireDate.isBefore(today.plusYears(5))) {
            throw new ServiceException("HCO005");
        }
    }

    private void validatePointBalance(Member member, Long amount) {
        if (member.getPointBalance() < amount) {
            throw new ServiceException("HCO010");
        }
    }

    public void usePoint(PointLog pointLog) {

        Long useAmt = pointLog.getAmount(); // 사용금액

        List<PointWallet> usablePointList = pointWalletRepository.findUsableWallets(pointLog.getMemberId());

        // 사용금액 각가의 거래건에 업데이트 처리
        for (PointWallet pointWallet : usablePointList) {
            Long balancePoint = pointWallet.getIssuedAmount() - pointWallet.getUsedAmount();
            if (balancePoint >= useAmt) {
                pointWallet.setUsedAmount(pointWallet.getUsedAmount() + useAmt);
                useAmt = 0L;
            } else {
                pointWallet.setUsedAmount(pointWallet.getUsedAmount() + balancePoint);
                useAmt -= balancePoint;
            }

            pointWalletRepository.save(pointWallet);
            if (useAmt == 0) {
                break;
            }
        }
        pointUseDetailRepository.save(PointUseDetail.from(pointLog));
    }
}
