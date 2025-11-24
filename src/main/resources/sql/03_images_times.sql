-- ============================================
-- 이미지 및 시간 데이터
-- ============================================
-- 작성일: 2025-01-26
-- S3 버킷: s3-oneday.s3.ap-northeast-2.amazonaws.com
-- ============================================

-- ============================================
-- 이미지 데이터 (실제 S3 업로드 경로 반영)
-- ============================================
INSERT INTO images (image_id, class_id, image_url, is_representative) VALUES
-- Class 1: 수채화 (4개) - 2025/11/18/class-1
(1, 1, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/18/class-1/그림1.png', TRUE),
(2, 1, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/18/class-1/그림2.png', FALSE),
(3, 1, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/18/class-1/그림3.png', FALSE),
(4, 1, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/18/class-1/그림4.png', FALSE),

-- Class 2: 은반지 (5개) - 2025/11/19/class-2
(5, 2, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/19/class-2/금속공예1.png', TRUE),
(6, 2, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/19/class-2/금속공예2.png', FALSE),
(7, 2, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/19/class-2/금속공예3.png', FALSE),
(8, 2, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/19/class-2/금속공예4.png', FALSE),
(9, 2, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/19/class-2/금속공예5.png', FALSE),

-- Class 3: 꽃다발 (5개) - 2025/11/20/class-3
(10, 3, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-3/꽃꽂이1.png', TRUE),
(11, 3, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-3/꽃꽂이2.png', FALSE),
(12, 3, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-3/꽃꽂이3.png', FALSE),
(13, 3, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-3/꽃꽂이4.png', FALSE),
(14, 3, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-3/꽃꽂이5.png', FALSE),

-- Class 4: 도자기컵 (5개) - 2025/11/20/class-4
(15, 4, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-4/도자기공예1.png', TRUE),
(16, 4, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-4/도자기공예2.png', FALSE),
(17, 4, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-4/도자기공예3.png', FALSE),
(18, 4, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-4/도자기공예4.png', FALSE),
(19, 4, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/20/class-4/도자기공예5.png', FALSE),

-- Class 5: 아크릴화 (4개) - 2025/11/21/class-5
(20, 5, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/21/class-5/그림5.png', TRUE),
(21, 5, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/21/class-5/그림6.png', FALSE),
(22, 5, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/21/class-5/그림7.png', FALSE),
(23, 5, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/21/class-5/그림8.png', FALSE),

-- Class 6: 유화 (4개) - 2025/11/22/class-6
(24, 6, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/22/class-6/그림9.png', TRUE),
(25, 6, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/22/class-6/그림10.png', FALSE),
(26, 6, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/22/class-6/그림11.png', FALSE),
(27, 6, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/22/class-6/그림12.png', FALSE),

-- Class 7: 펜화 (4개) - 2025/11/23/class-7
(28, 7, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/23/class-7/그림13.png', TRUE),
(29, 7, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/23/class-7/그림14.png', FALSE),
(30, 7, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/23/class-7/그림15.png', FALSE),
(31, 7, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/23/class-7/그림16.png', FALSE),

-- Class 8: 프리저브드플라워 (5개) - 2025/11/24/class-8
(32, 8, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/24/class-8/꽃꽂이6.png', TRUE),
(33, 8, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/24/class-8/꽃꽂이7.png', FALSE),
(34, 8, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/24/class-8/꽃꽂이8.png', FALSE),
(35, 8, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/24/class-8/꽃꽂이9.png', FALSE),
(36, 8, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/24/class-8/꽃꽂이10.png', FALSE),

-- Class 9: 가을리스 (4개) - 2025/11/25/class-9
(37, 9, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/25/class-9/꽃꽂이11.png', TRUE),
(38, 9, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/25/class-9/꽃꽂이12.png', FALSE),
(39, 9, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/25/class-9/꽃꽂이13.png', FALSE),
(40, 9, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/25/class-9/꽃꽂이14.png', FALSE),

-- Class 10: 테이블플라워 (4개) - 2025/11/26/class-10
(41, 10, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/26/class-10/꽃꽂이15.png', TRUE),
(42, 10, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/26/class-10/꽃꽂이16.png', FALSE),
(43, 10, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/26/class-10/꽃꽂이17.png', FALSE),
(44, 10, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/26/class-10/꽃꽂이18.png', FALSE),

-- Class 11: 물레도자기 (5개) - 2025/11/27/class-11
(45, 11, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/27/class-11/도자기공예6.png', TRUE),
(46, 11, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/27/class-11/도자기공예7.png', FALSE),
(47, 11, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/27/class-11/도자기공예8.png', FALSE),
(48, 11, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/27/class-11/도자기공예9.png', FALSE),
(49, 11, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/27/class-11/도자기공예10.png', FALSE),

-- Class 12: 도자기페인팅 (5개) - 2025/11/28/class-12
(50, 12, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/28/class-12/도자기공예11.png', TRUE),
(51, 12, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/28/class-12/도자기공예12.png', FALSE),
(52, 12, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/28/class-12/도자기공예13.png', FALSE),
(53, 12, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/28/class-12/도자기공예14.png', FALSE),
(54, 12, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/28/class-12/도자기공예15.png', FALSE),

-- Class 13: 코일링화병 (5개) - 2025/11/29/class-13
(55, 13, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/29/class-13/도자기공예16.png', TRUE),
(56, 13, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/29/class-13/도자기공예17.png', FALSE),
(57, 13, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/29/class-13/도자기공예18.png', FALSE),
(58, 13, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/29/class-13/도자기공예19.png', FALSE),
(59, 13, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/29/class-13/도자기공예20.png', FALSE),

-- Class 14: 미니어처도자기 (3개) - 2025/11/30/class-14
(60, 14, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/30/class-14/도자기공예21.png', TRUE),
(61, 14, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/30/class-14/도자기공예22.png', FALSE),
(62, 14, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/11/30/class-14/도자기공예23.png', FALSE),

-- Class 15: 인물크로키 (4개) - 2025/12/01/class-15
(63, 15, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/01/class-15/드로잉1.png', TRUE),
(64, 15, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/01/class-15/드로잉2.png', FALSE),
(65, 15, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/01/class-15/드로잉3.png', FALSE),
(66, 15, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/01/class-15/드로잉4.png', FALSE),

-- Class 16: 색연필일러스트 (3개) - 2025/12/03/class-16
(67, 16, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/03/class-16/드로잉5.png', TRUE),
(68, 16, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/03/class-16/드로잉6.png', FALSE),
(69, 16, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/03/class-16/드로잉7.png', FALSE),

-- Class 17: 마카롱 (4개) - 2025/12/05/class-17
(70, 17, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/05/class-17/베이킹1.png', TRUE),
(71, 17, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/05/class-17/베이킹2.png', FALSE),
(72, 17, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/05/class-17/베이킹3.png', FALSE),
(73, 17, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/05/class-17/베이킹4.png', FALSE),

-- Class 18: 쿠키스콘 (4개) - 2025/12/07/class-18
(74, 18, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/07/class-18/베이킹5.png', TRUE),
(75, 18, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/07/class-18/베이킹6.png', FALSE),
(76, 18, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/07/class-18/베이킹7.png', FALSE),
(77, 18, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/07/class-18/베이킹8.png', FALSE),

-- Class 19: 치즈케이크 (4개) - 2025/12/09/class-19
(78, 19, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/09/class-19/베이킹9.png', TRUE),
(79, 19, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/09/class-19/베이킹10.png', FALSE),
(80, 19, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/09/class-19/베이킹11.png', FALSE),
(81, 19, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/09/class-19/베이킹12.png', FALSE),

-- Class 20: 크루아상 (4개) - 2025/12/11/class-20
(82, 20, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/11/class-20/베이킹13.png', TRUE),
(83, 20, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/11/class-20/베이킹14.png', FALSE),
(84, 20, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/11/class-20/베이킹15.png', FALSE),
(85, 20, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/11/class-20/베이킹16.png', FALSE),

-- Class 21: 홈메이드파스타 (5개) - 2025/12/13/class-21
(86, 21, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/13/class-21/요리1.png', TRUE),
(87, 21, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/13/class-21/요리2.png', FALSE),
(88, 21, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/13/class-21/요리3.png', FALSE),
(89, 21, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/13/class-21/요리4.png', FALSE),
(90, 21, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/13/class-21/요리5.png', FALSE),

-- Class 22: 일본가정식 (5개) - 2025/12/15/class-22
(91, 22, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/15/class-22/요리6.png', TRUE),
(92, 22, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/15/class-22/요리7.png', FALSE),
(93, 22, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/15/class-22/요리8.png', FALSE),
(94, 22, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/15/class-22/요리9.png', FALSE),
(95, 22, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/15/class-22/요리10.png', FALSE),

-- Class 23: 프렌치코스 (5개) - 2025/12/20/class-23
(96, 23, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/20/class-23/요리11.png', TRUE),
(97, 23, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/20/class-23/요리12.png', FALSE),
(98, 23, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/20/class-23/요리13.png', FALSE),
(99, 23, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/20/class-23/요리14.png', FALSE),
(100, 23, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2025/12/20/class-23/요리15.png', FALSE),

-- Class 24: 태국커리 (5개) - 2026/01/10/class-24
(101, 24, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/10/class-24/요리16.png', TRUE),
(102, 24, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/10/class-24/요리17.png', FALSE),
(103, 24, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/10/class-24/요리18.png', FALSE),
(104, 24, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/10/class-24/요리19.png', FALSE),
(105, 24, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/10/class-24/요리20.png', FALSE),

-- Class 25: 한식명절 (3개) - 2026/01/20/class-25
(106, 25, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/20/class-25/요리21.png', TRUE),
(107, 25, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/20/class-25/요리22.png', FALSE),
(108, 25, 'https://s3-oneday.s3.ap-northeast-2.amazonaws.com/images/2026/01/20/class-25/요리23.png', FALSE);

-- ============================================
-- 시간 데이터 (93개 - 클래스당 3~4개)
-- ============================================
-- 기준 날짜: 2025-11-21 (오늘), 범위: 2025-11-25 ~ 2026-01-25
INSERT INTO times (time_id, class_id, start_at, end_at) VALUES
-- Class 1: 수채화 (4개)
(1, 1, '2025-11-20 14:00:00', '2025-11-20 17:00:00'),
(2, 1, '2025-12-05 14:00:00', '2025-12-05 17:00:00'),
(3, 1, '2025-12-12 14:00:00', '2025-12-12 17:00:00'),
(4, 1, '2025-12-19 14:00:00', '2025-12-19 17:00:00'),

-- Class 2: 은반지 (4개)
(5, 2, '2025-11-20 13:00:00', '2025-11-20 16:00:00'),
(6, 2, '2025-12-07 13:00:00', '2025-12-07 16:00:00'),
(7, 2, '2025-12-14 13:00:00', '2025-12-14 16:00:00'),
(8, 2, '2025-12-21 13:00:00', '2025-12-21 16:00:00'),

-- Class 3: 꽃다발 (4개)
(9, 3, '2025-11-17 15:00:00', '2025-11-17 17:00:00'),
(10, 3, '2025-12-04 15:00:00', '2025-12-04 17:00:00'),
(11, 3, '2025-12-11 15:00:00', '2025-12-11 17:00:00'),
(12, 3, '2025-12-18 15:00:00', '2025-12-18 17:00:00'),

-- Class 4: 도자기컵 (3개)
(13, 4, '2025-11-01 10:00:00', '2025-11-01 15:00:00'),
(14, 4, '2025-12-15 10:00:00', '2025-12-15 15:00:00'),
(15, 4, '2026-01-05 10:00:00', '2026-01-05 15:00:00'),

-- Class 5: 아크릴화 (4개)
(16, 5, '2025-10-29 13:00:00', '2025-10-29 16:00:00'),
(17, 5, '2025-12-06 13:00:00', '2025-12-06 16:00:00'),
(18, 5, '2025-12-13 13:00:00', '2025-12-13 16:00:00'),
(19, 5, '2025-12-20 13:00:00', '2025-12-20 16:00:00'),

-- Class 6: 유화 (3개)
(20, 6, '2025-11-03 14:00:00', '2025-11-03 18:00:00'),
(21, 6, '2025-12-17 14:00:00', '2025-12-17 18:00:00'),
(22, 6, '2026-01-07 14:00:00', '2026-01-07 18:00:00'),

-- Class 7: 펜화 (4개)
(23, 7, '2025-10-26 16:00:00', '2025-10-26 19:00:00'),
(24, 7, '2025-12-03 16:00:00', '2025-12-03 19:00:00'),
(25, 7, '2025-12-10 16:00:00', '2025-12-10 19:00:00'),
(26, 7, '2025-12-17 16:00:00', '2025-12-17 19:00:00'),

-- Class 8: 프리저브드플라워 (3개)
(27, 8, '2025-11-02 13:00:00', '2025-11-02 16:00:00'),
(28, 8, '2025-12-16 13:00:00', '2025-12-16 16:00:00'),
(29, 8, '2026-01-06 13:00:00', '2026-01-06 16:00:00'),

-- Class 9: 가을리스 (4개)
(30, 9, '2025-11-25 14:00:00', '2025-11-25 17:00:00'),
(31, 9, '2025-12-02 14:00:00', '2025-12-02 17:00:00'),
(32, 9, '2025-12-09 14:00:00', '2025-12-09 17:00:00'),
(33, 9, '2025-12-16 14:00:00', '2025-12-16 17:00:00'),

-- Class 10: 테이블플라워 (4개)
(34, 10, '2025-11-28 15:00:00', '2025-11-28 18:00:00'),
(35, 10, '2025-12-05 15:00:00', '2025-12-05 18:00:00'),
(36, 10, '2025-12-12 15:00:00', '2025-12-12 18:00:00'),
(37, 10, '2025-12-19 15:00:00', '2025-12-19 18:00:00'),

-- Class 11: 물레도자기 (3개)
(38, 11, '2025-11-04 10:00:00', '2025-11-04 16:00:00'),
(39, 11, '2025-12-18 10:00:00', '2025-12-18 16:00:00'),
(40, 11, '2026-01-08 10:00:00', '2026-01-08 16:00:00'),

-- Class 12: 도자기페인팅 (4개)
(41, 12, '2025-11-27 13:00:00', '2025-11-27 16:00:00'),
(42, 12, '2025-12-04 13:00:00', '2025-12-04 16:00:00'),
(43, 12, '2025-12-11 13:00:00', '2025-12-11 16:00:00'),
(44, 12, '2025-12-18 13:00:00', '2025-12-18 16:00:00'),

-- Class 13: 코일링화병 (3개)
(45, 13, '2025-12-06 10:00:00', '2025-12-06 15:00:00'),
(46, 13, '2025-12-20 10:00:00', '2025-12-20 15:00:00'),
(47, 13, '2026-01-10 10:00:00', '2026-01-10 15:00:00'),

-- Class 14: 미니어처도자기 (4개)
(48, 14, '2025-11-29 14:00:00', '2025-11-29 17:00:00'),
(49, 14, '2025-12-06 14:00:00', '2025-12-06 17:00:00'),
(50, 14, '2025-12-13 14:00:00', '2025-12-13 17:00:00'),
(51, 14, '2025-12-20 14:00:00', '2025-12-20 17:00:00'),

-- Class 15: 인물크로키 (4개)
(52, 15, '2025-10-30 18:00:00', '2025-10-30 21:00:00'),
(53, 15, '2025-12-07 18:00:00', '2025-12-07 21:00:00'),
(54, 15, '2025-12-14 18:00:00', '2025-12-14 21:00:00'),
(55, 15, '2025-12-21 18:00:00', '2025-12-21 21:00:00'),

-- Class 16: 색연필일러스트 (3개)
(56, 16, '2025-12-01 14:00:00', '2025-12-01 17:00:00'),
(57, 16, '2025-12-15 14:00:00', '2025-12-15 17:00:00'),
(58, 16, '2026-01-05 14:00:00', '2026-01-05 17:00:00'),

-- Class 17: 마카롱 (4개)
(59, 17, '2025-11-26 10:00:00', '2025-11-26 14:00:00'),
(60, 17, '2025-12-03 10:00:00', '2025-12-03 14:00:00'),
(61, 17, '2025-12-10 10:00:00', '2025-12-10 14:00:00'),
(62, 17, '2025-12-17 10:00:00', '2025-12-17 14:00:00'),

-- Class 18: 쿠키스콘 (4개)
(63, 18, '2025-10-28 13:00:00', '2025-10-28 16:00:00'),
(64, 18, '2025-12-05 13:00:00', '2025-12-05 16:00:00'),
(65, 18, '2025-12-12 13:00:00', '2025-12-12 16:00:00'),
(66, 18, '2025-12-19 13:00:00', '2025-12-19 16:00:00'),

-- Class 19: 치즈케이크 (3개)
(67, 19, '2025-12-02 14:00:00', '2025-12-02 18:00:00'),
(68, 19, '2025-12-16 14:00:00', '2025-12-16 18:00:00'),
(69, 19, '2026-01-06 14:00:00', '2026-01-06 18:00:00'),

-- Class 20: 크루아상 (3개)
(70, 20, '2025-12-05 09:00:00', '2025-12-05 15:00:00'),
(71, 20, '2025-12-19 09:00:00', '2025-12-19 15:00:00'),
(72, 20, '2026-01-09 09:00:00', '2026-01-09 15:00:00'),

-- Class 21: 홈메이드파스타 (4개)
(73, 21, '2025-11-27 17:00:00', '2025-11-27 20:00:00'),
(74, 21, '2025-12-04 17:00:00', '2025-12-04 20:00:00'),
(75, 21, '2025-12-11 17:00:00', '2025-12-11 20:00:00'),
(76, 21, '2025-12-18 17:00:00', '2025-12-18 20:00:00'),

-- Class 22: 일본가정식 (4개)
(77, 22, '2025-11-29 17:00:00', '2025-11-29 20:00:00'),
(78, 22, '2025-12-06 17:00:00', '2025-12-06 20:00:00'),
(79, 22, '2025-12-13 17:00:00', '2025-12-13 20:00:00'),
(80, 22, '2025-12-20 17:00:00', '2025-12-20 20:00:00'),

-- Class 23: 프렌치코스 (3개)
(81, 23, '2025-12-07 18:00:00', '2025-12-07 22:00:00'),
(82, 23, '2025-12-21 18:00:00', '2025-12-21 22:00:00'),
(83, 23, '2026-01-11 18:00:00', '2026-01-11 22:00:00'),

-- Class 24: 태국커리 (4개)
(84, 24, '2025-11-30 16:00:00', '2025-11-30 19:00:00'),
(85, 24, '2025-12-07 16:00:00', '2025-12-07 19:00:00'),
(86, 24, '2025-12-14 16:00:00', '2025-12-14 19:00:00'),
(87, 24, '2025-12-21 16:00:00', '2025-12-21 19:00:00'),

-- Class 25: 한식명절 (3개)
(88, 25, '2025-12-08 10:00:00', '2025-12-08 14:00:00'),
(89, 25, '2025-12-22 10:00:00', '2025-12-22 14:00:00'),
(90, 25, '2026-01-12 10:00:00', '2026-01-12 14:00:00'),

-- 추가 시간 (인기 클래스용)
(91, 1, '2026-01-02 14:00:00', '2026-01-02 17:00:00'),
(92, 17, '2026-01-03 10:00:00', '2026-01-03 14:00:00'),
(93, 21, '2026-01-04 17:00:00', '2026-01-04 20:00:00');
