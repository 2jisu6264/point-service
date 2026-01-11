package com.musinsa.sys.point.service;

import com.musinsa.sys.common.constants.Val;
import com.musinsa.sys.common.enums.ProcessCode;
import com.musinsa.sys.common.exception.ServiceException;
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
import com.musinsa.sys.point.enums.WalletSourceType;
import com.musinsa.sys.point.repository.PointLogRepository;
import com.musinsa.sys.point.repository.PointPolicyRepository;
import com.musinsa.sys.point.repository.PointUseDetailRepository;
import com.musinsa.sys.point.repository.PointWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public PointResp approveSaving(PointSavingApprovalReq pointSavingApprovalReq) {
        Long memberId = pointSavingApprovalReq.getMemberId();
        Long amount = pointSavingApprovalReq.getAmount();

        //거래구분코드 확인

        //회원 여부 확인
        Member member = getMember(memberId);
        //한도체크 ( 1회 충전금액, 총보유금액, 만료일)
        validateSavingAmount(amount);
        validateBalanceLimit(member.getPointBalance(), amount);
        validateExpireDate(pointSavingApprovalReq.getExpireDate());


        //pointLog 생성 후 save
        pointLogRepository.save(PointLog.from(memberId, amount, PointLogType.SAVING_APPROVAL.getCode(), pointSavingApprovalReq.getLogAt()));

        //member 포인트 추가 후 save
        member.addPointBalance(amount);
        memberRepository.save(member);

        // 포인트 지갑 생성 후 save
        PointWallet pointWallet = PointWallet.from(memberId, pointSavingApprovalReq.getSourceType(), pointSavingApprovalReq.getAmount(), pointSavingApprovalReq.getExpireDate());
        pointWalletRepository.save(pointWallet);

        return new PointResp(memberId, amount);
    }

    @Transactional
    public PointResp cancelSaving(PointSavingCancelReq pointSavingCancelReq) {
        Long memberId = pointSavingCancelReq.getMemberId();
        Long amount = pointSavingCancelReq.getAmount();
        Long walletId = pointSavingCancelReq.getWalletId();

        PointLog pointLog = new PointLog();
        //거래구분코드
        pointLog.setLogType(PointLogType.SAVING_CANCEL.getCode());

        //회원 여부 확인
        Member member = getMember(memberId);
        validatePointBalance(member, amount);

        //pointLog 생성 후 save
        pointLogRepository.save(PointLog.from(memberId, amount, PointLogType.SAVING_CANCEL.getCode(), pointSavingCancelReq.getLogAt()));

        member.subsPointBalance(amount);
        memberRepository.save(member);

        //취소할 거래 조회 (세분화)
        PointWallet cancelWallet = getCancelWallet(memberId, walletId);
        cancelWallet.setWalletStatus(Val.CANCEL);
        pointWalletRepository.save(cancelWallet);

        return new PointResp(memberId, amount);
    }

    @Transactional
    public PointUseApprovalResp useApproval(PointUseApprovalReq pointUseApprovalReq) {
        Long memberId = pointUseApprovalReq.getMemberId();
        Long amount = pointUseApprovalReq.getAmount();

        //회원 여부 확인
        Member member = getMember(memberId);
        validatePointBalance(member, amount);

        PointLog pointLog = PointLog.from(memberId, amount, PointLogType.USE_APPROVAL.getCode(), pointUseApprovalReq.getLogAt());
        //주문번호 체크
        String orderNo = orderNoGenerator.generateOrderNo();
        pointLog.setOrderNo(orderNo);
        //사용처리
        usePoint(pointLog);
        pointLogRepository.save(pointLog);

        member.subsPointBalance(amount);
        memberRepository.save(member);

        return new PointUseApprovalResp(memberId, orderNo, member.getPointBalance());
    }

    @Transactional
    public PointResp processRefund(PointUseCancelReq pointUseCancelReq) {
        Long memberId = pointUseCancelReq.getMemberId();
        String orderNo = pointUseCancelReq.getOrderNo();
        Long cancelAmount = pointUseCancelReq.getAmount();

        // 회원 락
        Member member = getMember(memberId);

        // 해당 주문의 사용 로그 조회
        PointLog useLogs = pointLogRepository.findUseLogsByOrderNoForUpdate(orderNo, PointLogType.USE_APPROVAL.getCode());
        if (useLogs == null)
            throw new ServiceException(ProcessCode.HCO006.getProcCd());

        processRefund(useLogs, cancelAmount);

        // 취소 로그 기록
        PointLog cancelLog = PointLog.from(memberId, orderNo, cancelAmount, PointLogType.USE_CANCEL.getCode(), pointUseCancelReq.getLogAt());
        pointLogRepository.save(cancelLog);

        // 회원 잔액 복원
        member.addPointBalance(cancelAmount);
        memberRepository.save(member);

        return new PointResp(memberId, member.getPointBalance());
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
            throw new ServiceException(ProcessCode.HCO006.getProcCd());
        } else if (cancelWallet.getUsedAmount() > 0) {
            throw new ServiceException(ProcessCode.HCO008.getProcCd());
        } else if (!cancelWallet.getWalletStatus().equals(Val.NORMAL)) {
            throw new ServiceException(ProcessCode.HCO009.getProcCd());
        }
        return cancelWallet;
    }

    private void validatePointBalance(Member member, Long amount) {
        if (member.getPointBalance() < amount) {
            throw new ServiceException(ProcessCode.HCO010.getProcCd());
        }
    }

    private void validateSavingAmount(long amount) {

        //null값 체크하기
        PointPolicy minPolicy = pointPolicyRepository.findByPolicyKey(PointPolicyKey.POINT_SAVING_MIN.name());
        PointPolicy maxPolicy = pointPolicyRepository.findByPolicyKey(PointPolicyKey.POINT_SAVING_MAX.name());

        long min = minPolicy.getPolicyValue();
        long max = maxPolicy.getPolicyValue();

        if (amount < min || amount > max) {
            throw new ServiceException(ProcessCode.HCO003.getProcCd()); // 적립금액 범위 초과
        }
    }

    private void validateBalanceLimit(long currentBalance, long earnAmount) {
        PointPolicy maxBalancePolicy = pointPolicyRepository.findByPolicyKey(PointPolicyKey.POINT_BALANCE_MAX.name());
        long maxBalance = maxBalancePolicy.getPolicyValue();

        if (currentBalance + earnAmount > maxBalance) {
            throw new ServiceException(ProcessCode.HCO003.getProcCd()); // 보유한도 초과
        }
    }

    private LocalDate validateExpireDate(LocalDate expireDate) {
        LocalDate today = LocalDate.now();

        if (expireDate.isBefore(today.plusDays(1))) {
            throw new ServiceException(ProcessCode.HCO004.getProcCd());
        }
        if (!expireDate.isBefore(today.plusYears(5))) {
            throw new ServiceException(ProcessCode.HCO005.getProcCd());
        }
        return expireDate;
    }

    public void usePoint(PointLog pointLog) {

        Long remainUseAmount = pointLog.getAmount(); // 남은 사용 금액

        List<PointWallet> usablePointList =
                pointWalletRepository.findUsableWallets(pointLog.getMemberId());

        for (PointWallet pointWallet : usablePointList) {

            if (remainUseAmount <= 0) break;

            Long issuedAmount = pointWallet.getIssuedAmount();
            Long usedAmount = pointWallet.getUsedAmount();
            Long expiredAmount = pointWallet.getExpiredAmount();

            // 실제 사용 가능한 금액
            Long usableAmount = issuedAmount - usedAmount - expiredAmount;

            if (usableAmount <= 0) continue;

            // 이번 wallet에서 사용할 금액
            Long useTarget = Math.min(usableAmount, remainUseAmount);

            pointWallet.setUsedAmount(pointWallet.getUsedAmount() + useTarget);
            pointWalletRepository.save(pointWallet);

            remainUseAmount -= useTarget;
        }

        if (remainUseAmount > 0) {
            throw new ServiceException(ProcessCode.HCO013.getProcCd());
        }

        pointUseDetailRepository.save(PointUseDetail.from(pointLog));
    }

    // 환불 프로세스 진행
    public void processRefund(PointLog useLogs, Long cancelAmount) {

        long remainCancelAmount = cancelAmount;
        long memberId = useLogs.getMemberId();

        // 주문에 사용된 wallet들 (사용 순서 역순 추천)
        List<PointWallet> cancelTargetList = pointWalletRepository.findCancelWallets(memberId);

        for (PointWallet pointWallet : cancelTargetList) {

            if (remainCancelAmount <= 0) break;

            long usedAmount = pointWallet.getUsedAmount();      // 이 wallet에서 사용된 금액
            long expiredAmount = pointWallet.getExpiredAmount();// 이 wallet에서 만료된 금액

            if (usedAmount <= 0) continue;

            // 이번 wallet에서 실제로 취소할 금액
            long cancelTarget = Math.min(usedAmount, remainCancelAmount);

            // 만료된 금액 중 취소 대상
            long reSaveAmount = Math.min(expiredAmount, cancelTarget);

            // 만료 안 된 사용 금액
            long restoreAmount = cancelTarget - reSaveAmount;

            // 만료된 금액 → 신규 적립
            if (reSaveAmount > 0) {
                PointWallet newWallet = PointWallet.from(memberId, WalletSourceType.RESAVING, reSaveAmount, LocalDate.now().plusYears(1));
                pointWalletRepository.save(newWallet);
            }

            // 만료 안 된 금액 → 기존 wallet 복원
            if (restoreAmount > 0) {
                pointWallet.setUsedAmount(pointWallet.getUsedAmount() - restoreAmount);
                pointWalletRepository.save(pointWallet);
            }

            remainCancelAmount -= cancelTarget;
        }

        if (remainCancelAmount > 0) {
            throw new ServiceException(ProcessCode.HCO014.getProcCd());
        }
    }
}
