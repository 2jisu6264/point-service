package com.musinsa.sys.point.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.musinsa.sys.point.domain.PointLogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PointSavingCancelReq  {

	@NotNull
	private Long memberId;

	@NotNull
	private PointLogType logType;

	@NotNull
	private Long amount;

	@NotNull
	private Long walletId;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime logAt;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime originalLogAt;
}
