package com.oneday.core.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.util.Map;

@Getter
public class PaymentRequestDto {

	@NotNull
	@Positive
	private int timeId;

	@NotNull
	private Map<String, Object> tossResponse;
}
