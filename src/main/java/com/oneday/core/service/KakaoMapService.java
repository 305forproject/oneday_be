package com.oneday.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.core.dto.common.CoordinateDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Kakao Map REST API를 사용하여 주소를 좌표로 변환하는 서비스
 *
 * @author zionge2k
 * @since 2025-01-27
 */
@Slf4j
@Service
public class KakaoMapService {

	private final String kakaoApiKey;
	private static final String GEOCODING_URL = "https://dapi.kakao.com/v2/local/search/address.json";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public KakaoMapService(@Value("${kakao.api.key}") String kakaoApiKey) {
		this.kakaoApiKey = kakaoApiKey;
	}

	/**
	 * 주소를 좌표로 변환
	 *
	 * @param address 변환할 주소
	 * @return 좌표 정보, 실패 시 null
	 */
	public CoordinateDto getCoordinatesFromAddress(String address) {
		try {
			String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
			String urlString = GEOCODING_URL + "?query=" + encodedAddress;

			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "KakaoAK " + kakaoApiKey);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java Application)");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);

			int responseCode = conn.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
				);
				StringBuilder response = new StringBuilder();
				String line;

				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();

				CoordinateDto coordinates = parseCoordinatesFromResponse(response.toString());
				
				if (coordinates != null) {
					log.info("좌표 변환 성공: address={}, latitude={}, longitude={}", 
							address, coordinates.latitude(), coordinates.longitude());
				} else {
					log.warn("좌표 변환 실패: address={}, 결과 없음", address);
				}
				
				return coordinates;

			} else {
				log.warn("Kakao API 호출 실패: address={}, responseCode={}", address, responseCode);
				return null;
			}

		} catch (IOException e) {
			log.error("좌표 변환 중 오류 발생: address={}", address, e);
			return null;
		}
	}

	/**
	 * JSON 응답에서 좌표 정보 추출 (Jackson 라이브러리 사용)
	 */
	private CoordinateDto parseCoordinatesFromResponse(String jsonResponse) {
		try {
			JsonNode root = objectMapper.readTree(jsonResponse);

			JsonNode documents = root.get("documents");
			if (documents == null || !documents.isArray() || documents.isEmpty()) {
				return null;
			}

			JsonNode firstDocument = documents.get(0);
			String latitude = firstDocument.get("y").asText();
			String longitude = firstDocument.get("x").asText();

			return new CoordinateDto(latitude, longitude);

		} catch (Exception e) {
			log.error("JSON 파싱 중 오류 발생", e);
			return null;
		}
	}
}
