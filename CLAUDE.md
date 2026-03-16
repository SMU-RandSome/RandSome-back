# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 명령어

```bash
# 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 전체 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "org.smu.randsome.randsomeback.패키지.ClassName"

# 클린 빌드
./gradlew clean build
```

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.x |
| ORM | Spring Data JPA (Hibernate) |
| 동적 쿼리 | QueryDSL 7.1 |
| DB (local/test) | H2 in-memory |
| DB (dev/prod) | MySQL (예정) |
| 인증 | JJWT 0.12.3 |
| API 문서 | SpringDoc OpenAPI 2.8.9 (Swagger UI) |
| 빌드 | Gradle 8.x |
| 테스트 | JUnit 5, Mockito, Awaitility, OkHttp3 MockWebServer |
| Util | Lombok, Jackson |

---

## 아키텍처

### 패키지 구조

DDD 기반 레이어드 아키텍처를 따른다.

``` text
src/main/java/org/smu/randsome/randsomeback/
├── domain/                     # 도메인별 패키지
│   └── {도메인}/
│       ├── entity/             # Aggregate Root, Entity, Value Object
│       ├── repository/         # Repository 인터페이스
│       ├── controller/         # Presentation — HTTP 요청/응답 변환, ApiResponse 조합
│       ├── service/            # Application Service — 비즈니스 흐름 조율
│       └── implement/          # 구현 계층 — 실제 로직 실행 (Repository 호출, 외부 API 등)
└── global/                     # 횡단 관심사
    ├── config/                 # JpaAuditing, QueryDSL, TimeZone 설정
    ├── entity/                 # BaseEntity, EntityStatus
    └── support/
        ├── error/              # CoreException, ErrorType, GlobalExceptionHandler
        └── response/           # ApiResponse<T>, ErrorMessage, ResultType
```

### 프로파일

| 프로파일 | 용도 | DB |
|---------|------|----|
| `local` (기본) | 로컬 개발 | H2, DDL auto-create |
| `test` | 테스트 | H2, Sentry 비활성화 |
| `dev` | 개발 서버 | (설정 예정) |
| `prod` | 운영 서버 | (설정 예정) |

---

## DDD 개발 원칙

### 계층 책임

**Controller (프레젠테이션 계층)**
- HTTP 요청을 받아 Service를 호출하고, 반환받은 결과를 `ApiResponse`로 조합해 응답한다.
- `ApiResponse` 조합은 오직 Controller에서만 한다. Service가 `ApiResponse`를 반환하거나 알아서는 안 된다.
- 비즈니스 로직을 포함하지 않는다.

**Service (애플리케이션 계층)**
- 비즈니스 흐름(유스케이스)을 읽기 쉽게 조율한다. "무엇을 하는가"를 표현한다.
- 트랜잭션 경계를 담당한다.
- 직접 구현하지 않고 `implement` 계층의 컴포넌트들을 호출한다.
- 반환 타입은 도메인 객체 또는 페이징 결과 DTO이며, `ApiResponse`를 반환하지 않는다.


**Implement (구현 계층)**
- Service에서 위임받은 실제 구현을 담당한다. "어떻게 하는가"를 표현한다.
- Repository 호출, 외부 API 연동, 복잡한 조회 조건 등 기술적 세부사항을 캡슐화한다.
- `MemberReader`, `MemberManager`, `MemberValidator` 등 역할별로 분리한다.

**Entity / Aggregate**
- 비즈니스 규칙과 불변식(invariant)을 스스로 보호한다. 상태 변경 로직은 엔티티 내부 메서드로 캡슐화한다.

**Repository**
- DB와 직접적인 접근을 담당한다

### 모든 도메인 엔티티는 `BaseEntity`를 상속한다

```java
// id, createdAt, updatedAt, status(ACTIVE/DELETED) 자동 제공
public class Member extends BaseEntity { ... }
```

### 소프트 삭제 (Soft Delete)

물리 삭제 대신 `entity.delete()` 를 호출한다. 조회 시 `status = ACTIVE` 조건을 명시적으로 포함한다.

---

## 켄트 벡 증강 코딩(Tidy First / TDD) 원칙

### TDD 사이클: Red → Green → Refactor

1. **Red**: 실패하는 테스트를 먼저 작성한다.
2. **Green**: 테스트를 통과하는 가장 단순한 코드를 작성한다.
3. **Refactor**: 중복을 제거하고, 의도를 드러내도록 개선한다. 테스트는 항상 통과 상태를 유지한다.

### 단순한 설계 4원칙 (Simple Design)

1. 모든 테스트를 통과한다.
2. 코드의 의도를 명확하게 드러낸다.
3. 중복을 제거한다.
4. 최소한의 요소만 사용한다 (YAGNI — You Aren't Gonna Need It).

### Tidy First: 구조 변경과 동작 변경을 분리한다

- 리팩터링(구조 변경)과 기능 추가(동작 변경)를 하나의 커밋에 섞지 않는다.
- 코드를 정리(tidy)할 때는 오직 구조만 변경하고, 동작은 유지한다.

### 점진적 변경

- 큰 변경을 한 번에 하지 않는다. 항상 작은 단계로 나누어 각 단계에서 테스트가 통과하는 상태를 유지한다.

---

## 테스트 작성 규칙

모든 테스트는 목적에 맞는 베이스 클래스를 상속한다.

| 베이스 클래스 | 용도 |
|-------------|------|
| `UnitTestSupport` | 단위 테스트 (`@ExtendWith(MockitoExtension.class)`) |
| `IntegrationTestSupport` | 통합 테스트 (`@SpringBootTest`) |
| `ControllerTestSupport` | 컨트롤러 테스트 (`@WebMvcTest` + MockMvc) |
| `TestDateTimeUtils` | 날짜/시간 관련 테스트 유틸 |

---

## API 응답 규칙

`ApiResponse<T>` 조합은 **Controller에서만** 한다. Service나 Implement 계층은 `ApiResponse`를 몰라야 한다.

```java
ApiResponse.success();         // 성공, 데이터 없음
ApiResponse.success(data);      // 성공, 데이터 포함
ApiResponse.error(ErrorType.X); // 실패 — GlobalExceptionHandler가 자동 처리
```

새로운 에러 타입은 `ErrorType` enum에 추가하며, 적절한 `HttpStatus`와 `LogLevel`을 지정한다. 예외는 `CoreException`으로 던지면 `GlobalExceptionHandler`가 자동으로 `ApiResponse.error()`로 변환한다.

---

## 주의 사항

- **Spring Security는 활성화** 상태다. 인증 없이 접근 가능한 경로: `/v1/auth/**`, `/v1/members/sign-up`, `/v1/feed`, Swagger UI(`/swagger/**`, `/v3/api-docs/**`), Actuator health/info. `/v1/admin/**`은 `ROLE_ADMIN` 전용, 나머지는 `ROLE_MEMBER` 또는 `ROLE_ADMIN` 필요.
- **MySQL 드라이버도 현재 비활성화** 상태다. `dev`/`prod` 프로파일 적용 전에 활성화하고 `application-dev.yml`, `application-prod.yml`을 작성한다.
- **`application-test.yml`에 테스트용 시크릿 키가 포함**되어 있다. 실제 운영 키를 절대 커밋하지 않는다.
- **QueryDSL Q클래스**는 `./gradlew compileJava` 후 `build/generated` 에 생성된다. IDE에서 해당 경로를 소스 루트로 등록해야 한다.
- **타임존은 Asia/Seoul** 로 고정된다 (`TimeZoneConfig`). 날짜/시간은 항상 `LocalDateTime`을 사용한다.
