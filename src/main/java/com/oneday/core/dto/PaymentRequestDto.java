package com.oneday.core.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class PaymentRequestDto {

	private int classId;

	private Map<String, Object> tossResponse;
}