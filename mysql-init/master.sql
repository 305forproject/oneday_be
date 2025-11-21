-- ============================================
-- 테스트 데이터 초기화 마스터 파일
-- ============================================
-- 작성일: 2025-01-26
-- 수정일: 2025-01-26 (25개 클래스 데이터 추가 완료)
-- 
-- 📋 포함된 데이터:
-- 1. 00_init.sql - 데이터베이스 초기화 및 기존 데이터 삭제
-- 2. 01_users_and_status.sql - 사용자 30명 (강사 10명, 수강생 20명), 예약 상태 4개
-- 3. 02_classes.sql - 클래스 25개 (공예/예술 16개, 요리/베이킹 9개)
-- 4. 03_images_times.sql - 이미지 108개 (실제 S3 경로), 시간 93개
-- 5. 04_reservations_payments.sql - 예약 536건, 결제 536건 (예약률 30-80%, 상태 분포 realistic)
-- 
-- 🚀 MySQL에서 실행 방법:
-- mysql -u root -p oneday_db < mysql-init/master.sql
-- 
-- 또는 각 파일을 개별적으로 실행:
-- mysql -u root -p oneday_db < mysql-init/00_init.sql
-- mysql -u root -p oneday_db < mysql-init/01_users_and_status.sql
-- mysql -u root -p oneday_db < mysql-init/02_classes.sql
-- mysql -u root -p oneday_db < mysql-init/03_images_times.sql
-- mysql -u root -p oneday_db < mysql-init/04_reservations_payments.sql
-- ============================================

SOURCE mysql-init/00_init.sql;
SOURCE mysql-init/01_users_and_status.sql;
SOURCE mysql-init/02_classes.sql;
SOURCE mysql-init/03_images_times.sql;
SOURCE mysql-init/04_reservations_payments.sql;
