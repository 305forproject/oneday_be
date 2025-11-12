# Phase 1: 기반 구조 준비하기 🏗️

> **목표**: JWT 기능을 만들기 위한 기본 설정과 파일 구조를 준비합니다.

## 📚 시작하기 전에

### JWT란 무엇인가요?

**JWT (JSON Web Token)**: 사용자 인증 정보를 안전하게 담은 토큰입니다.

- 로그인하면 서버가 토큰을 발급해줍니다
- 이후 요청마다 이 토큰을 보내서 "나 로그인한 사용자야!"라고 증명합니다
- 비밀번호를 매번 보낼 필요가 없어 안전합니다

### 전체 흐름 이해하기

```
1. 회원가입: 사용자 정보 저장 (비밀번호는 암호화)
2. 로그인: 이메일/비밀번호 확인 → JWT 토큰 발급
3. 인증된 요청: 토큰을 헤더에 담아 보냄 → 서버가 토큰 확인 → 요청 처리
```

---

## 1단계: 필요한 라이브러리 설치하기 (의존성 추가)

**📌 왜 필요한가요?**

- JWT를 만들고 검증하려면 전용 라이브러리가 필요합니다
- 보안 기능(비밀번호 암호화 등)을 위해 Spring Security가 필요합니다

**📝 작업할 파일**:

- `build.gradle` (라이브러리 설치 목록)
- `application.yml` (설정 값 저장)

### ✅ 해야 할 일

#### 1-1. `build.gradle`에 라이브러리 추가

```gradle
dependencies {
    // Spring Security: 보안 기능 제공
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT 라이브러리: 토큰 생성/검증
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
}
```

#### 1-2. `application.yml`에 JWT 설정 추가

```yaml
jwt:
  # 토큰을 만들 때 사용하는 비밀키 (실제로는 환경변수로 관리)
  secret: your-secret-key-change-this-in-production-min-256-bits
  
  # Access Token 유효시간 (1시간 = 3600000 밀리초)
  access-token-expiration: 3600000
  
  # Refresh Token 유효시간 (7일 = 604800000 밀리초)
  refresh-token-expiration: 604800000
```

**💡 용어 설명**:

- **Access Token**: 실제 API 요청에 사용하는 토큰 (짧은 유효기간)
- **Refresh Token**: Access Token이 만료되면 새로 발급받을 때 사용 (긴 유효기간)

---

## 2단계: 폴더 구조 이해하기

**📌 왜 필요한가요?**

- 코드를 역할별로 정리하면 찾기 쉽고 관리하기 좋습니다
- 팀원들과 협업할 때 어디에 무엇이 있는지 쉽게 알 수 있습니다

### 📂 폴더별 역할

```
src/main/java/com/oneday/core/
│
├── 📁 controller/          # API 엔드포인트 (URL 처리)
│   ├── auth/              # 로그인, 회원가입 API
│   └── user/              # 사용자 관련 API
│
├── 📁 service/            # 비즈니스 로직 (실제 처리)
│   ├── auth/              # 인증 로직
│   └── user/              # 사용자 관리 로직
│
├── 📁 repository/         # 데이터베이스 접근
│   └── user/              # 사용자 데이터 조회/저장
│
├── 📁 entity/             # 데이터베이스 테이블 구조
│   └── User.java          # 사용자 테이블
│
├── 📁 dto/                # 데이터 전달 객체 (요청/응답)
│   ├── auth/              # 인증 관련 요청/응답
│   └── common/            # 공통 응답 형식
│
├── 📁 exception/          # 에러 처리
│   ├── auth/              # 인증 관련 에러
│   ├── ErrorCode.java     # 에러 코드 정의
│   └── GlobalExceptionHandler.java  # 에러 처리 총괄
│
└── 📁 config/security/    # 보안 설정
    ├── SecurityConfig.java        # Spring Security 설정
    ├── JwtProperties.java         # JWT 설정 값
    ├── JwtTokenProvider.java      # JWT 생성/검증
    └── JwtAuthenticationFilter.java  # JWT 검사 필터
```

### 💡 데이터 흐름

```
1. Controller: 사용자 요청을 받음
   ↓
2. Service: 비즈니스 로직 처리
   ↓
3. Repository: 데이터베이스에 저장/조회
   ↓
4. Controller: 결과를 사용자에게 응답
```

---

## 3단계: 사용자 데이터 구조 만들기 (Entity)

**📌 왜 필요한가요?**

- 사용자 정보를 데이터베이스에 저장하려면 테이블 구조가 필요합니다
- User 엔티티는 사용자 정보를 담는 그릇입니다

**📝 작업할 파일**:

- `src/main/java/com/oneday/core/entity/User.java` (사용자 정보)
- `src/main/java/com/oneday/core/entity/Role.java` (권한 종류)
- `src/main/java/com/oneday/core/repository/user/UserRepository.java` (데이터 접근)

### ✅ 해야 할 일

#### 3-1. Role enum 만들기 (권한 정의)

```java
package com.oneday.core.entity;

// 사용자 권한을 구분합니다
public enum Role {
    USER,   // 일반 사용자
    ADMIN   // 관리자
}
```

#### 3-2. User 엔티티 만들기

```java
package com.oneday.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User implements UserDetails {  // Spring Security용 인터페이스
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // 사용자 고유 번호
    
    @Column(unique = true, nullable = false)
    private String email;               // 이메일 (로그인 ID)
    
    @Column(nullable = false)
    private String password;            // 비밀번호 (암호화됨)
    
    @Column(nullable = false)
    private String name;                // 이름
    
    @Enumerated(EnumType.STRING)
    private Role role;                  // 권한 (USER 또는 ADMIN)
    
    private LocalDateTime createdAt;    // 가입일
    private LocalDateTime updatedAt;    // 수정일
    
    // UserDetails 인터페이스 메서드 구현
    // (Spring Security가 사용자 정보를 가져올 때 사용)
    
    @Override
    public String getUsername() {
        return email;  // 이메일을 사용자명으로 사용
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### 3-3. UserRepository 만들기 (데이터베이스 접근)

```java
package com.oneday.core.repository.user;

import com.oneday.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 이메일 중복 확인
    boolean existsByEmail(String email);
}
```

**💡 용어 설명**:

- **Entity**: 데이터베이스 테이블과 매핑되는 클래스
- **Repository**: 데이터베이스에 접근하는 인터페이스
- **UserDetails**: Spring Security가 사용자 정보를 가져오는 표준 방식
- **@PrePersist**: 엔티티가 저장되기 직전에 실행되는 메서드
- **@PreUpdate**: 엔티티가 수정되기 직전에 실행되는 메서드

---

## ✅ Phase 1 체크리스트

- [ ] `build.gradle`에 라이브러리 추가 (Spring Security, JWT)
- [ ] `application.yml`에 JWT 설정 추가
- [ ] 폴더 구조 이해하고 필요한 디렉토리 생성
- [ ] `Role.java` enum 만들기
- [ ] `User.java` 엔티티 만들기
- [ ] `UserRepository.java` 만들기
- [ ] 애플리케이션 실행해서 에러 없는지 확인

---

## 다음 단계

✅ Phase 1 완료 후 → **[Phase 2: JWT 토큰 기능](phase2_jwt_core.md)** 로 이동하세요!

