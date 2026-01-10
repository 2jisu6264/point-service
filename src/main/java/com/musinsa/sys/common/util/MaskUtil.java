package com.musinsa.sys.common.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MaskUtil {
    /**
     * 입력된 문자를 마스킹처리한다.
     * 하이픈 정책
     * 한글 회원명    본인 정보는 마스킹 없이 정보 표시 타인 정보는 가운데 자리 마스킹
     * 영문 회원명    본인 정보는 마스킹 없이 정보 표시 타인 정보는 마스킹
     * 생년월일       상시 표시
     * 휴대폰번호     본인 휴대폰 - 상시 표시, 타인 휴대폰 - 뒤 4자리 빼고 마스킹
     * 계좌번호       은행 로고가 있는 경우 은행 로고+계좌번호 표시 은행 로고가 없는 경우 은행명+계좌번호 표시 계좌번호는 뒤 4자리 제외하고 마스킹
     * 카드번호       6~12자리 마스킹
     * 선불카드번호   마스킹 없이 정보 표시 OTC
     * 가상계좌번호   마스킹 없이 정보 표시
     * 이메일 주소    계정이 5자리 이하인 경우 앞 1자리 제외하고 마스킹 계정이 5자리 초과인 경우 앞 3자리 제외하고 마스킹
     * 날짜           월, 일이 1자리인 경우 0을 포함하여 표기 안내 등 text 안에 표기하는 경우
     * 시간           분, 초가 1자인 경우 0을 포함하여 표기 24시간으로 표기 안내 등 text 안에 표기하는 경우 24시간으로 표기
     * 날짜와 시간    리스트 내 날짜 시간 표기
     * 머니           천 단위 ~,~ 표기
     * 포인트         천 단위 ~,~ 표기
     * 비밀번호       입출력 마스킹 처리
     * 백분율         소수점이 없는 경우 앞자리만 노출 소수점이 있는 경우 첫째자리까지 표시
     *
     * @param tskDscd : 0(핸드폰번호), 1(계좌번호), 2(카드번호), 3(이름), 4(이메일)
     * @param tgtInfo : 대상문자
     * @return
     */
    public static String getMaskedInfo(String tskDscd, String tgtInfo) {

        String masking = "*";
        String maskingTgt = "";
        String rsltTgtInfo = "";

        if (tskDscd.equals("0")) { /* 핸드폰번호 */
            if (tgtInfo.length() != 11) {
                log.info("핸드폰번호 길이오류");
                return tgtInfo;
            }

            if (!tgtInfo.matches("[+-]?\\d*(\\.\\d+)?")) {
                log.info("핸드폰번호 값 오류(문자열포함)");
                return tgtInfo;
            }

            for (int i = 0; i < 4; i++) {
                maskingTgt += masking;
            }

            rsltTgtInfo = tgtInfo.substring(0, 3) + maskingTgt + tgtInfo.substring(tgtInfo.length() - 4);

        } else if (tskDscd.equals("1")) { /* 계좌번호 */

            if (!tgtInfo.matches("[+-]?\\d*(\\.\\d+)?")) {
                log.info("계좌번호 값 오류(문자열포함)");
                return tgtInfo;
            }

            for (int i = 0; i < tgtInfo.length() - 4; i++) {
                maskingTgt += masking;
            }

            tgtInfo = maskingTgt + tgtInfo.substring(tgtInfo.length() - 4);
        } else if (tskDscd.equals("2")) { /* 카드번호 */

            if (!tgtInfo.matches("[+-]?\\d*(\\.\\d+)?")) {
                log.info("계좌번호 값 오류(문자열포함)");
                return tgtInfo;
            }

            for (int i = 0; i < 7; i++) {
                maskingTgt += masking;
            }

            tgtInfo = tgtInfo.substring(0, 6) + maskingTgt + tgtInfo.substring(tgtInfo.length() - 4, tgtInfo.length());
        } else if (tskDscd.equals("3")) { /* 이름 */
            if (tgtInfo != null && !"".equals(tgtInfo)) {
                log.info("tgtInfo : [{}],[{}]", tgtInfo.length(), tgtInfo);
                // 이름 가운데 글자 마스킹
                String middleMask = "";
                // 이름이 외자 또는 4자 이상인 경우 분기
                if (tgtInfo.length() > 2) {
                    middleMask = tgtInfo.substring(1, tgtInfo.length() - 1);
                } else {
                    middleMask = tgtInfo.substring(1);
                }
                // 가운데 글자 마스킹 하기위한 증감값
                for (int i = 0; i < middleMask.length(); i++) {
                    maskingTgt += masking;
                }

                rsltTgtInfo = tgtInfo.substring(0, 1) + maskingTgt;
                if (tgtInfo.length() > 2) {
                    log.info("last : [{}]", tgtInfo.substring(tgtInfo.length() - 1));
                    rsltTgtInfo += tgtInfo.substring(tgtInfo.length() - 1);
                }
            }
        } else if (tskDscd.equals("4")) { /* 이메일 */
            if (tgtInfo.length() >= 3) {
                rsltTgtInfo = tgtInfo.replaceAll("(?<=.{3}).(?=.*@)", "*");
            } else {
                rsltTgtInfo = tgtInfo;
            }
        }
        return rsltTgtInfo;
    }
}
