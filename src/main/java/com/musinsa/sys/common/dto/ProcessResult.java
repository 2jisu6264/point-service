package com.musinsa.sys.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class ProcessResult<O> {
    private String sucsFalr;    	// 성공실패여부
    private String rsltCd;			// 결과코드
    private String rsltMesg;		// 결과메시지
    private O rsltObj;				// 결과객체

    public ProcessResult(String sucsFalr, String rsltCd, String rsltMesg, O rsltObj) {
        super();
        this.sucsFalr = sucsFalr;
        this.rsltCd = rsltCd;
        this.rsltMesg = rsltMesg;
        this.rsltObj = rsltObj;
    }
}
