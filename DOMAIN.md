## 도메인

- **도메인 공통 속성 (`BaseEntity`)**
  - `id`: Long
  - `createdAt`: 등록 시각
  - `updatedAt`: 수정 시각
  - `status`: 엔티티 상태 (`ACTIVE`, `DELETED`)

### 회원(`Member`)

- **속성**
  - `nickname`: VARCHAR (unique)
  - `legalName`: VARCHAR (실명)
  - `email`: VO(`Email`) - `address` (unique, `@sangmyung.kr` 형식 강제)
  - `password`: VO(`Password`) - `hashedValue` (BCrypt 해시)
  - `studentId`: VO(`StudentId`) - `number` (이메일에서 추출, 9자리 숫자, 연도 범위 검증)
  - `gender`: ENUM(`MALE`, `FEMALE`)
  - `role`: ENUM(`ROLE_MEMBER`, `ROLE_CANDIDATE`, `ROLE_ADMIN`)
  - `mbti`: ENUM(16가지 MBTI)
  - `socialProfile`: VO(`SocialProfile`) - `instagramId` (unique), `selfIntroduction`, `idealDescription`
  - `refreshToken`: VARCHAR
- **행위**
  - `create()`: 회원 생성
  - `isPasswordCorrect()`: 비밀번호 검증
  - `updateRole()`: 역할 변경
  - `updateRefreshToken()`: 리프레시 토큰 갱신
  - `revokeRefreshToken()`: 리프레시 토큰 폐기
  - `updateSocialProfile()`: 소셜 프로필 수정
  - `updateMbti()`: MBTI 수정
- **규칙**
  - `@sangmyung.kr` 형식이 아니면 등록 불가
  - 이메일 중복 불가
  - 학번은 이메일 `@` 앞자리에서 추출하여 저장 (9자리 숫자, 2021년 ~ 현재 연도)
  - 닉네임은 랜덤 생성
  - 비밀번호는 BCrypt 해시 저장
  - 후보 승인 전 기본 역할은 `ROLE_MEMBER`

### 후보 등록 신청(`CandidateRegistration`)

- **속성**
  - `memberId`: Long (회원 참조)
  - `registrationStatus`: ENUM(`PENDING`, `APPROVED`, `REJECTED`)
  - `rejectedReason`: VARCHAR
  - `approvedAt`: DateTime
- **행위**
  - `apply()`: 후보 등록 신청 생성
  - `approve()`: 관리자 승인
  - `reject()`: 관리자 거절
- **규칙**
  - `Member` : `CandidateRegistration` = `1:N` (신청 이력 보관)
  - 한 회원은 동시에 `PENDING` 상태 신청 1건만 가능
  - `approve`/`reject`는 `PENDING` 상태에서만 가능
  - 관리자가 승인하면 회원 역할을 `ROLE_CANDIDATE`로 변경

### 매칭 신청(`MatchingRequest`)

- **속성**
  - `memberId`: Long
  - `type`: ENUM(`RANDOM`, `IDEAL`)
  - `requestCount`: INT (1~5)
  - `totalPrice`: BIG_DECIMAL
  - `rejectedReason`: VARCHAR
  - `applicationStatus`: ENUM(`PENDING`, `APPROVED`, `REJECTED`)
- **행위**
  - `apply()`: 매칭 신청
  - `cancel()`: 신청 취소
  - `approve()`: 관리자 승인 후 매칭 실행
  - `reject()`: 관리자 거절
- **규칙**
  - 관리자가 최종 승인 후 매칭 실행
  - `requestCount`에 따라 가격 차등
  - `RANDOM`: 인당 1000원
  - `IDEAL`: 인당 1500원

### 매칭 결과(`MatchingResult`)

- **속성**
  - `matchingRequestId`: 매칭 신청 ID
  - `candidateMemberId`: 매칭된 후보 회원 ID
- **행위**
  - `create()`: 결과 생성
- **규칙**
  - 열람은 결제 확인 + 관리자 승인 이후 가능

### 후보자 등록 결제(`CandidatePayment`)

- **속성**
  - `memberId`: Long (`Member` 참조)
  - `candidateRegistrationId`: Long (`CandidateRegistration` 참조)
  - `paymentType`: ENUM(`CANDIDATE_REGISTRATION`)
  - `amount`: BIG_DECIMAL
  - `paymentStatus`: ENUM(`PENDING`, `APPROVED`, `REJECTED`)
  - `rejectedReason`: VARCHAR
- **행위**
  - `register(member, candidateRegistration)`: 후보자 등록 결제 생성
  - `approve()`: 결제 승인
  - `reject(rejectedReason)`: 결제 거절
- **규칙**
  - 후보자 등록 신청 건당 결제 1건을 생성
  - 결제 금액은 `PaymentType.CANDIDATE_REGISTRATION` 정책(2000원)으로 계산
  - 결제는 `PENDING`으로 시작하며 관리자 승인/거절로 상태 전이

### 매칭 결제(`MatchingPayment`)

- **속성**
  - `memberId`: Long (`Member` 참조)
  - `matchingRequestId`: Long (`MatchingRequest` 참조)
  - `paymentType`: ENUM(`RANDOM_MATCHING`, `IDEAL_TYPE_MATCHING`)
  - `amount`: BIG_DECIMAL
  - `paymentStatus`: ENUM(`PENDING`, `APPROVED`, `REJECTED`)
  - `rejectedReason`: VARCHAR
- **행위**
  - `register(member, matchingRequest, paymentType, personCount)`: 매칭 결제 생성
  - `approve()`: 결제 승인
  - `reject(rejectedReason)`: 결제 거절
- **규칙**
  - `paymentType`은 매칭 결제 타입(`RANDOM_MATCHING`, `IDEAL_TYPE_MATCHING`)만 허용
  - 결제 금액은 `PaymentType.calculateFee(personCount)` 정책으로 계산
  - 결제는 `PENDING`으로 시작하며 관리자 승인/거절로 상태 전이

### 약관(`Terms`)

- **속성**
  - `title`: VARCHAR
  - `content`: TEXT
  - `version`: VARCHAR
  - `required`: BOOLEAN
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

### 계좌(`BankAccount`)

- **속성**
  - `memberId`: Long
  - `bankName`: VARCHAR
  - `accountNumber`: VARCHAR
  - `accountHolder`: VARCHAR
- **행위**
  - `register(memberId, bankName, accountNumber, accountHolder)`: 계좌 등록
- **규칙**
  - 회원 본인 계좌만 등록 가능
  - 동일 회원의 중복 계좌 정책(허용/미허용)은 서비스 정책으로 관리

---