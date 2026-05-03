## 도메인

- **도메인 공통 속성 (`BaseEntity`)**
  - `id`: Long
  - `createdAt`: 등록 시각
  - `updatedAt`: 수정 시각
  - `deletedAt`: 소프트 삭제 시각
  - `status`: 엔티티 상태 (`ACTIVE`, `SUSPENDED`, `DELETED`)
- **행위**
  - `active()`: 상태를 `ACTIVE`로 변경
  - `isActive()`: `ACTIVE` 상태 여부 확인
  - `suspend()`: 상태를 `SUSPENDED`로 변경
  - `isSuspended()`: `SUSPENDED` 상태 여부 확인
  - `delete()`: 상태를 `DELETED`로 변경 + `deletedAt` 설정
  - `isDeleted()`: `DELETED` 상태 여부 확인

---

### 회원(`Member`)

- **속성**
  - `nickname`: VARCHAR(20, unique) - 랜덤 생성 (성별값#UUID8자리)
  - `legalName`: VARCHAR(50, not null) - 실명
  - `email`: VO(`Email`) - `address` (unique, `@sangmyung.kr` 형식 강제)
  - `password`: VO(`Password`) - `hashedValue` (BCrypt 해시)
  - `studentId`: VO(`StudentId`) - `number` (이메일에서 추출, 9자리 숫자, 연도 범위 검증)
  - `gender`: ENUM(`MALE`, `FEMALE`)
  - `role`: ENUM(`ROLE_MEMBER`, `ROLE_SUSPEND_MEMBER`, `ROLE_CANDIDATE`, `ROLE_ADMIN`)
  - `mbti`: ENUM(16가지 MBTI)
  - `department`: ENUM(62개 학과)
  - `socialProfile`: VO(`SocialProfile`) - `instagramId` (not null, unique), `selfIntroduction`(1000자), `idealDescription`(1000자)
  - `refreshToken`: VARCHAR
  - `version`: Long (낙관적 락)
- **행위**
  - `create()`: 회원 생성
  - `isPasswordCorrect()`: 비밀번호 검증
  - `isEmailCorrect()`: 이메일 일치 검증
  - `isAdmin()`: 관리자 여부 확인
  - `updateRole()`: 역할 변경
  - `updateRefreshToken()`: 리프레시 토큰 갱신
  - `revokeRefreshToken()`: 리프레시 토큰 폐기
  - `updateProfile()`: 프로필 수정 (실명, MBTI, 학과, 소셜 프로필)
  - `updatePassword()`: 비밀번호 변경
  - `withdraw()`: 회원 탈퇴 (`DELETED` 상태 전환 + `deletedAt` 설정 + 토큰 폐기)
  - `suspend()`: 회원 정지 (`SUSPENDED` 상태 전환 + `ROLE_SUSPEND_MEMBER`로 변경 + 토큰 폐기)
- **규칙**
  - `@sangmyung.kr` 형식이 아니면 등록 불가
  - 이메일 중복 불가 (`email` + `status` 유니크 제약)
  - 학번은 이메일 `@` 앞자리에서 추출하여 저장 (9자리 숫자, 2021년 ~ 현재 연도)
  - 닉네임은 랜덤 생성
  - 비밀번호는 BCrypt 해시 저장
  - 후보 승인 전 기본 역할은 `ROLE_MEMBER`
  - **인스타그램 아이디는 필수값 (not null)**

### 회원 프로필 태그(`MemberProfileTag`)

- **속성**
  - `member`: Member 참조 (1:1, unique)
  - `personalityTag`: ENUM(13종) - `ACTIVE`, `QUIET`, `AFFECTIONATE`, `INDEPENDENT`, `FUNNY`, `SERIOUS`, `OPTIMISTIC`, `CAREFUL`, `EMOTIONAL`, `RATIONAL`, `CONSIDERATE`, `TETO`, `EGEN`
  - `faceTypeTag`: ENUM(17종) - `PUPPY`, `CAT`, `BEAR`, `FOX`, `RABBIT`, `PURE`, `CHIC`, `WARM`, `DINOSAUR`, `HAMSTER`, `WOLF`, `CUTE`, `STRONG`, `PRINCE`, `DUBU`, `JOKER`, `SNAKE`
  - `datingStyleTag`: ENUM(13종) - `FREQUENT_CONTACT`, `MODERATE_CONTACT`, `PLANNED_DATE`, `SPONTANEOUS_DATE`, `RESPECTFUL_SPACE`, `EXPRESSIVE`, `GROW_TOGETHER`, `HOME_DATE`, `OUTDOOR_DATE`, `DEEP_TALK`, `FRIEND_LIKE`, `ROMANTIC`, `SLOW_STARTER`
- **행위**
  - `create()`: 프로필 태그 생성 (3가지 태그 모두 필수)
  - `updateTags()`: 태그 일괄 수정
- **규칙**
  - Member와 1:1 관계, 태그 변경이 Member 엔티티에 영향을 주지 않도록 분리
  - 세 가지 태그 모두 필수값

### 회원 기기(`MemberDevice`)

- **속성**
  - `member`: Member 참조 (N:1)
  - `deviceToken`: VARCHAR(512, not null)
  - `lastSyncedAt`: LocalDateTime (not null)
- **행위**
  - `register()`: 기기 등록
  - `updateLastSyncedAt()`: 동기화 시각 갱신
  - `reactivate()`: 기기 재활성화 (소프트 삭제 복구 + 동기화 시각 갱신)
- **규칙**
  - `(member_id, device_token)` 유니크 제약
  - 푸시 알림 발송 시 사용

### 회원 제재(`MemberRestriction`)

- **속성**
  - `member`: Member 참조 (N:1)
  - `reason`: VARCHAR (not null) - 제재 사유
- **행위**
  - `create()`: 제재 기록 생성
- **규칙**
  - 현재는 영구 정지만 지원 (시간 관련 필드 없음)

---

### 후보 등록 신청(`CandidateRegistration`)

- **속성**
  - `member`: Member 참조 (N:1)
  - `registrationStatus`: ENUM(`PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`, `CANCELED`)
  - `rejectedReason`: VARCHAR
  - `approvedAt`: DateTime
  - `rejectedAt`: DateTime
  - `withdrawnAt`: DateTime
  - `version`: Long (낙관적 락)
- **행위**
  - `apply()`: 후보 등록 신청 생성 (`PENDING`)
  - `approve(approvedAt)`: 관리자 승인
  - `reject(rejectedReason, rejectedAt)`: 관리자 거절
  - `withdraw(withdrawnAt)`: 승인된 후보 철회
  - `cancel()`: 대기 중인 신청 취소
  - `isApproved()`, `isPending()`: 상태 확인
- **규칙**
  - `Member` : `CandidateRegistration` = `1:N` (신청 이력 보관)
  - `approve()`는 이미 `APPROVED` 상태면 멱등하게 종료
  - `WITHDRAWN`, `REJECTED`, `CANCELED` 상태에서는 승인 불가
  - `APPROVED` 상태에서는 거절 불가
  - `withdraw()`는 `APPROVED` 상태에서만 가능
  - `cancel()`은 `PENDING` 상태에서만 가능

---

### 매칭 신청(`MatchingApplication`)

- **속성**
  - `member`: Member 참조 (N:1)
  - `matchingType`: ENUM(`RANDOM`, `IDEAL`)
  - `applicationCount`: INT (1~5)
  - `applicationStatus`: ENUM(`PENDING`, `SUCCESS`, `PARTIAL_MATCH`, `FAILED`)
  - `matchedCount`: INT - 실제 매칭된 수
  - `completedAt`: DateTime
  - `version`: Long (낙관적 락)
- **행위**
  - `apply(member, matchingType, applicationCount)`: 매칭 신청 생성 (`PENDING`)
  - `complete(completedAt, matchedCount)`: 매칭 완료 (결과에 따라 상태 자동 결정)
  - `getTargetGender()`: 신청자 반대 성별 반환
  - `ApplicationStatus.isCompleted()`: 완료 상태 여부 확인 (`SUCCESS`, `PARTIAL_MATCH`, `FAILED`이면 `true`)
- **규칙**
  - `applicationCount`는 1~5 범위
  - 상태 전이: `PENDING` → `SUCCESS` / `PARTIAL_MATCH` / `FAILED`
  - `matchedCount == applicationCount` → `SUCCESS`
  - `0 < matchedCount < applicationCount` → `PARTIAL_MATCH`
  - `matchedCount == 0` → `FAILED`
  - 이상형 매칭의 선호 태그는 `MatchingIdealTypeSnapshot`에 별도 스냅샷으로 저장
  - **결제 대신 티켓을 사용** (MatchingType에 따라 RANDOM / IDEAL 티켓 차감)

### 이상형 매칭 스냅샷(`MatchingIdealTypeSnapshot`)

- **속성**
  - `matchingApplicationId`: Long (unique) - MatchingApplication 참조 ID
  - `preferredPersonalityTags`: Set<PersonalityTag> (ElementCollection)
  - `preferredFaceTypeTags`: Set<FaceTypeTag> (ElementCollection)
  - `preferredDatingStyleTags`: Set<DatingStyleTag> (ElementCollection)
  - `preferredMbtis`: Set<Mbti> (ElementCollection)
- **행위**
  - `create(matchingApplicationId, idealTypePreference)`: 스냅샷 생성
  - `toVO()`: `IdealTypePreference` VO로 변환
- **규칙**
  - 이상형 매칭 신청 시 선택한 선호 태그를 독립적으로 기록
  - `matchingApplicationId`는 유니크 제약 (1:1 관계)
  - 매칭 신청 당시의 이상형 조건을 보존 (회원 프로필 변경에 무관)
  - 태그별 통계 쿼리(선호 태그 분포, 인기 태그 등)의 기반 테이블로 활용

### 이상형 선호 조건(`IdealTypePreference` VO)

- **속성**
  - `preferredPersonalityTags`: Set<PersonalityTag>
  - `preferredFaceTypeTags`: Set<FaceTypeTag>
  - `preferredDatingStyleTags`: Set<DatingStyleTag>
  - `preferredMbtis`: Set<Mbti>
- **행위**
  - `of(personalityTags, faceTypeTags, datingStyleTags, mbtis)`: VO 생성
  - `scoreAgainst(candidateProfileTag, candidateMbti)`: 이상형 조건 점수 계산 (0~4점)
- **규칙**
  - 각 카테고리에서 후보자 태그가 선호 목록에 포함되면 1점 부여
  - 빈 선호 목록은 해당 카테고리를 무시 (0점, 패널티 없음)
  - 최대 점수: 4점 (성격 + 얼굴상 + 연애스타일 + MBTI)
  - 불변 객체 (record)

### 매칭 결과(`MatchingResult`)

- **속성**
  - `matchingApplication`: `MatchingApplication` 참조 (N:1)
  - `candidate`: 매칭된 후보 `Member` 참조 (N:1)
- **행위**
  - `create(matchingApplication, candidate)`: 결과 생성
- **규칙**
  - 한 매칭 신청에 대해 `applicationCount`개의 결과가 생성됨
  - 후보자 수가 부족하면 가능한 수만큼만 생성 (PARTIAL_MATCH 또는 FAILED)
  - 후보자 탈퇴 시 매칭 결과 조회에서 개인정보 가림 처리

---

### 티켓(`Ticket`)

- **속성**
  - `member`: Member 참조 (N:1)
  - `ticketType`: ENUM(`RANDOM`, `IDEAL`)
  - `quantity`: VO(`TicketQuantity`) - 현재 잔액
  - `version`: Long (낙관적 락)
- **행위**
  - `create(member, ticketType, quantity)`: 티켓 생성
  - `earn(amount)`: 티켓 획득
  - `use(amount)`: 티켓 사용
  - `refund(amount)`: 티켓 환불
  - `getQuantityValue()`: 잔액 조회
- **규칙**
  - `(member_id, ticket_type)` 유니크 제약 - 회원당 타입별 1개
  - 회원 가입 시 기본 지급: RANDOM 3장, IDEAL 1장
  - 낙관적 락으로 동시성 제어

### 티켓 거래 기록(`TicketHistory`)

- **속성**
  - `member`: Member 참조 (N:1)
  - `ticketType`: ENUM(`RANDOM`, `IDEAL`)
  - `actionType`: ENUM(`USE`, `EARN`, `REFUND`)
  - `source`: ENUM(`JOIN`, `ATTENDANCE`, `COUPON`, `MATCHING`, `PARTIAL_MATCH_REFUND`, `NO_MATCH_REFUND`, `ADMIN`)
  - `amount`: INT
  - `description`: VARCHAR(100, not null) - source 기본 설명 자동 설정
- **행위**
  - `register()`: 거래 기록 생성
- **규칙**
  - 모든 티켓 변동에 대한 감사 추적(audit trail)
  - description이 비어있으면 source의 기본 설명 사용

---

### 쿠폰 이벤트(`CouponEvent`)

- **속성**
  - `name`: VARCHAR(100, not null)
  - `description`: VARCHAR(500)
  - `type`: ENUM(`HAPPY_HOUR`, `SECRET_CODE`)
  - `eventStatus`: ENUM(`DRAFT`, `ACTIVE`, `SOLD_OUT`, `ENDED`)
  - `totalQuantity`: INT (not null) - 총 발급 수량
  - `rewardTicketType`: ENUM(`RANDOM`, `IDEAL`) - 보상 티켓 종류
  - `rewardTicketAmount`: INT (not null) - 쿠폰당 지급 티켓 수
  - `startsAt`: DateTime (not null)
  - `expiresAt`: DateTime (not null) - 이벤트 만료 시각
  - `couponExpiresAt`: DateTime (not null) - 발급된 쿠폰 만료 시각
- **행위**
  - `create()`: 이벤트 생성 (`DRAFT`)
  - `update()`: 이벤트 수정 (`DRAFT` 상태에서만)
  - `activate(now)`: 활성화 (`DRAFT` -> `ACTIVE`)
  - `soldOut()`: 품절 처리 (`ACTIVE` -> `SOLD_OUT`)
  - `end()`: 종료 (`ACTIVE` -> `ENDED`)
  - `isIssuable(now)`: 쿠폰 발급 가능 여부 확인
- **규칙**
  - 상태 전이: `DRAFT` -> `ACTIVE` -> (`SOLD_OUT` | `ENDED`)
  - 시간 검증: `startsAt < expiresAt < couponExpiresAt`
  - 수량 검증: `totalQuantity > 0`, `rewardTicketAmount > 0`
  - `DRAFT` 상태에서만 수정 가능
  - 이미 만료된 이벤트는 활성화 불가

### 쿠폰(`Coupon`)

- **속성**
  - `couponEvent`: CouponEvent 참조 (N:1)
  - `member`: Member 참조 (N:1)
  - `couponStatus`: ENUM(`AVAILABLE`, `USED`, `EXPIRED`)
  - `version`: Long (낙관적 락)
  - `usedAt`: DateTime
  - `expiredAt`: DateTime - 쿠폰 만료 시각 (이벤트의 couponExpiresAt에서 복사)
- **행위**
  - `issue(couponEvent, member)`: 쿠폰 발급 (`AVAILABLE`)
  - `use(now)`: 쿠폰 사용 -> 티켓 지급
  - `expire()`: 쿠폰 만료 처리
  - `isAvailable(now)`: 사용 가능 여부 확인
  - `isOwnedBy(memberId)`: 소유자 확인
- **규칙**
  - `(coupon_event_id, member_id)` 유니크 제약 - 이벤트당 회원 1장
  - `AVAILABLE` 상태이고 만료 전인 경우에만 사용 가능
  - `expire()`는 이미 `USED`인 쿠폰에 대해 멱등하게 종료
  - 낙관적 락으로 동시성 제어

---

### 출석(`Attendance`)

- **속성**
  - `member`: Member 참조 (N:1)
  - `attendanceDate`: LocalDate (not null)
- **행위**
  - `create(member, attendanceDate)`: 출석 기록 생성
- **규칙**
  - `(member_id, attendance_date)` 유니크 제약 - 1일 1회
  - 출석 시 티켓 지급 (TicketSource.ATTENDANCE)

---

### 공지사항(`Announcement`)

- **속성**
  - `admin`: Member 참조 (N:1, not null) - 작성자 (관리자)
  - `title`: VARCHAR (not null)
  - `content`: TEXT (not null)
- **행위**
  - `register(admin, title, content)`: 공지사항 등록
- **규칙**
  - 관리자만 작성 가능
  - 소프트 삭제 지원

---

### 신고(`Report`)

- **속성**
  - `reporter`: Member 참조 (N:1) - 신고자
  - `reportedMember`: Member 참조 (N:1) - 피신고자
  - `targetType`: ENUM(`MATCHING_RESULT`)
  - `targetId`: Long - 신고 대상 ID
  - `reason`: ENUM(`INAPPROPRIATE_CONTENT`, `PLAGIARIZED_PROFILE`, `FAKE_PROFILE`, `HARASSMENT`, `SCAM`, `OTHER`)
  - `description`: VARCHAR(500) - 상세 설명 (선택)
  - `reportStatus`: ENUM(`PENDING`, `IN_REVIEW`, `RESOLVED`, `REJECTED`)
- **행위**
  - `create()`: 신고 생성 (`PENDING`)
  - `updateStatusToInReview()`: 검토 중으로 변경
  - `markAsResolved()`: 처리 완료
  - `markAsRejected()`: 신고 기각
  - `isPending()`: 대기 상태 확인
- **규칙**
  - `(reporter_id, target_type, target_id)` 유니크 제약 - 동일 대상 중복 신고 불가
  - 초기 상태는 `PENDING`

---

### 알림(`Notification`)

- **속성**
  - `memberId`: Long (not null) - FK 아닌 단순 ID
  - `type`: ENUM (not null)
    - `ANNOUNCEMENT_REGISTERED`: 공지사항 등록
    - `CANDIDATE_APPLIED_TO_ADMIN`: 후보자 신청 (관리자 대상)
    - `MATCHING_APPLIED_TO_ADMIN`: 매칭 신청 (관리자 대상)
    - `MATCHING_APPROVED` / `MATCHING_REJECTED`: 매칭 승인/거절
    - `CANDIDATE_APPROVED` / `CANDIDATE_REJECTED`: 후보자 승인/거절
  - `title`: VARCHAR (not null) - type에서 자동 파생
  - `body`: VARCHAR (not null) - type에서 자동 파생
- **행위**
  - `create(memberId, type)`: 알림 생성 (title/body 자동 설정)
- **규칙**
  - title과 body는 NotificationType enum에서 자동 결정
  - MemberDevice의 deviceToken을 통해 FCM 푸시 발송

---

### 매칭 피드 이벤트(`MatchingFeedEvent`)

- **속성**
  - `eventType`: ENUM(`CANDIDATE_REGISTERED`, `MATCH_REQUESTED`)
  - `nickname`: VARCHAR (이벤트 주체 닉네임)
  - `requestCount`: INT (매칭 신청 이벤트일 경우 신청 인원 수, 후보 등록 이벤트이면 NULL)
- **행위**
  - `recordMatchRequest(nickname, requestCount)`: 매칭 신청 피드 이벤트 기록
  - `recordCandidateRegister(nickname)`: 후보 등록 피드 이벤트 기록
- **규칙**
  - 관리자가 매칭 신청 또는 후보 등록을 승인할 때 자동 기록됨
  - 최신 피드는 최대 10건 조회 (`lastId` 없을 때)
  - 커서 기반 페이지네이션: `lastId` 이후의 피드 조회 가능
  - 비회원도 열람 가능 (`GET /v1/feed`)

---

### 약관(`Terms`)

- **속성**
  - `title`: VARCHAR (not null)
  - `content`: TEXT (not null)
  - `version`: VARCHAR (not null)
  - `required`: BOOLEAN (not null)
- **행위**
  - `register(title, content, version, required)`: 약관 등록
- **규칙**
  - 약관은 버전 단위로 관리
  - 필수 약관(`required=true`) 미동의 시 회원가입/핵심 기능 진행 불가

### 약관 동의(`TermsAgreement`)

- **속성**
  - `memberId`: Long
  - `termsId`: Long
  - `agreed`: BOOLEAN
  - `agreedAt`: DateTime
- **행위**
  - `agree(memberId, termsId)`: 동의 생성
- **규칙**
  - 회원-약관 버전 조합 단위로 동의 이력 관리
  - 필수 약관은 최신 버전 기준 동의 필요

---

### 통계(`Statistics`)

- **행위**
  - `getDashboard()`: 대시보드 통계 조회
- **응답 데이터**
  - `candidateCount`: 승인된 매칭 후보 수
  - `todayMatchingCount`: 오늘 생성된 매칭 신청 수
  - `totalMatchingCount`: 전체 매칭 신청 수
- **규칙**
  - 읽기 전용 조회 (상태 변경 없음)
  - `candidateCount`는 `registrationStatus = APPROVED`인 활성 후보만 집계
  - `todayMatchingCount`는 오늘 자정 ~ 익일 자정 범위의 활성 신청만 집계
  - 모든 집계는 `status = ACTIVE`인 데이터만 포함 (소프트 삭제 인식)
