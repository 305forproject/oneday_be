-- ============================================
-- 사용자 및 예약 상태 데이터
-- ============================================
-- 작성일: 2025-01-26
-- ============================================

-- ============================================
-- 사용자 데이터 (강사 10명 + 수강생 20명)
-- ============================================
-- 비밀번호: password1234 (BCrypt)
INSERT INTO users (id, email, password, name, role, created_at) VALUES
-- 강사 10명
(1, 'tuser1@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '김민준', 'USER', NOW()),
(2, 'tuser2@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '박서연', 'USER', NOW()),
(3, 'tuser3@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '이도현', 'USER', NOW()),
(4, 'tuser4@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '최지우', 'USER', NOW()),
(5, 'tuser5@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '정하은', 'USER', NOW()),
(6, 'tuser6@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '강동원', 'USER', NOW()),
(7, 'tuser7@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '윤아름', 'USER', NOW()),
(8, 'tuser8@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '서준혁', 'USER', NOW()),
(9, 'tuser9@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '한소희', 'USER', NOW()),
(10, 'tuser10@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '정우성', 'USER', NOW()),
-- 수강생 20명
(11, 'suser1@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '강예준', 'USER', NOW()),
(12, 'suser2@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '윤서아', 'USER', NOW()),
(13, 'suser3@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '임시우', 'USER', NOW()),
(14, 'suser4@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '한지민', 'USER', NOW()),
(15, 'suser5@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '오수현', 'USER', NOW()),
(16, 'suser6@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '송민서', 'USER', NOW()),
(17, 'suser7@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '장은호', 'USER', NOW()),
(18, 'suser8@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '최민지', 'USER', NOW()),
(19, 'suser9@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '김태양', 'USER', NOW()),
(20, 'suser10@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '박별이', 'USER', NOW()),
(21, 'suser11@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '이하늘', 'USER', NOW()),
(22, 'suser12@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '정바다', 'USER', NOW()),
(23, 'suser13@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '조슬기', 'USER', NOW()),
(24, 'suser14@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '강나래', 'USER', NOW()),
(25, 'suser15@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '홍길동', 'USER', NOW()),
(26, 'suser16@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '신사임당', 'USER', NOW()),
(27, 'suser17@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '세종대왕', 'USER', NOW()),
(28, 'suser18@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '이순신', 'USER', NOW()),
(29, 'suser19@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '유관순', 'USER', NOW()),
(30, 'suser20@test.com', '$2a$10$wJsc3GlrcG8mt1zaFKTPAOp5n3xeHkDNyVilKlakJjnsfL0FrogP6', '안중근', 'USER', NOW());

-- ============================================
-- 예약 상태 데이터
-- ============================================
INSERT INTO reservation_status (status_code, status_name) VALUES
(1, '예약완료'),
(2, '결제대기'),
(3, '예약취소'),
(4, '수업완료');
