package com.musinsa.sys.point.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.musinsa.sys.point.enums.PointLogType;
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

	@NotBlank
	@Size(max = 2)
	private PointLogType logType;

	@NotBlank
	@Size(max = 20)
	private String storeNo;

	@NotNull
	private Long amount;

	@NotNull
	@Pattern(
			regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(Z|[+-]\\d{2}:\\d{2})$"
	)
	private LocalDateTime logAt;

	@JsonFormat(pattern = "yyyyMMdd")
	private LocalDate  expireDate;
}
