package com.musinsa.sys.point.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.musinsa.sys.point.enums.PointLogType;
import com.musinsa.sys.point.domain.WalletSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class PointSavingApprovalReq  {

	@NotNull
	private Long memberId;

	@NotNull(message = "지급 유형값을 입력하세요.")
	private WalletSourceType sourceType;

	@NotNull(message = "거래금액을 입력하세요.")
	private Long amount;

	@NotNull
	@Pattern(
			regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(Z|[+-]\\d{2}:\\d{2})$"
	)
	private LocalDateTime logAt;

	@NotNull(message = "포인트만료일을 입력하세요.")
	@JsonFormat(pattern = "yyyyMMdd")
	private LocalDate  expireDate;
}
