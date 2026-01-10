package com.musinsa.sys.common.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum ProcessCode {

    /* 공통 */
    HCO000("success", "HCO000", "정상으로 처리되었어요"),
    HCO001("fail", "HCO001", "올바르지 않은 거래구분코드입니다."),
    HCO002("fail", "HCO002", "올바르지 않은 거래상세구분코드입니다."),
    HCO003("fail", "HCO003", "1회 한도가 초가되었습니다."),
    HCO004("fail", "HCO004", "만료일은 최소 1일 이후여야 합니다."),
    HCO005("fail", "HCO005", "만료일은 5년 미만이어야 합니다."),
    HCO006("fail", "HCO006", "사용된 거래가 조회되지 않습니다."),
    HCO009("fail", "HCO009", "취소할 수 있는 상태가 아닙니다."),
    HCO999("fail", "HCO999", "시스템 에러가 발생했어요. 고객센터로 연락해주세요"),


    /* 회원관리(Member) */
    HMB001("fail", "HMB001", "회원 정보를 찾을 수 없습니다."),
    HMB002("fail", "HMB002", "비밀번호가 맞지 않아요");


    private String sucsFalr;
    private String procCd;
    private String rsltMesg;

    private ProcessCode(String sucsFalr, String procCd, String rsltMesg) {
        this.sucsFalr = sucsFalr;
        this.procCd = procCd;
        this.rsltMesg = rsltMesg;
    }

    public String getSucsFalr() {
        return sucsFalr;
    }

    public String getProcCd() {
        return procCd;
    }

    public String getRsltMesg() {
        return rsltMesg;
    }

    public static ProcessCode findByProcessCode(String inProcCd) {
        return Arrays.stream(ProcessCode.values())
                .filter(procCd -> procCd.getProcCd().equals(inProcCd))
                .findAny()
                .orElse(HCO999);
    }

}