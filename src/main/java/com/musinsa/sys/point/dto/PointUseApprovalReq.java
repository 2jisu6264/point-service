package com.musinsa.sys.point.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class PointUseApprovalReq  {

	@NotNull(message = "이용사고유값이 비어있습니다.")
	private Long uscoSno;
	
	@NotNull(message = "가맹점고유값이 비어있습니다.")
	private Long mrstSno;
	
	@NotNull(message = "회원고유값이 비어있습니다.")
	private Long mbrSno;  		// 회원 고유번호
	
	private String tmnNo; 		// 단말기 번호

	@NotBlank(message = "거래구분코드값이 비어있습니다.")
	private String trDscd;

	@NotBlank(message = "거래상세구분코드값이 비어있습니다.")
	private String trDtlDscd;

	private String stlmWyDcmtNo;// 결제수단식별번호
	
	@NotNull(message = "요청금액값이 비어있습니다.")
	private Long rqsAmt;		// 요청금액
	
	@NotNull(message = "처리금액값이 비어있습니다.")
	private Long pcsAmt;		// 처리금액

	private String trDt;		// 거래일자
	private String trTm;		// 거래시각
	private String trAprvNo;	// 거래승인번호

}
