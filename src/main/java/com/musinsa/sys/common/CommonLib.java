package com.musinsa.sys.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class CommonLib {
    private static final String mainkey = "hyphen221201hpbs";

    /**
     * 현재 일자 시간을 리턴한다.
     * @param format_str - yyyy MM dd HH mm ss SSS
     * @return Current Date time - strNow
     */
    public static String getCurDtim(String format_str) {
        long now = System.currentTimeMillis();
        SimpleDateFormat sdfNow = new SimpleDateFormat(format_str);
        String strNow = sdfNow.format(new Date(now));
        return strNow;
    }

    /**
     * StackTrace 를 String 으로 변환하는 메소드
     * @param e Exception
     * @return String
     */
    public static String getStackTraceToString(Exception e) {
        ByteArrayOutputStream ostr = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(ostr));
        return (ostr.toString());
    }

    /**
     * 파라미터의 값이 null일때 초기화, null이 아닌경우 trim
     * @param param 대상 문자열
     * @return String
     */
    public static String nullTrim(String param) {

        if( param == null ){
            param = "";
            return param;
        } else if(param.equals("null")){
            param = "";
            return param;
        }
        else{
            return param.trim();
        }
    }

    /**
     * 파라미터의 값이 null일때 초기화, null이 아닌경우 trim
     * @param param 대상 문자열
     * @return String
     */
    public static String nullTrim(Long param) {

        String retrunStr = "";
        if( param == null ){
            retrunStr = "";
            return retrunStr;
        }else{
            return Long.toString(param).trim();
        }
    }

    /**
     * Null 문자열 ("NULL,null") 이 들어올 경우 널 스트링을 리턴하는 함수
     * @param data 판별대상 문자열
     * @return 널스트링
     */
    public static String nullCheck(String data) {
        if (data == null || data.equals("null") || data.equals("NULL")) {
            data = "";
        }
        return data;
    }


    /**
     * Null 문자열 ("NULL,null") 이 들어올 경우 스페이스 한칸을 리턴하는 함수
     * @param data 판별대상 문자열
     * @return 널스트링
     */
    public static String nullToSpace(String data) {
        if (data == null ) {
            data = " ";
        }else if ( data.equals("null") || data.equals("NULL") || data.equals("") ) {
            data = " ";
        }
        return data;
    }


    /**
     * 입력된 문자를 지정된 길이의 전문형태로 변환한다. <br/>
     * 지정된 길이(len)보다 입력된 문자(str)이 큰 문자일 경우 지정된 길이 만큼만 리턴한다. <br/>
     * 한글을 substring할 경우 StringIndexOutOfBoundsException이 발생하며 이때 모든 문자열을 '?'로 처리한다.<br/>
     *  - type 정의 (입력된 문자(str)의 길이가 지정된 길이(len) 보다 작을 때 처리) <br/>
     *      SL : 남은 길이(len-str길이)만큼 앞쪽에 공백(" ")을 붙임 <br/>
     *      SR : 남은 길이(len-str길이)만큼 뒤쪽에 공백(" ")을 붙임 <br/>
     *      0L : 남은 길이(len-str길이)만큼 앞쪽에 공백("0")을 붙임 <br/>
     *      0R : 남은 길이(len-str길이)만큼 뒤쪽에 공백("0")을 붙임 <br/>
     * @param type : SL, SR, 0L, 0R
     * @param str : 대상문자
     * @param len : 지정길이
     * @return
     */
    public static String convFormat(String type, String str, int len) {

        StringBuffer sb = new StringBuffer();
        byte[] buff;
        int i, filler_len;

        if( str == null ) str = "";

        /* 길이가 overflow발생할 경우 문자열을(#)으로 초기화 */
        if( str.getBytes().length > len ) str = "#";

        buff = str.getBytes();
        filler_len = len - buff.length;

        if(filler_len == 0){
            sb.append(str);
        }else{
            if(filler_len > 0){
                if(type.equals("SR")){
                    sb.append(str);
                    for(i=0;i<filler_len;i++){ sb.append(" "); }
                }else if(type.equals("SL")){
                    for(i=0;i<filler_len;i++){ sb.append(" "); }
                    sb.append(str);
                }else if(type.equals("0R")){
                    sb.append(str);
                    for(i=0;i<filler_len;i++){ sb.append("0"); }
                }else if(type.equals("0L")){
                    for(i=0;i<filler_len;i++){ sb.append("0"); }
                    sb.append(str);
                }
            }else{
                try{
                    sb.append(str.substring(0, len));
                }catch(StringIndexOutOfBoundsException e){
                    for(i=0;i<len;i++){ sb.append("?"); }
                }
            }
        }

        return sb.toString();
    }
    
    /**
     * 입력된 문자를 마스킹처리한다.
     * 	하이픈 정책
     * 	한글 회원명    본인 정보는 마스킹 없이 정보 표시 타인 정보는 가운데 자리 마스킹
     영문 회원명    본인 정보는 마스킹 없이 정보 표시 타인 정보는 마스킹
     생년월일       상시 표시
     휴대폰번호     본인 휴대폰 - 상시 표시, 타인 휴대폰 - 뒤 4자리 빼고 마스킹
     계좌번호       은행 로고가 있는 경우 은행 로고+계좌번호 표시 은행 로고가 없는 경우 은행명+계좌번호 표시 계좌번호는 뒤 4자리 제외하고 마스킹
     카드번호       6~12자리 마스킹
     선불카드번호   마스킹 없이 정보 표시 OTC
     가상계좌번호   마스킹 없이 정보 표시
     이메일 주소    계정이 5자리 이하인 경우 앞 1자리 제외하고 마스킹 계정이 5자리 초과인 경우 앞 3자리 제외하고 마스킹
     날짜           월, 일이 1자리인 경우 0을 포함하여 표기 안내 등 text 안에 표기하는 경우
     시간           분, 초가 1자인 경우 0을 포함하여 표기 24시간으로 표기 안내 등 text 안에 표기하는 경우 24시간으로 표기
     날짜와 시간    리스트 내 날짜 시간 표기
     머니           천 단위 ~,~ 표기
     포인트         천 단위 ~,~ 표기
     비밀번호       입출력 마스킹 처리
     백분율         소수점이 없는 경우 앞자리만 노출 소수점이 있는 경우 첫째자리까지 표시 
     * @param tskDscd : 0(핸드폰번호), 1(계좌번호), 2(카드번호), 3(이름), 4(이메일)
     * @param tgtInfo : 대상문자
     * @return
     */
    public static String mskInfo( String tskDscd, String tgtInfo ) {

        String masking = "*";
        String maskingTgt = "";
        String rsltTgtInfo = "";

        if(tskDscd.equals("0")) { /* 핸드폰번호 */
            if(tgtInfo.length() != 11) {
                log.info("핸드폰번호 길이오류");
                return tgtInfo;
            }

            if(!tgtInfo.matches("[+-]?\\d*(\\.\\d+)?")) {
                log.info("핸드폰번호 값 오류(문자열포함)");
                return tgtInfo;
            }

            for(int i=0; i < 4; i++) {
                maskingTgt += masking;
            }

            rsltTgtInfo = tgtInfo.substring(0, 3) + maskingTgt +  tgtInfo.substring(tgtInfo.length()-4);

        } else if(tskDscd.equals("1")) { /* 계좌번호 */

            if(!tgtInfo.matches("[+-]?\\d*(\\.\\d+)?")) {
                log.info("계좌번호 값 오류(문자열포함)");
                return tgtInfo;
            }

            for(int i=0; i<tgtInfo.length()-4; i++) {
                maskingTgt += masking;
            }

            tgtInfo = maskingTgt +  tgtInfo.substring(tgtInfo.length()-4);
        } else if(tskDscd.equals("2")) { /* 카드번호 */

            if(!tgtInfo.matches("[+-]?\\d*(\\.\\d+)?")) {
                log.info("계좌번호 값 오류(문자열포함)");
                return tgtInfo;
            }

            for(int i=0; i < 7; i++) {
                maskingTgt += masking;
            }

            tgtInfo = tgtInfo.substring(0, 6) + maskingTgt +  tgtInfo.substring(tgtInfo.length()-4, tgtInfo.length());
        } else if(tskDscd.equals("3")) { /* 이름 */
            if(tgtInfo != null && !"".equals(tgtInfo)){
                log.info("tgtInfo : [{}],[{}]", tgtInfo.length(), tgtInfo);
                // 이름 가운데 글자 마스킹
                String middleMask = "";
                // 이름이 외자 또는 4자 이상인 경우 분기
                if(tgtInfo.length() > 2){
                    middleMask = tgtInfo.substring(1, tgtInfo.length()-1);
                } else {
                    middleMask = tgtInfo.substring(1);
                }
                // 가운데 글자 마스킹 하기위한 증감값
                for(int i = 0; i < middleMask.length(); i++){
                    maskingTgt += masking;
                }

                rsltTgtInfo = tgtInfo.substring(0,1) + maskingTgt;
                if(tgtInfo.length() > 2) {
                    log.info("last : [{}]", tgtInfo.substring(tgtInfo.length() - 1));
                    rsltTgtInfo += tgtInfo.substring(tgtInfo.length() - 1);
                }
            }
        } else if(tskDscd.equals("4")) { /* 이메일 */
            if (tgtInfo.length() >= 3) {
                rsltTgtInfo = tgtInfo.replaceAll("(?<=.{3}).(?=.*@)", "*");
            } else {
                rsltTgtInfo = tgtInfo;
            }
        }
        return rsltTgtInfo;
    }

}
