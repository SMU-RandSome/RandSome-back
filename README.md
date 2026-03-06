# Randsome Backend

> 상명대학교 축제 이성 랜덤 매칭 서비스

---

## 서비스 소개

Randsome은 축제 기간 동안 이성 간 랜덤 매칭을 도와주는 온라인 서비스입니다.

기존 오프라인 매칭 부스의 **접근성 문제**와 **심리적 부담**을 해소하기 위해, 시간과 장소에 구애받지 않고 누구나 간편하게 참여할 수 있도록 기획되었습니다.

---

## 주요 기능

### 회원

| 기능 | 설명 |
|------|------|
| 회원가입 | 성별, MBTI, 소개글 입력 + 학생 이메일 인증 (닉네임 랜덤 생성) |
| 매칭 후보 등록 | 계좌 송금 → 관리자 승인 → 후보 등록 완료 |
| 매칭 후보 등록 취소 | 환불 불가 |
| 무작위 매칭 | 성별이 다른 후보자 중 랜덤 선택 |
| 이상형 기반 매칭 | 이상형 내용 기반 추천 알고리즘으로 선택 |
| 매칭 인원 선택 | 1 ~ 5명 (인원 수에 따라 결제 금액 상이) |
| 매칭 신청 내역 조회 | 승인 대기 / 승인 완료 섹션으로 구분 |
| 매칭 결과 열람 | 관리자 승인 완료 후에만 가능 |
| 최근 매칭 소식 조회 | 비회원도 열람 가능 |
| 회원 정보 수정 | 닉네임, 소개글, MBTI, 이상형 등 |

### 관리자

| 기능 | 설명 |
|------|------|
| 회원 관리 | 전체 회원 이력 열람 및 제한 |
| 매칭 후보 신청 관리 | 조회 / 승인 / 거절 |
| 랜덤 매칭 신청 관리 | 조회 / 승인 / 거절 |
| 결제 내역 관리 | 송금 확인 후 승인 처리 |

---

## 사용자 유형

- **비회원 (Guest)** : 메인 페이지 및 최근 매칭 소식 열람 가능, 회원가입 가능
- **회원 (Member)** : 매칭 후보 등록, 랜덤 매칭 신청, 매칭 결과 열람 등 전체 기능 사용
- **관리자 (Admin)** : 서비스 운영 전반 관리

> 상명대학교 재학생 및 휴학생(19학번 이상)만 이용 가능합니다.

---

## 결제 안내

결제는 **계좌 이체** 방식으로만 진행됩니다.

- 관리자가 직접 송금 내역을 확인 후 승인합니다.
- 정확한 금액을 **한 번에** 입금해야 합니다 (분할 송금 불가).
- 신청자 이름과 입금자명이 **동일**해야 합니다.
- 관리자 승인까지 최대 **약 10분** 소요됩니다.
- 잘못된 계좌 송금에 대한 책임은 본인에게 있습니다.
- **환불은 불가능**합니다.

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

## 실행 방법

```bash
# 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 전체 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build
```

---

## 패키지 구조

```
src/main/java/org/smu/randsome/randsomeback/
├── domain/                     # 도메인별 패키지
│   └── {도메인}/
│       ├── entity/             # Aggregate Root, Entity, Value Object
│       ├── repository/         # Repository 인터페이스
│       ├── controller/         # Presentation
│       ├── service/            # Application Service
│       └── implement/          # 구현 계층
└── global/                     # 횡단 관심사
    ├── config/
    ├── entity/
    └── support/
        ├── error/
        └── response/
```

---

## 프로파일

| 프로파일 | 용도 | DB |
|---------|------|----|
| `local` (기본) | 로컬 개발 | H2 |
| `test` | 테스트 | H2 |
| `dev` | 개발 서버 | MySQL (예정) |
| `prod` | 운영 서버 | MySQL (예정) |
