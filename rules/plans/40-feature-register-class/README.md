# 클래스 등록 기능 구현 계획

## 📋 개요

**기능명**: 클래스 등록 (Class Registration)  
**PRD 위치**: `rules/prds/40-feature-register-class/README.md`  
**브랜치**: `40-feature-register-class`  
**예상 총 소요 시간**: 8-10시간

---

## 🎯 목표

강사(INSTRUCTOR)가 원데이 클래스를 등록할 수 있는 API를 구현합니다.

### 핵심 요구사항

- [x] 인증된 INSTRUCTOR 사용자만 클래스 등록 가능
- [x] 카테고리, 클래스명, 가격, 시간, 이미지 정보 입력
- [x] 시간 중복 검증 (동일 강사의 동일 시간대)
- [x] 트랜잭션 관리로 데이터 무결성 보장
- [x] 8가지 에러 코드 처리 (CL001~CL008)

---

## 📂 Phase 구조

### Phase 1: 기본 설정 및 엔티티 준비

- **목표**: CategoryType Enum 생성 및 Categories 테이블 초기화
- **소요 시간**: 1시간
- **파일**: `phase-1-setup-and-entities.md`

### Phase 2: 예외 처리 구조

- **목표**: 도메인별 예외 클래스 및 ErrorCode 정의
- **소요 시간**: 1시간
- **파일**: `phase-2-exception-handling.md`

### Phase 3: Repository 계층

- **목표**: 데이터 접근 계층 구현 (CRUD + 시간 중복 체크)
- **소요 시간**: 1시간
- **파일**: `phase-3-repository-layer.md`

### Phase 4: Service 계층

- **목표**: 핵심 비즈니스 로직 구현 (클래스 등록)
- **소요 시간**: 2-3시간
- **파일**: `phase-4-service-layer.md`

### Phase 5: Controller 계층

- **목표**: REST API 엔드포인트 구현
- **소요 시간**: 1시간
- **파일**: `phase-5-controller-layer.md`

### Phase 6: 테스트

- **목표**: 단위 테스트 및 통합 테스트 작성
- **소요 시간**: 2-3시간
- **파일**: `phase-6-testing.md`

---

## 🚀 시작하기

### 1. 현재 브랜치 확인

```bash
git branch
# * 40-feature-register-class
```

### 2. Phase 1부터 순차적으로 진행

```bash
# Phase 1 계획 확인
cat rules/plans/40-feature-register-class/phase-1-setup-and-entities.md
```

### 3. 각 Phase 완료 후 체크리스트 업데이트

```markdown
- [x] Phase 1 완료
- [ ] Phase 2 진행 중
```

---

## 📌 YAGNI 원칙 적용 사항

### ✅ 현재 구현 범위

- CategoryType Enum (8개 카테고리)
- Categories 테이블 자동 초기화
- 기본적인 시간 중복 검증
- 필수 비즈니스 로직만 구현
- 핵심 테스트 시나리오 (7개)

### ❌ 제외 (향후 작업)

- 파일 업로드 상세 검증 (확장자, 크기)
- S3 연동
- 이미지 썸네일 생성
- 클래스 수정/삭제 API
- 페이징 처리
- 검색 기능

---

## 🔗 관련 문서

- PRD: `rules/prds/40-feature-register-class/README.md`
- Copilot Instructions: `.github/copilot-instructions.md`
- 아키텍처 규칙: `rules/architecture.md`
- 예외 처리 규칙: `rules/exception_handling.md`

---

## 📊 진행 상황

- [x] Phase 1: 기본 설정 및 엔티티 준비 ✅
- [ ] Phase 2: 예외 처리 구조
- [ ] Phase 3: Repository 계층
- [ ] Phase 4: Service 계층
- [ ] Phase 5: Controller 계층
- [ ] Phase 6: 테스트

**전체 진행률**: 1/6 (17%)

---

## 👤 작성자

- **작성일**: 2025-01-26
- **최종 수정일**: 2025-01-26

