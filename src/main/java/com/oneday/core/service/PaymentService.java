package com.oneday.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.core.dto.payment.PaymentRequestDto;
import com.oneday.core.entity.Payment;
import com.oneday.core.entity.Reservation;
import com.oneday.core.repository.PaymentRepository;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final ReservationService reservationService;
	private final PaymentRepository paymentRepository;
	private final ObjectMapper objectMapper;

	@Value("${toss.secret-key}")
	private String widgetSecretKey;

	@Transactional
	public Payment processPayment(long studentId, PaymentRequestDto requestDto) throws Exception {

		// 1. 토스에 결제 승인 요청
		JsonNode tossData = confirmTossPayment(
				requestDto.getPaymentKey(),
				requestDto.getOrderId(),
				requestDto.getAmount()
		);

		// 2. 승인 성공 시, 예약 생성
		Reservation savedReservation = reservationService.createReservation(requestDto.getTimeId(), studentId);

		// 3. 결제 정보 매핑 및 DB 저장
		return savePayment(savedReservation, tossData);
	}

	// [내부 메서드 1] 토스 승인 요청 (ObjectMapper 사용으로 변경)
	private JsonNode confirmTossPayment(String paymentKey, String orderId, long amount) throws Exception {
		// 요청 본문 생성
		Map<String, Object> requestMap = new HashMap<>();
		requestMap.put("orderId", orderId);
		requestMap.put("amount", amount);
		requestMap.put("paymentKey", paymentKey);

		String jsonBody = objectMapper.writeValueAsString(requestMap);

		// 인증 헤더 생성
		Base64.Encoder encoder = Base64.getEncoder();
		byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
		String authorizations = "Basic " + new String(encodedBytes);

		// 연결 설정
		URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Authorization", authorizations);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);

		// 데이터 전송
		try (OutputStream outputStream = connection.getOutputStream()) {
			outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
		}

		// 응답 처리
		int code = connection.getResponseCode();
		boolean isSuccess = code == 200;

		InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

		// ★ 여기서 JSON 파싱을 Jackson(ObjectMapper)으로 처리
		JsonNode responseNode = objectMapper.readTree(responseStream);

		if (!isSuccess) {
			log.error("토스 결제 승인 실패: {}", responseNode.toString());
			String errorMessage = responseNode.has("message") ? responseNode.get("message").asText() : "Unknown Error";
			throw new RuntimeException("토스 결제 승인 실패: " + errorMessage);
		}

		return responseNode;
	}

	// [내부 메서드 2] DB 저장 (JsonNode 사용으로 변경)
	private Payment savePayment(Reservation reservation, JsonNode tossData) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

		Payment payment = Payment.builder()
				.reservation(reservation)
				.tossOrderId(tossData.get("orderId").asText())
				.tossPaymentKey(tossData.get("paymentKey").asText())
				.tossPaymentMethod(tossData.get("method").asText())
				.tossPaymentStatus(tossData.get("status").asText())
				.totalAmount(tossData.get("totalAmount").asInt())
				.requestedAt(OffsetDateTime.parse(tossData.get("requestedAt").asText(), formatter).toLocalDateTime())
				.approvedAt(OffsetDateTime.parse(tossData.get("approvedAt").asText(), formatter).toLocalDateTime())
				.build();

		return paymentRepository.save(payment);
	}
}
