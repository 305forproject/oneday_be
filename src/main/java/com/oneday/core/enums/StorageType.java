package com.oneday.core.enums;

/**
 * 파일 저장소 타입
 *
 * @author zionge2k
 * @since 2025-01-27
 */
public enum StorageType {
	S3, LOCAL;

	/**
	 * 문자열을 StorageType으로 변환
	 *
	 * @param type 저장소 타입 문자열 (대소문자 무관)
	 * @return StorageType
	 * @throws UnsupportedOperationException 지원하지 않는 타입인 경우
	 */
	public static StorageType from(String type) {
		try {
			return valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new UnsupportedOperationException(
				"지원하지 않는 storage-type입니다: " + type + ". 's3' 또는 'local'을 사용하세요.");
		}
	}
}

