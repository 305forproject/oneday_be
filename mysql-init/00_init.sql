-- ============================================
-- 데이터베이스 초기화
-- ============================================
-- 작성일: 2025-01-26
-- ============================================

-- 외래 키 체크 및 Safe Update 모드 비활성화
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- ============================================
-- 기존 데이터 삭제 (외래 키 의존성 역순)
-- ============================================
DELETE FROM payment;
DELETE FROM reservations;
DELETE FROM times;
DELETE FROM images;
DELETE FROM classes;
DELETE FROM reservation_status;
DELETE FROM refresh_tokens;
DELETE FROM users;

-- 외래 키 체크 및 Safe Update 모드 활성화
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;
