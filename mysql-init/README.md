# 테스트 데이터 초기화 가이드

## 📁 파일 구조

```
mysql-init/
├── 00_init.sql                      # 초기화 (기존 데이터 삭제)
├── 01_users_and_status.sql         # 사용자 30명 + 예약상태 4개
├── 02_classes.sql                   # 클래스 25개 (data.sql에서 추출 필요)
├── 03_images_times.sql             # 이미지 108개 + 시간 93개 (data.sql에서 추출 필요)
├── 04_reservations_payments.sql    # 예약 273건 + 결제 273건 ✅
└── master.sql                       # 전체 실행 마스터 파일
```

## ✅ 완료된 작업

1. ✅ **사용자 데이터**: `01_users_and_status.sql` 생성 완료

   - 강사 10명 (id: 1-10, tuser1~10@test.com)
   - 수강생 20명 (id: 11-30, suser1~20@test.com)
   - 예약 상태 4개 (예약완료, 결제대기, 예약취소, 수업완료)

2. ✅ **예약/결제 데이터**: `04_reservations_payments.sql` 생성 완료
   - 273개 예약 (시간대별 0-70% 예약률)
   - 273개 결제 (Toss 결제 시뮬레이션)
   - 상태 분포: 50% 예약완료, 30% 결제대기, 10% 취소, 10% 완료

## 🔧 추가 작업 필요

### 1. 클래스 데이터 파일 생성

`data.sql` 파일에서 25개 클래스 데이터를 추출하여 `02_classes.sql`에 저장해야 합니다.

**수동 작업**:

1. `data.sql` 열기
2. 78번 줄부터 클래스 데이터 시작
3. 25개 클래스 INSERT 문 복사
4. `mysql-init/02_classes.sql`에 붙여넣기
5. 파일 시작 부분에 주석 추가:

```sql
-- ============================================
-- 클래스 데이터 (25개)
-- ============================================
-- 작성일: 2025-01-26
-- CategoryInitializer가 생성한 Categories 사용
-- 1: 건강/뷰티, 2: 공예/예술, 3: 스포츠/레저, 4: 요리/베이킹
-- 5: 음악/댄스, 6: 언어/교육, 7: IT/기술, 8: 라이프스타일
-- ============================================

INSERT INTO classes (class_id, teacher_id, category_id, class_name, ...) VALUES
-- (클래스 데이터)
```

### 2. 이미지 및 시간 데이터 파일 생성

`data.sql` 파일에서 이미지와 시간 데이터를 추출하여 `03_images_times.sql`에 저장해야 합니다.

**수동 작업**:

1. `data.sql` 열기
2. 168번 줄부터 이미지 데이터 시작
3. 108개 이미지 + 93개 시간 INSERT 문 복사
4. `mysql-init/03_images_times.sql`에 붙여넣기
5. 파일 시작 부분에 주석 추가:

```sql
-- ============================================
-- 이미지 및 시간 데이터
-- ============================================
-- 작성일: 2025-01-26
-- 이미지: 108개 (클래스당 3~5개, 첫 번째는 대표 이미지)
-- 시간: 93개 (클래스당 2~5개 시간대)
-- ============================================

INSERT INTO images (image_id, class_id, image_url, is_representative) VALUES
-- (이미지 데이터)

INSERT INTO times (time_id, class_id, start_at, end_at) VALUES
-- (시간 데이터)
```

## 🚀 데이터베이스 초기화 방법

### 방법 1: 마스터 파일로 전체 실행 (권장)

```bash
cd c:\Users\WD\oneday_be
mysql -u root -p oneday_db < mysql-init/master.sql
```

### 방법 2: 개별 파일 순차 실행

```bash
cd c:\Users\WD\oneday_be
mysql -u root -p oneday_db < mysql-init/00_init.sql
mysql -u root -p oneday_db < mysql-init/01_users_and_status.sql
mysql -u root -p oneday_db < mysql-init/02_classes.sql
mysql -u root -p oneday_db < mysql-init/03_images_times.sql
mysql -u root -p oneday_db < mysql-init/04_reservations_payments.sql
```

### 방법 3: MySQL Workbench 사용

1. MySQL Workbench 열기
2. File > Run SQL Script 선택
3. `master.sql` 파일 선택
4. 실행

## 📊 데이터 검증 쿼리

초기화 후 다음 쿼리로 데이터를 확인하세요:

```sql
-- 전체 데이터 개수 확인
SELECT '사용자' AS 테이블, COUNT(*) AS 개수 FROM users
UNION ALL SELECT '예약상태', COUNT(*) FROM reservation_status
UNION ALL SELECT '클래스', COUNT(*) FROM classes
UNION ALL SELECT '이미지', COUNT(*) FROM images
UNION ALL SELECT '시간', COUNT(*) FROM times
UNION ALL SELECT '예약', COUNT(*) FROM reservations
UNION ALL SELECT '결제', COUNT(*) FROM payment;

-- 예약 상태별 분포
SELECT rs.status_name, COUNT(*) AS 개수
FROM reservations r
JOIN reservation_status rs ON r.status_code = rs.status_code
GROUP BY rs.status_name;

-- 클래스별 예약 현황
SELECT c.class_name, COUNT(DISTINCT r.reservation_id) AS 예약수, c.max_capacity AS 정원
FROM classes c
LEFT JOIN times t ON c.class_id = t.class_id
LEFT JOIN reservations r ON t.time_id = r.time_id
GROUP BY c.class_id
ORDER BY c.class_id;
```

## ⚠️ 주의사항

1. **외래 키 제약조건**: 파일 실행 순서가 중요합니다.
2. **인코딩**: UTF-8 인코딩 유지 필수 (한글 깨짐 방지)
3. **CategoryInitializer**: Categories 테이블은 애플리케이션이 자동 생성하므로 SQL에 포함하지 않음
4. **기존 데이터**: `00_init.sql`이 모든 테이블 데이터를 삭제하므로 주의

## 📝 다음 단계

1. [ ] `02_classes.sql` 파일 수동 생성
2. [ ] `03_images_times.sql` 파일 수동 생성
3. [ ] 전체 데이터 초기화 실행
4. [ ] 데이터 검증 쿼리 실행
5. [ ] Spring Boot 애플리케이션 실행 테스트

## 🔗 관련 파일

- `data.sql`: 원본 통합 SQL 파일
- `reservations_payments.sql`: 예약/결제 생성 소스 파일
- `generate_test_data.py`: 예약/결제 데이터 생성 Python 스크립트
