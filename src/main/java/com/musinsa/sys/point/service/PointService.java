package com.musinsa.sys.point.service;

import com.musinsa.sys.common.enums.ProcessCode;
import com.musinsa.sys.order.component.OrderNoGenerator;
import com.musinsa.sys.member.entity.Member;
import com.musinsa.sys.member.repository.MemberRepository;
import com.musinsa.sys.point.entity.PointLog;
import com.musinsa.sys.point.enums.PointLogType;
import com.musinsa.sys.point.enums.PointPolicyKey;
import com.musinsa.sys.point.entity.PointWallet;
import com.musinsa.sys.point.dto.PointResp;
import com.musinsa.sys.point.dto.PointSavingApprovalReq;
import com.musinsa.sys.point.repository.PointPolicyRepository;
import com.musinsa.sys.point.repository.PointWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointPolicyService policyService;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointWalletRepository pointWalletRepository;
    private final OrderNoGenerator orderNoGenerator;
    private final MemberRepository memberRepository;
    private static final DateTimeFormatter EXPIRE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    @Transactional
    public PointResp savingApproval(PointSavingApprovalReq pointSavingApprovalReq) throws Exception {

        PointResp pointResp = new PointResp();
        PointWallet pointWallet = new PointWallet();
        PointLog pointLog = new PointLog();
        Member member;
        String orderNo;

        //거래구분코드 확인
        if (pointSavingApprovalReq.getLogType() != PointLogType.SAVING_APPROVAL){
            throw new ServiceException(ProcessCode.HCO001.getProcCd());
        }else{
            pointLog.setLogType(PointLogType.SAVING_APPROVAL);
        }

        //회원 여부 확인
        member = memberRepository.findByMemberId(pointSavingApprovalReq.getMemberId());
        if(member == null){
            throw new ServiceException(ProcessCode.HMB001.getProcCd());
        }else{
            pointLog.setMemberId(member.getMemberId());
        }

        //한도체크 ( 1회 충전금액, 총보유금액, 만료일)
        validateSavingAmount(pointSavingApprovalReq.getAmount());
        validateBalanceLimit(member.getPointBalance(), pointSavingApprovalReq.getAmount());
        validateExpireDate(pointSavingApprovalReq.getExpireDate());

        pointLog.setIssuedAmount(pointSavingApprovalReq.getAmount());

        //주문번호 체크
        orderNo = orderNoGenerator.generateOrderNo();
        pointLog.setOrderNo(orderNo);
        pointLog.setCreatedAt(LocalDateTime.now());

        Long totalBalance = member.getPointBalance() + pointSavingApprovalReq.getAmount();
        member.setPointBalance(totalBalance);
        pointWallet.setMemberId(pointSavingApprovalReq.getMemberId());
        pointWallet.setIssuedAmount(pointSavingApprovalReq.getAmount());
        pointWallet.setUsedAmount(0L);
        pointWallet.setExpiredAmount(0L);
        pointWallet.setExpireDate(pointSavingApprovalReq.getExpireDate());

        pointWalletRepository.save(pointWallet);

        return pointResp;
    }
/*
    @Transactional
    public PointResp savingCancel(PointSavingCancelReq pointSavingCancelReq) throws Exception {

        String curDt = CommonLib.getCurDtim("yyyyMMdd");
        String curTm = CommonLib.getCurDtim("HHmmss");

        long boxAmt = 0L;

        long walletLimit = 0L;

        HpptrLst hpptrLst = new HpptrLst();
        hpptrLst.setUscoSno(pointSavingCancelReq.getUscoSno());
        hpptrLst.setCnclDscd(pointSavingCancelReq.getCnclDscd());
        hpptrLst.setTrDscd(HpptrLstTrDscd.SAVINGCANCEL.getTrDscd());
        hpptrLst.setTrDtlDscd(HpptrLstTrDtlDscd.POINT.getTrDtlDscd());

        UsctrlMst usctrlMst = usctrlMstRepository.findByUscoSno(pointSavingCancelReq.getUscoSno());

        //거래일자
        if(CommonLib.nullTrim(hpptrLst.getTrDt()).equals("")) {
            hpptrLst.setTrDt(curDt);
        }

        //거래시간
        if(CommonLib.nullTrim(hpptrLst.getTrTm()).equals("")) {
            hpptrLst.setTrTm(curTm);
        }

        String date; //거래날짜+시간
        String num;  //승인번호

        //취소할 거래 조회
        HpptrLst hpptrLstOrg = new HpptrLst();

        if(CommonLib.nullTrim(pointSavingCancelReq.getOgtrAprvNo()).equals("")){
            hpptrLstOrg = hpptrLstRepository.findByTrDscdAndTrDtlDscdAndTrAprvNo(HpptrLstTrDscd.SAVINGAPPROVAL.getTrDscd(), HpptrLstTrDtlDscd.POINT.getTrDtlDscd(), pointSavingCancelReq.getTrAprvNo());
        }else{
            hpptrLstOrg = hpptrLstRepository.findByTrDscdAndTrDtlDscdAndTrAprvNo(HpptrLstTrDscd.SAVINGAPPROVAL.getTrDscd(), HpptrLstTrDtlDscd.POINT.getTrDtlDscd(), pointSavingCancelReq.getOgtrAprvNo());
        }

        if(hpptrLstOrg == null){
            throw new ServiceException("HPO024");
        }

        if(!hpptrLstOrg.getCnclDscd().equals(HpptrLstCnclDscd.NORMAL.getCnclDscd())){
            throw new ServiceException("HPO004");
        }

        if(CommonLib.nullTrim(pointSavingCancelReq.getMbtlNo()).equals("") && !CommonLib.nullTrim(pointSavingCancelReq.getMbrSno()).equals("")) { //백오피스에서 사용시 요청값이 다름
            date = pointSavingCancelReq.getTrDt()+pointSavingCancelReq.getTrTm();
            num = pointSavingCancelReq.getTrAprvNo();
        }else{
            date = pointSavingCancelReq.getOgtrDt()+pointSavingCancelReq.getOgtrTm();
            num = pointSavingCancelReq.getOgtrAprvNo();
        }

        if(!(hpptrLstOrg.getTrDt() + hpptrLstOrg.getTrTm()).equals(date)){
            throw new ServiceException("HPO025", "(일시불일치)");
        }

        if(!hpptrLstOrg.getTrAprvNo().equals(num)){
            throw new ServiceException("HPO025", "(승인번호불일치)");
        }

        if(!hpptrLstOrg.getRqsAmt().equals(pointSavingCancelReq.getRqsAmt())){
            throw new ServiceException("HPO025", "(요청금액 불일치)");
        }
        if(!hpptrLstOrg.getPcsAmt().equals(pointSavingCancelReq.getPcsAmt())){
            throw new ServiceException("HPO025", "(처리금액 불일치)");
        }

        MbrmngMst mbrmngMst = new MbrmngMst();

        if(CommonLib.nullTrim(pointSavingCancelReq.getMbtlNo()).equals("")) { //백오피스에서 회원 일련번호로 조회
            mbrmngMst = mbrmngMstRepository.findByUscoSnoAndMbrSno(pointSavingCancelReq.getUscoSno(), pointSavingCancelReq.getMbrSno());
        }else{
            mbrmngMst = mbrmngMstRepository.findByUscoSnoAndMbtlNoAndStcdNot(pointSavingCancelReq.getUscoSno(), pointSavingCancelReq.getMbtlNo(), MemberStcd.WITHDRAW.getStcd());
        }
        if(mbrmngMst == null){
            throw new ServiceException("HMB001");
        }
        if(!hpptrLstOrg.getMbrSno().equals(mbrmngMst.getMbrSno())){
            throw new ServiceException("HPO025", "(회원정보불일치)");
        }

        hpptrLst.setMbrSno(mbrmngMst.getMbrSno());
        MrstmMst mrstmMst = mrstmMstRepository.findById(pointSavingCancelReq.getMrstSno()).orElse(null);
        if(mrstmMst == null){
            throw new ServiceException("HMS001");
        }

        // 포인트 취소처리
        PosbxMst posbxMst = posbxMstRepository.findByTrSno(hpptrLstOrg.getTrSno());
        posbxMst.setStcd(PosbxMstStcd.CANCELED.getStcd()); //취소
        posbxMstRepository.save(posbxMst);

        // 보관함 취소 처리
        boxAmt = memberService.trCancel(hpptrLstOrg);
        hpptrLst.setMrstSno(hpptrLstOrg.getMrstSno());
        hpptrLst.setStlmWyDcmtNo(hpptrLstOrg.getStlmWyDcmtNo());

        //승인번호
        if(CommonLib.nullTrim(pointSavingCancelReq.getTrAprvNo()).equals("")) {
            BzgvnoLst bzgvnoLst = new BzgvnoLst();
            bzgvnoLst.setGvnoTskSno("00001");
            bzgvnoLst.setGvnoStdVl1(hpptrLst.getTrDt());
            bzgvnoLst.setGvnoStdVl2(BzgvnoMstTskDscd.POINT.getTskDscd());
            bzgvnoLst.setGvnoStdVl3(CommonLib.getSvrIdx());
            hpptrLst.setTrAprvNo(givingANumberService.crtnAprvNo(bzgvnoLst)); // 승인번호
        } else {
            hpptrLst.setTrAprvNo(pointSavingCancelReq.getTrAprvNo()); // 승인번호
        }
        hpptrLst.setTmnNo(hpptrLstOrg.getTmnNo());
        hpptrLst.setRqsAmt(hpptrLstOrg.getRqsAmt());
        hpptrLst.setPcsAmt(hpptrLstOrg.getPcsAmt());
        hpptrLst.setTrAfRmd((long)mbrmngMst.getPntAmt() - hpptrLstOrg.getPcsAmt()); // 잔액
        hpptrLst.setTrRpcd("0000"); // 거래응답코드
        hpptrLst.setTrRspdMsg("포인트 적립 취소 완료"); // 거래응답메시지

        hpptrLst.setOgtrSno(hpptrLstOrg.getTrSno());
        hpptrLst.setOgtrDt(hpptrLstOrg.getTrDt());
        hpptrLst.setOgtrTm(hpptrLstOrg.getTrTm());
        hpptrLst.setEnrDt(CommonLib.getCurDtim("yyyyMMdd"));
        hpptrLst.setEnrTm(CommonLib.getCurDtim("HHmmss"));
        //거래내역 업데이트
        hpptrLstRepository.save(hpptrLst);

        //취소거래내역 셋팅
        hpptrLstOrg.setCnctrSno(hpptrLst.getTrSno());
        hpptrLstOrg.setCnctrDt(hpptrLst.getTrDt());
        hpptrLstOrg.setCnctrTm(hpptrLst.getTrTm());
        hpptrLstOrg.setCnclDscd(hpptrLst.getCnclDscd());

        //취소거래내역 업데이트
        hpptrLstRepository.save(hpptrLstOrg);

        *//* 예치금 관련 *//*
        checkLmtAmtService.usAmt(hpptrLst.getUscoSno(), hpptrLst.getPcsAmt(), hpptrLst.getTrDscd());

        //회원정보 업데이트
        mbrmngMst.setPntAmt(mbrmngMst.getPntAmt() - (hpptrLstOrg.getPcsAmt() - boxAmt));
        mbrmngMstRepository.save(mbrmngMst);

        //기명, 무기명 체크 (충전한도)
        if( "Y".equals(mbrmngMst.getRgsdEnrYn())) {
            walletLimit = usctrlMst.getRgsdMaxHldgAmt();
        }else{
            walletLimit = usctrlMst.getNnmMaxHldgAmt();
        }

        // 회원 거래통계 업데이트
        memberService.updateMbrtrlMst(hpptrLst, hpptrLst.getPcsAmt());

        //지갑이 한도를 넘지 않고 보관함 잔액이 있는 경우 (보관함 -> 지갑)
        long boxTotal = mbrLstMapperRepository.boxSelect(mbrmngMst.getMbrSno());

        long walletTotal = mbrmngMst.getMonyAmt() + mbrmngMst.getPntAmt();

        if(boxTotal != 0 && walletTotal < walletLimit){
            memberService.boxToWallet(mbrmngMst, walletLimit);
        }

        PointResp pointSavingCancelResp = new PointResp();
        pointSavingCancelResp.setUscoSno(hpptrLst.getUscoSno());
        pointSavingCancelResp.setMrstSno(hpptrLst.getMrstSno());
        pointSavingCancelResp.setMrstNm(mrstmMst.getMrstNm());
        pointSavingCancelResp.setMbtlNo(pointSavingCancelReq.getMbtlNo());
        pointSavingCancelResp.setTrSno(hpptrLst.getTrSno());
        pointSavingCancelResp.setTrDt(hpptrLst.getTrDt());
        pointSavingCancelResp.setTrTm(hpptrLst.getTrTm());
        pointSavingCancelResp.setTrAprvNo(hpptrLst.getTrAprvNo());
        pointSavingCancelResp.setRqsAmt(hpptrLst.getRqsAmt());
        pointSavingCancelResp.setPcsAmt(hpptrLst.getPcsAmt());
        pointSavingCancelResp.setTrAfRmd(hpptrLst.getTrAfRmd());
        return pointSavingCancelResp;
    }

    @Transactional
    public PointExpireResp pointExpire(PointExpireReq pointExpireReq) throws Exception {
        UscmngMst uscmngMst = uscmngMstRepository.findById(pointExpireReq.getUscoSno()).orElseThrow(() -> new ServiceException("HUC003"));
        useCompanyService.isNormal(pointExpireReq.getUscoSno());
        MbrmngMst mbrmngMst = mbrmngMstRepository.findById(pointExpireReq.getMbrSno()).orElseThrow(() -> new ServiceException("HMB001"));
        memberService.memberStatusCheck(mbrmngMst);
        if(pointExpireReq.getPcsAmt() < 0) {
            throw new ServiceException("HPO022");
        }

        if(mbrmngMst.getPntAmt() < pointExpireReq.getPcsAmt()) {
            throw new ServiceException("HPO026");
        }

        String curDt = CommonLib.getCurDtim("yyyyMMdd");
        String curTm = CommonLib.getCurDtim("HHmmss");

        HpptrLst hpptrLst = new HpptrLst();
        hpptrLst.setUscoSno(pointExpireReq.getUscoSno());
        hpptrLst.setMbrSno(pointExpireReq.getMbrSno());
        hpptrLst.setMrstSno(uscmngMst.getRpstMrstNo());
        hpptrLst.setTrDscd(HpptrLstTrDscd.EXPIRE.getTrDscd());
        hpptrLst.setTrDtlDscd(HpptrLstTrDtlDscd.POINT.getTrDtlDscd());
        hpptrLst.setStlmWyDcmtNo("");

        if(CommonLib.nullTrim(pointExpireReq.getTrDt()).equals("")){
            hpptrLst.setTrDt(curDt);
            hpptrLst.setTrTm(curTm);
        } else {
            hpptrLst.setTrDt(pointExpireReq.getTrDt());
            hpptrLst.setTrTm(pointExpireReq.getTrTm());
        }

        hpptrLst.setRqsAmt(pointExpireReq.getRqsAmt());
        hpptrLst.setPcsAmt(pointExpireReq.getPcsAmt());

        // 보관함 사용처리
        try {
            memberService.useAmt(hpptrLst, pointExpireReq.getPcsAmt());
        } catch (Exception e) {
            log.info(CommonLib.getStackTraceToString(e));
            throw new ServiceException("HPO026");
        }

        if(CommonLib.nullTrim(pointExpireReq.getTrAprvNo()).equals("")) {
            //거래내역 셋팅
            BzgvnoLst bzgvnoLst = new BzgvnoLst();
            bzgvnoLst.setGvnoTskSno("00001");
            bzgvnoLst.setGvnoStdVl1(curDt);
            bzgvnoLst.setGvnoStdVl2(BzgvnoMstTskDscd.POINT.getTskDscd());
            bzgvnoLst.setGvnoStdVl3(CommonLib.getSvrIdx());
            hpptrLst.setTrAprvNo(givingANumberService.crtnAprvNo(bzgvnoLst)); // 승인번호
        } else {
            hpptrLst.setTrAprvNo(pointExpireReq.getTrAprvNo());
        }

        hpptrLst.setOrdNo(pointExpireReq.getOrdNo());
        hpptrLst.setTrAfRmd(mbrmngMst.getPntAmt() - hpptrLst.getPcsAmt()); // 잔액
        hpptrLst.setTrRpcd("0000"); // 거래응답코드
        hpptrLst.setTrRspdMsg("포인트 소멸 완료"); // 거래응답메시지
        hpptrLst.setEnrDt(CommonLib.getCurDtim("yyyyMMdd"));
        hpptrLst.setEnrTm(CommonLib.getCurDtim("HHmmss"));
        //거래내역 업데이트
        hpptrLstRepository.save(hpptrLst);

        //회원정보 업데이트
        mbrmngMst.setPntAmt(mbrmngMst.getPntAmt() - hpptrLst.getPcsAmt());
        mbrmngMstRepository.save(mbrmngMst);

        // 회원 거래통계 업데이트
        memberService.updateMbrtrlMst(hpptrLst, hpptrLst.getPcsAmt());

        return PointExpireResp.builder()
                .uscoSno(pointExpireReq.getUscoSno())
                .mbrSno(pointExpireReq.getMbrSno())
                .trDt(hpptrLst.getTrDt())
                .trTm(hpptrLst.getTrTm())
                .trAprvNo(hpptrLst.getTrAprvNo())
                .rqsAmt(hpptrLst.getRqsAmt())
                .pcsAmt(hpptrLst.getPcsAmt())
                .ordNo(pointExpireReq.getOrdNo())
                .build();
    }*/

    private void validateSavingAmount(long amount) {
        long min = policyService.getValue(PointPolicyKey.POINT_SAVING_MAX.name());
        long max = policyService.getValue("POINT_EARN_MAX");

        if (amount < min || amount > max) {
            throw new ServiceException("HCO003"); // 적립금액 범위 초과
        }
    }

    private void validateBalanceLimit(long currentBalance, long earnAmount) {
        long maxBalance = policyService.getValue("POINT_BALANCE_MAX");

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

}
