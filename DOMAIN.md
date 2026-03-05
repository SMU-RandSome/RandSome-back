## 도메인

- **도메인 기본 속성**
  - `id`: Long
  - `created_at`:  등록 시각
  - `updated_at`:  수정 시각
  - `status`:  회원 상태 (ACTIVE, DELETED)

### **회원(Member)**

- 속성
  - `nickname` : VARCHAR (unique)
  - `legalName`: VARCHAR (실명)
  - `email` : VO(`Email`) — `address` (unique, `@sangmyung.kr` 형식 강제)
  - `password` : VO(`Password`) — `hashedValue` (BCrypt 해시)
  - `student_id` : VO(`StudentId`) — `number` (이메일에서 추출, 9자리 숫자, 연도 범위 검증)
  - `gender`: ENUM(`MALE`, `FEMALE`)
  - `role`: ENUM(`ROLE_MEMBER`, `ROLE_CANDIDATE`, `ROLE_ADMIN`)
  - `mbti`: ENUM(16가지 MBTI)
  - `socialProfile` : VO(`SocialProfile`) — `instagramId` (unique), `selfIntroduction`, `idealDescription`
  - `refreshToken`: VARCHAR
- 행위
  - `create()`: 회원 생성
  - `isPasswordCorrect()` : 비밀번호 검증
  - `updateRole()` : 역할 변경
  - `updateRefreshToken()` : 리프레시 토큰 갱신
  - `revokeRefreshToken()` : 리프레시 토큰 폐기
  - `updateSocialProfile()` : 소셜 프로필 수정
  - `updateMbti()` : MBTI 수정

- 규칙
  - `@sangmyung.kr` 형식이 아니면 등록 불가이다.
  - 이메일은 중복 불가이다.
  - 학번은 이메일 `@` 앞자리에서 추출하여 저장한다. (9자리 숫자, 2021년 ~ 현재 연도)
  - 닉네임은 랜덤 생성 된다.
  - 비밀번호는 BCrypt로 해시화 되어야 한다.
  - 관리자가 승인 해야지만 후보자(`ROLE_CANDIDATE`)로 역할 변경 된다.

### 후보 등록 신청(**CandidateRegistration)**

- **속성**
  - `member_id` : Long (회원 참조)
  - `registration_status`: ENUM(`PENDING`, `APPROVED`, `REJECTED`)
  - `rejected_reason` : VARCHAR
  - `approved_at` : DateTime
- **행위**
  - `apply()`: 매칭 후보 등록 신청
  - `approve()`: 관리자가 후보 승인
  - `reject()`: 관리자가 후보 거절
- **규칙**
  - 관리자가 승인해야만 `CANDIDATE` 역할로 변경됨
  - 송금 내역을 확인될 경우 관리자가 승인한다.


### **매칭 신청(MatchingRequest)**

- **속성**
  - `member_id`: Long
  - `type`: ENUM(`RANDOM`, `IDEAL`)
  - `requestCount`: INT (1~5)
  - `total_price`: BIG_DECIMAL
  - `rejected_reason` : String
  - `application_status`: ENUM(`PENDING`, `APPROVED`, `REJECTED`)
- **행위**
  - `apply()`: 매칭 신청
  - `cancel()`: 신청 취소
  - `approve()`: 관리자 승인 → 매칭 실행
  - `reject()`: 관리자 거절
- **규칙**
  - 신청 시 `Payment` 생성 → 결제 `CONFIRMED` 되어야 `PENDING` 상태로 이동
  - 관리자가 최종 승인 후 매칭 실행
  - `requestCount`에 따라 가격 차등 적용
  - `RANDOM` : 인당 1000원
  - `IDEAL` : 인당 1500원

### **매칭 결과**(**MatchingResult**)

- **속성**
  - `matching_request_id` : 매칭 신청 Id
  - `candidate_member_id`: Long (매칭된 회원 id)
- 행위
  - `create()` : 생성
- **규칙**
  - 열람 권한은 결제 + 관리자 승인 후 가능

### **결제**(Payment)

- **속성**
  - `member_id`: Long
  - `amount`: INT
  - `ref_id`: Long
  - `type` : ENUM(`CANDIDATE`, `MATCHING`)
  - `status`: ENUM(`WAITING`, `CONFIRMED`, `FAILED`)
- **행위**
  - `create()`: 결제 요청 생성
  - `confirmPayment()`: 관리자가 결제 확인
  - `failPayment()`: 결제 실패 처리
- **규칙**
  - 결제가 `CONFIRMED` 되어야 매칭 진행 가능
  - ref_id와 type 조합은 유니크 해야한다.