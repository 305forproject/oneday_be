# 1. Build Stage (빌드 환경)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사
# 의존성 캐싱을 위해 소스코드보다 먼저 복사합니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 윈도우에서 작성 후 리눅스로 복사될 때 실행 권한이 없을 수 있으므로 권한 부여
RUN chmod +x ./gradlew

# 종속성 다운로드 (소스 수정 시에도 이 단계까지는 캐시된 레이어 사용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
# 테스트는 배포 시 생략하여 속도를 높입니다 (-x test)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2. Run Stage (실행 환경)
# 실행 시에는 가벼운 JRE 21 이미지를 사용합니다.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사해옵니다.
# COPY --from=builder /app/build/libs/*.jar app.jar 로 하면 버전이 바뀌어도 대응 가능
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
