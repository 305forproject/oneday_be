package com.oneday.core.dto;

import lombok.Getter;

import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
public class PaymentRequestDto {

	@NotNull
	@Positive
	private int classId;

	@NotNull
	private Map<String, Object> tossResponse;
}