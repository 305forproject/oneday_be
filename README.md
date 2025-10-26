# OneDay Project

## 프로젝트 개요

Spring Boot 기반의 OneDay 애플리케이션입니다.

## 기술 스택

### Backend

- **Java**: 21
- **Spring Boot**: 3.5.7
- **빌드 도구**: Gradle

### 주요 의존성

- **Spring Boot Starter Data JPA**: 데이터베이스 ORM
- **Spring Boot Starter JDBC**: 데이터베이스 연결
- **Spring Boot Starter Web**: REST API 개발
- **Spring Boot DevTools**: 개발 편의성 향상
- **Lombok**: 보일러플레이트 코드 제거
- **MySQL Connector J**: MySQL 데이터베이스 드라이버
- **JUnit Platform**: 테스트 프레임워크

### 데이터베이스

- **MySQL**: 8.0

## 사전 요구사항

- Java 21 이상
- Docker & Docker Compose
- Gradle (Wrapper 포함)

## 환경 설정

### 1. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 설정합니다:

```env
MYSQL_PORT=3306
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=oneday
```

### 2. MySQL 초기화 스크립트 (선택사항)

`mysql-init` 디렉토리에 SQL 스크립트를 배치하면 컨테이너 시작 시 자동으로 실행됩니다.

## 실행 방법

### 1. Docker Compose로 MySQL 실행

```bash
# MySQL 컨테이너 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f mysql

# 컨테이너 상태 확인
docker-compose ps

# 컨테이너 중지
docker-compose down

# 볼륨까지 삭제 (데이터 완전 삭제)
docker-compose down -v
```

### 2. 애플리케이션 실행

```bash
# Gradle을 이용한 실행
./gradlew bootRun

# JAR 파일 빌드
./gradlew build

# 빌드된 JAR 파일 실행
java -jar build/libs/core-0.0.1-SNAPSHOT.jar
```

## Docker Compose 설정

### MySQL 서비스

- **컨테이너명**: `oneday-mysql`
- **이미지**: mysql:8.0
- **포트**: 환경 변수로 설정 (기본 3306)
- **문자셋**: UTF8MB4 (utf8mb4_unicode_ci)
- **타임존**: Asia/Seoul
- **자동 재시작**: 활성화
- **데이터 영속성**: `./mysql-data` 볼륨 마운트
- **초기화 스크립트**: `./mysql-init` 볼륨 마운트

### 네트워크

- **네트워크명**: `oneday-network`
- **드라이버**: Bridge

## 개발 모드

Spring Boot DevTools가 포함되어 있어 코드 변경 시 자동 재시작됩니다.

### DevTools 기능

- 자동 재시작 (Auto-restart)
- LiveReload 지원
- 개발 환경 최적화 설정

## 트러블슈팅

### MySQL 연결 실패

1. Docker 컨테이너가 실행 중인지 확인: `docker-compose ps`
2. `.env` 파일의 환경 변수 확인
3. 애플리케이션의 데이터베이스 설정 확인

### 포트 충돌

- `.env` 파일에서 `MYSQL_PORT`를 다른 포트로 변경

### 데이터 초기화

```bash
# 컨테이너와 볼륨 삭제 후 재시작
docker-compose down -v
docker-compose up -d
```

