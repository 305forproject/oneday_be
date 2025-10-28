---
apply: always
---

# Git 및 협업 규칙

## 1. Git 브랜치 전략

### 1.1 브랜치 종류

- **main**: 배포 가능한 안정 버전
- **develop**: 개발 중인 최신 코드
- **feature/기능명**: 새로운 기능 개발
- **fix/버그명**: 버그 수정

### 1.2 브랜치 네이밍 규칙

```bash
# 기능 개발
feature/user-login
feature/order-management

# 버그 수정
fix/user-validation-error
fix/order-calculation-bug

# 핫픽스
hotfix/critical-security-patch
```

## 2. Git 커밋 메시지 규칙

### 2.1 커밋 메시지 형식

```
<타입>: <제목>

<본문> (선택사항)

<꼬리말> (선택사항)
```

### 2.2 타입 종류

- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **docs**: 문서 수정
- **style**: 코드 포맷팅, 세미콜론 누락 등 (기능 변경 없음)
- **refactor**: 코드 리팩토링
- **test**: 테스트 코드 추가/수정
- **chore**: 빌드 작업, 패키지 매니저 설정 등

### 2.3 커밋 메시지 예시

```bash
# 기본 형식
feat: 사용자 로그인 기능 추가

# 본문이 있는 경우
feat: 사용자 로그인 기능 추가

JWT 토큰 기반 인증 방식 구현
- 로그인 API 엔드포인트 추가
- 토큰 생성 및 검증 로직 구현

# 버그 수정
fix: 주문 금액 계산 오류 수정

# 문서 수정
docs: README에 설치 방법 추가

# 리팩토링
refactor: UserService 메서드 분리

# 테스트
test: UserService 단위 테스트 추가
```

### 2.4 커밋 메시지 작성 규칙

- 제목은 50자 이내
- 제목 첫 글자는 대문자 또는 한글로 시작
- 제목 끝에 마침표(.) 금지
- 제목은 명령문으로 작성 ("추가함" ❌, "추가" ✅)
- 본문은 72자마다 줄바꿈
- 본문은 "무엇을", "왜" 변경했는지 설명

## 3. Git 워크플로우

### 3.1 기능 개발 플로우

```bash
# 1. develop 브랜치에서 최신 코드 받기
git checkout develop
git pull origin develop

# 2. feature 브랜치 생성
git checkout -b feature/user-login

# 3. 작업 진행 및 커밋
git add .
git commit -m "feat: 사용자 로그인 API 추가"

# 4. 원격 저장소에 푸시
git push origin feature/user-login

# 5. Pull Request 생성

# 6. 코드 리뷰 후 병합

# 7. 로컬 브랜치 삭제
git checkout develop
git branch -d feature/user-login
```

### 3.2 커밋 단위

- **작은 단위로 자주 커밋**
- 하나의 커밋은 하나의 의미 있는 변경사항만 포함
- 컴파일 에러가 없는 상태로 커밋

```bash
# Good - 작은 단위 커밋
git commit -m "feat: User 엔티티 추가"
git commit -m "feat: UserRepository 추가"
git commit -m "feat: UserService 추가"
git commit -m "test: UserService 테스트 추가"

# Bad - 너무 큰 단위
git commit -m "feat: 사용자 관리 기능 전체 추가"
```

## 4. Pull Request (PR) 규칙

### 4.1 PR 제목

- 커밋 메시지 규칙과 동일

```
feat: 사용자 로그인 기능 추가
fix: 주문 계산 오류 수정
```

### 4.2 PR 설명 템플릿

```markdown
## 작업 내용
- 사용자 로그인 API 구현
- JWT 토큰 기반 인증 추가

## 변경 사항
- UserController 추가
- AuthService 추가
- SecurityConfig 수정

## 테스트
- [ ] 단위 테스트 작성 완료
- [ ] 로컬 환경에서 동작 확인

## 스크린샷 (필요시)

## 관련 이슈
- #123
```

### 4.3 PR 생성 전 체크리스트

- [ ] 코드 컴파일 성공
- [ ] 테스트 통과
- [ ] 코드 스타일 준수
- [ ] 주석 및 Javadoc 작성
- [ ] 불필요한 코드 제거 (주석 처리된 코드, import 등)

## 5. 코드 리뷰 가이드

### 5.1 리뷰어 체크 포인트

- [ ] 요구사항이 정확히 구현되었는가?
- [ ] 비즈니스 로직이 Service 계층에 있는가?
- [ ] 예외 처리가 적절한가?
- [ ] 테스트 코드가 작성되었는가?
- [ ] 네이밍이 명확한가?
- [ ] 불필요한 코드가 없는가?
- [ ] 보안 이슈가 없는가?

### 5.2 리뷰 코멘트 작성

```markdown
# Good - 건설적인 피드백
💡 제안: UserService의 createUser 메서드가 너무 길어 보입니다. 
검증 로직을 별도 메서드로 분리하면 가독성이 좋아질 것 같습니다.

❓ 질문: 이메일 중복 체크를 두 번 하는 이유가 있나요?

# Bad - 명확하지 않은 피드백
이 코드는 좋지 않습니다.
다시 작성해주세요.
```

### 5.3 리뷰 승인 기준

- 모든 체크 포인트 통과
- 테스트 커버리지 80% 이상
- 빌드 성공
- 최소 1명의 리뷰어 승인

## 6. 협업 규칙

### 6.1 코드 소유권

- 모든 코드는 팀의 공동 소유
- 다른 팀원의 코드도 자유롭게 수정 가능 (리뷰 후)
- 중요한 변경사항은 팀 논의 후 진행

### 6.2 의사소통

- PR은 24시간 내 리뷰
- 긴급한 경우 직접 커뮤니케이션
- 코드로 설명할 수 없는 경우 주석 추가

### 6.3 분쟁 해결

- 의견 불일치 시 팀 미팅에서 결정
- 합의가 안 될 경우 간단한 방법 선택 (YAGNI 원칙)
- 결정 사항은 문서화

## 7. .gitignore 설정

### 7.1 기본 .gitignore

```
# IDE
.idea/
*.iml
.vscode/

# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

# OS
.DS_Store
Thumbs.db

# Application
application-local.yml
application-*.yml
!application.yml

# Logs
logs/
*.log

# Database
mysql-data/
```

## 8. Git 명령어 가이드

### 8.1 자주 사용하는 명령어

```bash
# 상태 확인
git status

# 변경사항 확인
git diff

# 커밋 로그 확인
git log --oneline --graph

# 브랜치 목록
git branch -a

# 리모트 브랜치와 동기화
git fetch origin

# 최신 코드 받기
git pull origin develop

# 충돌 해결 후
git add .
git commit -m "merge: develop 브랜치 병합"
```

### 8.2 실수 복구

```bash
# 마지막 커밋 수정
git commit --amend

# 작업 내용 임시 저장
git stash
git stash pop

# 특정 커밋으로 되돌리기 (협업 시 주의)
git reset --hard <commit-hash>

# 커밋 취소 (이력 유지)
git revert <commit-hash>
```

