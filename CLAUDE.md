# CLAUDE.md

이 문서는 Claude Code가 이 저장소에서 코드를 작성할 때 반드시 따라야 하는 절대 기준이다.

---

## 핵심 철학

이 프로젝트는 다음을 강하게 지향한다.

- DDD (도메인 주도 설계)
- OOP (객체지향 설계)
- 일관된 네이밍과 포매팅
- 유지보수하기 좋은 코드
- 적절한 책임 분배 (SRP)

"동작하는 코드"보다 "올바른 위치에 있는 코드"가 더 중요하다.

---

## 코드 작성 사고 순서

1. 도메인을 이해한다.
2. 비즈니스 규칙과 불변식을 정의한다.
3. 로직이 들어갈 올바른 위치를 먼저 결정한다.
4. 가장 단순하게 구현한다 (YAGNI).
5. 네이밍으로 의도를 드러낸다.

---

## 명령어

```bash
./gradlew build
./gradlew bootRun
./gradlew test
./gradlew test --tests "org.smu.randsome.randsomeback.패키지.ClassName"
./gradlew clean build
./gradlew compileJava   # QueryDSL Q클래스 생성
```

---

## 기술 스택

Java 21 / Spring Boot 3.5.11 / Spring Data JPA + QueryDSL 7.1 / H2(local·test) · MySQL(dev·prod 예정) / JJWT 0.12.3 / SpringDoc OpenAPI 2.8.9 / Caffeine Cache / Sentry + Prometheus / Gradle 8.x

---

## 패키지 구조

```text
org.smu.randsome.randsomeback/
├── domain/{도메인}/
│   ├── entity/        — Aggregate Root, Entity, Value Object
│   ├── repository/    — Repository 인터페이스
│   ├── controller/    — HTTP 요청·응답 변환, ApiResponse 조합
│   ├── service/       — 유스케이스 흐름 조율, 트랜잭션 경계
│   └── implement/     — 실제 로직 실행 (Reader / Manager / Validator / ...)
├── admin/{도메인}/    — 관리자 전용 기능, 구조 동일
├── global/            — config, entity(BaseEntity), jwt, support(error·response), swagger, annotation
└── infrastructure/    — 외부 인프라 연동 (이메일 등)
```

---

## 계층 책임

| 계층 | 해야 하는 것 | 절대 금지 |
|------|-------------|----------|
| Controller | HTTP 변환, ApiResponse 조합 | 비즈니스 로직, Repository 호출 |
| Service | 유스케이스 흐름, 트랜잭션 | Repository 직접 호출, ApiResponse 반환 |
| Implement | 실제 로직, Repository 호출 | 트랜잭션 경계, ApiResponse 반환 |
| Entity | 비즈니스 규칙, 상태 변경 캡슐화 | 외부 의존, setter 공개 |

---

## 네이밍 규칙

### 클래스

| 역할 | 규칙 | 예시 |
|------|------|------|
| 조회 전담 | `{Domain}Reader` | `MemberReader` |
| 쓰기 전담 | `{Domain}Manager` | `MemberManager` |
| 검증 전담 | `{Domain}Validator` | `MemberValidator` |
| 캐시 무효화 | `{Domain}CacheEvictor` | `AnnouncementCacheEvictor` |
| 전략 패턴 | `{Domain}Strategy` | `RandomMatchingStrategy` |
| 도메인 이벤트 | `{Domain}{Action}Event` | `AnnouncementRegisteredEvent` |
| 요청 DTO | `{Action}Request` | `AnnouncementRegisterRequest` |
| 응답 DTO | `{Domain}Response`, `{Domain}Item` | `AnnouncementItem` |
| 내부 커맨드 DTO | `New{Domain}`, `Update{Domain}` | `NewAnnouncement` |
| Swagger 문서 | `{Controller}Docs` | `AnnouncementControllerDocs` |

### 메서드

| 행위 | 네이밍 |
|------|--------|
| 단건 조회 | `find()`, `findBy{조건}()` — 없으면 예외 |
| 목록 조회 | `findAll()`, `findAllBy{조건}()` |
| 존재 확인 | `exists{조건}()` — boolean 반환 |
| 생성 | `create()`, `register()` |
| 수정 | `update{대상}()` |
| 삭제 | `delete()` — 소프트 삭제 |
| 검증 | `validate{대상}()` — 실패 시 예외 |

### 변수

- 타입명을 그대로 변수명으로 쓰지 않는다: `Member member` (X) → `Member admin` (O)
- boolean 필드·메서드는 `is{상태}()` 형태로 작성한다.
- 약어를 피한다: `req` (X) → `request` (O)

---

## DDD / OOP 핵심 규칙

### Anemic Domain Model 금지

```java
// Bad
member.setName(name);

// Good
member.changeName(name);
```

### 비즈니스 로직은 Entity 내부에

상태 변경은 엔티티 메서드로만 수행한다. 외부에서 상태를 꺼내 판단하지 않는다.

### Tell, Don't Ask

```java
// Bad
if (member.getRole() == Role.ROLE_ADMIN) { ... }

// Good
member.isAdmin();
// 또는
memberValidator.validateAdmin(member);
```

### 단일 책임

- 한 클래스는 한 가지 역할만 담당한다.
- 한 클래스가 Reader · Manager · Validator 역할을 동시에 가지면 분리한다.

---

## 트랜잭션 규칙

- 트랜잭션 경계는 Service 계층만 담당한다.
- 읽기 전용 유스케이스는 `@Transactional(readOnly = true)`.
- 캐시 무효화 등 부수 효과는 트랜잭션 커밋 이후에 처리한다.

```java
// Service: 이벤트 발행
@Transactional
public void register(...) {
    ...
    eventPublisher.publishEvent(new AnnouncementRegisteredEvent(id));
}

// CacheEvictor: 커밋 후 실행
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@CacheEvict(cacheNames = CacheConfig.ANNOUNCEMENTS, allEntries = true)
public void handle(AnnouncementRegisteredEvent event) { }
```

---

## 에러 처리

모든 비즈니스 예외는 `CoreException(ErrorType)`으로 던진다. `GlobalExceptionHandler`가 자동으로 `ApiResponse.error()`로 변환한다.

```java
throw new CoreException(ErrorType.NOT_FOUND_MEMBER);
```

새 에러 타입은 `ErrorType` enum에 추가하고 `HttpStatus`와 `LogLevel`을 지정한다.

---

## 소프트 삭제

물리 삭제 대신 `entity.delete()`를 호출한다. 조회 시 반드시 `status = EntityStatus.ACTIVE` 조건을 포함한다.

---

## 테스트 원칙

- Red → Green → Refactor 사이클을 따른다.
- 리팩터링(구조 변경)과 기능 추가(동작 변경)를 같은 커밋에 섞지 않는다.
- 큰 변경을 한 번에 하지 않는다. 항상 테스트가 통과하는 작은 단계로 나눈다.

| 베이스 클래스 | 용도 |
|-------------|------|
| `UnitTestSupport` | 단위 테스트 (`@ExtendWith(MockitoExtension.class)`) |
| `IntegrationTestSupport` | 통합 테스트 (`@SpringBootTest`) |
| `ControllerTestSupport` | 컨트롤러 테스트 (`@WebMvcTest` + `MockMvcTester`) |

---

## 안티패턴 (절대 금지)

- Fat Service — Service가 비즈니스 로직을 직접 구현
- Anemic Entity — 데이터만 있고 로직이 없는 Entity
- God Object — 하나의 클래스가 모든 것을 담당
- Setter 남발 — 상태 변경 의도를 숨김
- 계층 책임 혼합 — Controller에서 비즈니스 로직, Service에서 ApiResponse 반환

---

## 주의 사항

- **Spring Security**: `/v1/auth/**`, `/v1/members/sign-up`, `/v1/feed`, Swagger, Actuator health/info 는 인증 불필요. `/v1/admin/**` 은 `ROLE_ADMIN` 전용.
- **MySQL 드라이버**: 현재 주석 처리. `dev`/`prod` 프로파일 적용 전 활성화 필요.
- **시크릿 키**: `application-test.yml`의 JWT 키는 테스트 전용. 실제 운영 키 절대 커밋 금지.
- **QueryDSL Q클래스**: `./gradlew compileJava` 후 `build/generated`에 생성.
- **타임존**: 날짜/시간은 항상 `LocalDateTime` 사용 (Asia/Seoul 고정).

---

## 최종 기준

코드를 작성할 때 항상 묻는다.

> 이 로직은 이 계층에 있는 게 맞는가?

아니라면 틀린 코드다.

- Entity = 비즈니스 로직
- Service = 유스케이스 흐름
- Implement = 실제 실행
- Controller = API 변환

일관성 + 책임 분리 + 유지보수성이 가장 중요한 기준이다.
