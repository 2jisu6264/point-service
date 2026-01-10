package com.musinsa.sys.point.service;

import com.musinsa.sys.common.sequence.OrderNoGenerator;
import com.musinsa.sys.member.domain.Member;
import com.musinsa.sys.member.repository.MemberRepository;
import com.musinsa.sys.point.domain.*;
import com.musinsa.sys.point.dto.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public PointResp savingApproval(PointSavingApprovalReq pointSavingApprovalReq) throws Exception {

        PointResp pointResp = new PointResp();
        PointWallet pointWallet = new PointWallet();

        PointLog pointLog = new PointLog();
        Member member;

        Long memberId = pointSavingApprovalReq.getMemberId();
        Long amount = pointSavingApprovalReq.getAmount();

        //거래구분코드 확인
        pointLog.setLogType(PointLogType.SAVING_APPROVAL);

        //회원 여부 확인
        member = memberRepository.findByMemberIdForUpdate(memberId);
        if (member == null) {
            throw new ServiceException(ProcessCode.HMB001.getProcCd());
        } else {
            pointLog.setMemberId(member.getMemberId());
        }

        //한도체크 ( 1회 충전금액, 총보유금액, 만료일)
        validateSavingAmount(amount);
        validateBalanceLimit(member.getPointBalance(),amount);
        validateExpireDate(pointSavingApprovalReq.getExpireDate());

        pointLog.setAmount(amount);
        pointLog.setLogAt(pointSavingApprovalReq.getLogAt());

        //주문번호 체크
/*        orderNo = orderNoGenerator.generateOrderNo();
        pointLog.setOrderNo(orderNo);*/
        pointLog.setCreatedAt(LocalDateTime.now().withNano(0));

        pointLogRepository.save(pointLog);

        Long totalBalance = member.getPointBalance() + amount;
        member.setPointBalance(totalBalance);

        memberRepository.save(member);
        pointWallet.setMemberId(memberId);
        pointWallet.setWalletStatus("00");
        pointWallet.setSourceType(pointSavingApprovalReq.getSourceType());
        pointWallet.setIssuedAmount(pointSavingApprovalReq.getAmount());
        pointWallet.setUsedAmount(0L);
        pointWallet.setExpiredAmount(0L);
        pointWallet.setExpireDate(pointSavingApprovalReq.getExpireDate());
        pointWallet.setCreatedAt(LocalDateTime.now().withNano(0));
        pointWalletRepository.save(pointWallet);

        pointResp.setMemberId(memberId);
        pointResp.setWalletId(pointWallet.getWalletId());
        pointResp.setAmount(amount);
        return pointResp;
    }

    @Transactional
    public PointResp savingCancel(PointSavingCancelReq pointSavingCancelReq) throws Exception {

        PointResp pointResp = new PointResp();
        PointLog pointLog = new PointLog();
        Member member;

        Long memberId = pointSavingCancelReq.getMemberId();
        Long amount = pointSavingCancelReq.getAmount();
        Long walletId = pointSavingCancelReq.getWalletId();

        //거래구분코드
        pointLog.setLogType(PointLogType.SAVING_CANCEL);

        //회원 여부 확인
        member = memberRepository.findByMemberIdForUpdate(memberId);
        if (member == null) {
            throw new ServiceException("HMB001");
        } else {
            pointLog.setMemberId(memberId);
        }

        //취소할 거래 조회 (세분화)
        PointWallet cancelWallet = pointWalletRepository.findByMemberIdAndWalletId(memberId, walletId);
        if (cancelWallet == null) {
            throw new ServiceException("HCO006");
        } else if (cancelWallet.getUsedAmount() > 0) {
            throw new ServiceException("HCO008");
        } else if (!cancelWallet.getWalletStatus().equals("00")) {
            throw new ServiceException("HCO009");
        }

        pointLog.setAmount(amount);
        pointLog.setLogAt(pointSavingCancelReq.getLogAt());
        pointLog.setCreatedAt(LocalDateTime.now().withNano(0));

        pointLogRepository.save(pointLog);

        if (member.getPointBalance() < amount) {
            throw new ServiceException("HCO010");
        }

        Long totalBalance = member.getPointBalance() - amount;
        member.setPointBalance(totalBalance);

        memberRepository.save(member);

        cancelWallet.setWalletStatus("10");
        pointWalletRepository.save(cancelWallet);

        pointResp.setMemberId(memberId);
        pointResp.setWalletId(walletId);
        pointResp.setAmount(walletId);
        return pointResp;
    }

    @Transactional
    public PointResp useApproval(PointUseApprovalReq pointUseApprovalReq) throws Exception {

        PointResp pointResp = new PointResp();
        PointLog pointLog = new PointLog();
        Member member;

        Long memberId = pointUseApprovalReq.getMemberId();
        Long amount = pointUseApprovalReq.getAmount();

        //거래구분코드
        pointLog.setLogType(PointLogType.USE_APPROVAL);

        if(CommonLib.nullTrim(pointExpireReq.getTrDt()).equals("")){
            hpptrLst.setTrDt(curDt);
            hpptrLst.setTrTm(curTm);
        } else {
            pointLog.setMemberId(memberId);
        }

        long pointBalance = member.getPointBalance();
        if(pointBalance < amount) {
            throw new ServiceException("HMB003");
        }

        //주문번호 체크
        String orderNo = orderNoGenerator.generateOrderNo();
        pointLog.setOrderNo(orderNo);

        //사용처리
        usePoint(pointLog);

        pointLog.setAmount(amount);
        pointLog.setLogAt(pointUseApprovalReq.getLogAt());
        pointLog.setCreatedAt(LocalDateTime.now().withNano(0));

        pointLogRepository.save(pointLog);

        if (member.getPointBalance() < amount) {
            throw new ServiceException("HCO010");
        }

        Long totalBalance = member.getPointBalance() - amount;
        member.setPointBalance(totalBalance);

        memberRepository.save(member);

        pointResp.setMemberId(memberId);
        pointResp.setAmount(totalBalance);
        return pointResp;
    }
    @Transactional
    public PointResp useCancel(PointUseCancelReq pointUseCancelReq) throws Exception {

        PointResp pointResp = new PointResp();
        PointLog pointLog = new PointLog();
        Member member;

        Long memberId = pointUseCancelReq.getMemberId();
        Long amount = pointUseCancelReq.getAmount();

        //거래구분코드
        pointLog.setLogType(PointLogType.SAVING_CANCEL);

        //회원 여부 확인
        member = memberRepository.findByMemberIdForUpdate(memberId);
        if (member == null) {
            throw new ServiceException("HMB001");
        } else {
            pointLog.setMemberId(memberId);
        }

        //취소할 거래 조회 (세분화)
   /*     PointWallet cancelWallet = pointWalletRepository.findByMemberIdAndWalletId(memberId, walletId);
        if (cancelWallet == null) {
            throw new ServiceException("HCO006");
        } else if (cancelWallet.getUsedAmount() > 0) {
            throw new ServiceException("HCO008");
        } else if (!cancelWallet.getWalletStatus().equals("00")) {
            throw new ServiceException("HCO009");
        }*/

        pointLog.setAmount(amount);
        pointLog.setLogAt(pointUseCancelReq.getLogAt());
        pointLog.setCreatedAt(LocalDateTime.now().withNano(0));

        pointLogRepository.save(pointLog);

        if (member.getPointBalance() < amount) {
            throw new ServiceException("HCO010");
        }

        Long totalBalance = member.getPointBalance() - amount;
        member.setPointBalance(totalBalance);

        memberRepository.save(member);

/*
        cancelWallet.setWalletStatus("10");
        pointWalletRepository.save(cancelWallet);
*/

        pointResp.setMemberId(memberId);
        return pointResp;
    }

    private void validateSavingAmount(long amount) {

        //null값체크하기
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

    private LocalDate validateExpireDate(LocalDate expireDate) {
        LocalDate today = LocalDate.now();

        if (expireDate.isBefore(today.plusDays(1))) {
            throw new ServiceException("HCO004");
        }
        if (!expireDate.isBefore(today.plusYears(5))) {
            throw new ServiceException("HCO005");
        }
        return expireDate;
    }
    public void usePoint(PointLog pointLog) throws Exception {

        Long useAmt = pointLog.getAmount(); // 사용금액

        List<PointWallet> usablePointList = new ArrayList<>();
        usablePointList = pointWalletRepository.findUsableWallets(pointLog.getMemberId());

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

        PointUseDetail pointUseDetail = new PointUseDetail();
        pointUseDetail.setOrderNo(pointLog.getOrderNo());
        pointUseDetail.setUsedAmount(pointLog.getAmount());
        pointUseDetail.setCreatedAt((LocalDateTime.now().withNano(0)));

        pointUseDetailRepository.save(pointUseDetail);
    }
}
