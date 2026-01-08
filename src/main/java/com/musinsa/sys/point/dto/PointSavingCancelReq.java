package com.musinsa.sys.point.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointSavingCancelReq  {

	@NotNull(message = "이용사고유값이 비어있습니다.")
	private Long uscoSno;

	@NotNull(message = "가맹점고유값이 비어있습니다.")
	private Long mrstSno;

	private String mbtlNo;

	private Long mbrSno;

	@NotNull(message = "요청금액값이 비어있습니다.")
	private Long rqsAmt;

	@NotNull(message = "처리금액값이 비어있습니다.")
	private Long pcsAmt;

	private String ogtrAprvNo;
	private String ogtrDt;
	private String ogtrTm;

	private String cnclDscd;    // 취소구분 코드

	private String trDt;
	private String trTm;
	private String trAprvNo;
}
