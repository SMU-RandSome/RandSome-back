package org.smu.randsome.randsomeback.global.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    // COMMON
    BAD_REQUEST                 (HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", LogLevel.INFO),
    UNAUTHORIZED_ERROR          (HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", LogLevel.WARN),
    FORBIDDEN_ERROR             (HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", LogLevel.WARN),
    FORBIDDEN_MODIFY            (HttpStatus.FORBIDDEN, "해당 리소스를 수정할 권한이 없습니다.", LogLevel.WARN),
    FORBIDDEN_DELETE            (HttpStatus.FORBIDDEN, "해당 리소스를 삭제할 권한이 없습니다.", LogLevel.WARN),
    NOT_FOUND                   (HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", LogLevel.INFO),
    DUPLICATE                   (HttpStatus.CONFLICT, "이미 존재하는 리소스입니다.", LogLevel.INFO),
    TOO_MANY_MATCHING_REQUESTS  (HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 시도해주세요.", LogLevel.INFO),
    CONCURRENT_UPDATE_CONFLICT  (HttpStatus.CONFLICT, "요청하신 데이터가 이미 변경되었습니다. 새로고침 후 다시 시도해 주세요.", LogLevel.WARN),
    LOCK_ACQUISITION_TIMEOUT    (HttpStatus.CONFLICT, "요청이 충돌했습니다. 잠시 후 다시 시도해 주세요.", LogLevel.WARN),
    DEFAULT_ERROR               (HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),

    // AUTH
    FAILED_TO_AUTHENTICATE (HttpStatus.UNAUTHORIZED, "인증에 실패했습니다. 자격 증명을 확인해주세요.", LogLevel.WARN),

    // TERMS
    REQUIRED_TERMS_NOT_AGREED (HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해야 합니다.", LogLevel.INFO),

    // MEMBER
    INVALID_STUDENT_ID_FORMAT                (HttpStatus.BAD_REQUEST, "학번 형식이 올바르지 않습니다. (숫자 9자리)", LogLevel.INFO),
    INVALID_STUDENT_ID_YEAR                  (HttpStatus.BAD_REQUEST, "서비스를 이용하실 수 없는 학번입니다", LogLevel.INFO),
    INVALID_PASSWORD_UPDATE_REQUEST          (HttpStatus.BAD_REQUEST, "비밀번호 업데이트 요청이 유효하지 않습니다. 이메일 인증을 먼저 완료해주세요.", LogLevel.INFO),
    INVALID_ACCOUNT                          (HttpStatus.UNAUTHORIZED, "계정 정보가 일치하지 않습니다.", LogLevel.WARN),
    NOT_FOUND_MEMBER                         (HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.", LogLevel.INFO),
    NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN (HttpStatus.NOT_FOUND, "요청하신 Refresh Token 으로 활성화 된 회원을 찾을 수 없습니다.", LogLevel.INFO),
    DUPLICATE_EMAIL                          (HttpStatus.CONFLICT, "이미 사용 중인 Email 입니다.", LogLevel.INFO),

    // CANDIDATE
    NOT_ALLOW_WITHDRAW_NON_APPROVED         (HttpStatus.BAD_REQUEST, "승인된 후보자만 철회할 수 있습니다.", LogLevel.INFO),
    NOT_ALLOW_CANCEL_NON_PENDING            (HttpStatus.BAD_REQUEST, "승인 대기중인 후보자만 취소할 수 있습니다.", LogLevel.INFO),
    ALREADY_WITHDRAWN_CANDIDATE             (HttpStatus.BAD_REQUEST, "이미 철회된 후보자입니다.", LogLevel.INFO),
    NOT_ALLOW_ALREADY_APPROVED_REGISTRATION (HttpStatus.BAD_REQUEST, "이미 승인된 후보자 등록은 거절이 불가능합니다.", LogLevel.INFO),
    NOT_ALLOW_ALREADY_REJECTED_REGISTRATION (HttpStatus.BAD_REQUEST, "이미 거절된 후보자 신청은 승인이 불가능합니다.", LogLevel.INFO),
    NOT_ALLOW_ALREADY_CANCELED_REGISTRATION (HttpStatus.BAD_REQUEST, "이미 취소된 후보자 신청은 승인/거절이 불가능합니다.", LogLevel.INFO),
    NOT_FOUND_CANDIDATE                     (HttpStatus.NOT_FOUND, "후보자를 찾을 수 없습니다.", LogLevel.INFO),
    NOT_FOUND_CANDIDATE_REGISTRATION        (HttpStatus.NOT_FOUND, "후보자 신청 정보를 찾을 수 없습니다.", LogLevel.INFO),
    DUPLICATE_CANDIDATE                     (HttpStatus.CONFLICT, "이미 등록된 후보자 입니다.", LogLevel.INFO),
    ALREADY_PENDING_CANDIDATE               (HttpStatus.CONFLICT, "이미 후보자 등록 신청 중입니다.", LogLevel.INFO),

    // MATCH
    NOT_ALLOW_CANCEL_APPROVED           (HttpStatus.BAD_REQUEST, "승인된 매칭은 취소할 수 없습니다.", LogLevel.INFO),
    NOT_ALLOW_CANCEL_REJECTED           (HttpStatus.BAD_REQUEST, "거절된 매칭은 취소할 수 없습니다.", LogLevel.INFO),
    ALREADY_CANCELLED_MATCHING          (HttpStatus.BAD_REQUEST, "이미 취소된 매칭입니다.", LogLevel.INFO),
    NOT_ALLOW_ALREADY_APPROVED_MATCHING (HttpStatus.BAD_REQUEST, "이미 승인된 매칭은 거절이 불가능합니다.", LogLevel.INFO),
    NOT_FOUND_MATCHING                  (HttpStatus.NOT_FOUND, "매칭을 찾을 수 없습니다.", LogLevel.INFO),
    NOT_FOUND_MATCHING_RESULT           (HttpStatus.NOT_FOUND, "매칭 결과를 찾을 수 없습니다.", LogLevel.INFO),
    NOT_FOUND_APPROVED_MATCHING         (HttpStatus.NOT_FOUND, "승인된 매칭을 찾을 수 없습니다.", LogLevel.INFO),
    UNSUPPORTED_MATCHING_TYPE           (HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 매칭 타입입니다.", LogLevel.ERROR),
    IDEAL_MATCHING_NOT_IMPLEMENTED      (HttpStatus.INTERNAL_SERVER_ERROR, "이상형 매칭 기능은 아직 준비 중입니다.", LogLevel.WARN),
    FORBIDDEN_MATCHING_RESULT           (HttpStatus.FORBIDDEN, "다른 사용자의 매칭 결과에 접근할 수 없습니다.", LogLevel.WARN),

    // TICKET
    NOT_FOUND_TICKET      (HttpStatus.NOT_FOUND, "티켓을 찾을 수 없습니다.", LogLevel.INFO),
    NOT_ENOUGH_TICKETS    (HttpStatus.BAD_REQUEST, "티켓이 부족합니다.", LogLevel.INFO),
    INVALID_TICKET_AMOUNT (HttpStatus.BAD_REQUEST, "유효하지 않은 티켓 수량입니다. 0보다 큰 수량을 입력해주세요.", LogLevel.INFO),
    DUPLICATE_TICKET      (HttpStatus.CONFLICT, "해당 회원은 이미 티켓을 보유하고 있습니다.", LogLevel.INFO),

        // COUPON
    NOT_FOUND_COUPON_EVENT      (HttpStatus.NOT_FOUND,   "쿠폰 이벤트를 찾을 수 없습니다.", LogLevel.INFO),
    COUPON_EVENT_NOT_ACTIVE     (HttpStatus.BAD_REQUEST,  "현재 발급 가능한 이벤트가 아닙니다.", LogLevel.INFO),
    COUPON_EVENT_INVALID_STATUS  (HttpStatus.BAD_REQUEST,  "해당 상태의 이벤트에서는 이 작업을 수행할 수 없습니다.", LogLevel.WARN),
    COUPON_EVENT_ALREADY_EXPIRED (HttpStatus.BAD_REQUEST,  "이미 만료된 쿠폰 이벤트는 활성화할 수 없습니다.", LogLevel.INFO),
    ALREADY_ISSUED_COUPON       (HttpStatus.CONFLICT,     "이미 발급받은 쿠폰입니다.", LogLevel.INFO),
    COUPON_SOLD_OUT             (HttpStatus.CONFLICT,     "쿠폰이 모두 소진되었습니다.", LogLevel.INFO),
    NOT_FOUND_COUPON            (HttpStatus.NOT_FOUND,   "쿠폰을 찾을 수 없습니다.", LogLevel.INFO),
    COUPON_NOT_USABLE           (HttpStatus.BAD_REQUEST,  "사용할 수 없는 쿠폰입니다.", LogLevel.INFO),

    // ATTENDANCE
    DUPLICATE_ATTENDANCE(HttpStatus.CONFLICT, "오늘 이미 출석 체크를 완료했습니다.", LogLevel.INFO),

    // QR
    QR_TOKEN_EXPIRED      (HttpStatus.UNAUTHORIZED, "QR 코드가 만료되었습니다. 다시 생성해주세요.", LogLevel.INFO),
    QR_TOKEN_ALREADY_USED (HttpStatus.CONFLICT, "이미 사용된 QR 코드입니다.", LogLevel.INFO),
    INVALID_QR_TOKEN      (HttpStatus.BAD_REQUEST, "유효하지 않은 QR 코드입니다.", LogLevel.WARN),
    QR_GENERATION_FAILED  (HttpStatus.INTERNAL_SERVER_ERROR, "QR 코드 생성에 실패했습니다.", LogLevel.ERROR),

    // PAYMENT
    INVALID_PERSON_COUNT                (HttpStatus.BAD_REQUEST, "인원 수가 유효하지 않습니다.", LogLevel.INFO),
    NOT_ALLOW_ALREADY_CONFIRMED_PAYMENT (HttpStatus.BAD_REQUEST, "이미 확정된 결제는 거절이 불가능합니다.", LogLevel.INFO),
    NOT_ALLOW_CANCEL_CONFIRMED_PAYMENT  (HttpStatus.BAD_REQUEST, "이미 확정된 결제는 취소할 수 없습니다.", LogLevel.INFO),
    NOT_FOUND_PAYMENT                   (HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다.", LogLevel.INFO),

    //BANK_ACCOUNT
    NOT_FOUND_BANK_ACCOUNT      (HttpStatus.NOT_FOUND, "연결된 은행 계좌를 찾을 수 없습니다.", LogLevel.INFO),

    // REPORT
    NOT_FOUND_REPORT              (HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다.", LogLevel.INFO),
    CANNOT_REPORT_YOURSELF        (HttpStatus.BAD_REQUEST, "자신을 신고할 수 없습니다.", LogLevel.INFO),
    ALREADY_REPORTED_MEMBER       (HttpStatus.CONFLICT, "이미 신고한 사용자입니다.", LogLevel.INFO),
    REPORTED_MEMBER_SUSPENDED     (HttpStatus.FORBIDDEN, "신고가 누적된 사용자입니다. 서비스 이용이 제한되었습니다.", LogLevel.INFO),

    // AUTH
    INVALID_EMAIL_DOMAIN         (HttpStatus.BAD_REQUEST, "상명대학교 이메일(@sangmyung.kr)만 사용 가능합니다.", LogLevel.INFO),
    VERIFICATION_CODE_NOT_FOUND  (HttpStatus.BAD_REQUEST, "인증 코드를 먼저 요청해주세요.", LogLevel.INFO),
    VERIFICATION_CODE_EXPIRED    (HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다. 다시 요청해주세요.", LogLevel.INFO),
    VERIFICATION_CODE_MISMATCH   (HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다.", LogLevel.INFO),
    INVALID_SIGNUP_REQUEST       (HttpStatus.BAD_REQUEST, "회원가입 요청이 유효하지 않습니다. 이메일 인증을 먼저 완료해주세요.", LogLevel.INFO),
    INVALID_VERIFICATION_PURPOSE (HttpStatus.BAD_REQUEST, "잘못된 인증 토큰 요청입니다. 해당 인증 토큰은 이 작업에 사용할 수 없습니다.", LogLevel.INFO),
    EMAIL_SEND_FAILED            (HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),

    // FIREBASE
    INVALID_FCM_TOKEN       (HttpStatus.BAD_REQUEST, "유효하지 않은 FCM 토큰입니다.", LogLevel.INFO),
    NOT_FOUND_FCM_TOKEN     (HttpStatus.NOT_FOUND, "FCM 토큰을 찾을 수 없습니다.", LogLevel.INFO),
    FIREBASE_INIT_ERROR     (HttpStatus.INTERNAL_SERVER_ERROR, "Firebase 초기화에 실패했습니다.", LogLevel.ERROR),
    SEND_NOTIFICATION_ERROR (HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다.", LogLevel.ERROR),

    // JWT,
    EMPTY_TOKEN                        (HttpStatus.UNAUTHORIZED, "JWT 토큰이 존재하지 않습니다.", LogLevel.WARN),
    INVALID_TOKEN                      (HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.", LogLevel.WARN),
    TOKEN_THEFT_DETECTED               (HttpStatus.UNAUTHORIZED, "토큰 탈취가 감지되었습니다. 보안을 위해 재로그인이 필요합니다.", LogLevel.WARN),
    EMPTY_SECURITY_CONTEXT             (HttpStatus.UNAUTHORIZED, "Security Context 에 인증 정보가 없습니다.", LogLevel.WARN),
    NOT_FOUND_TOKEN                    (HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다.", LogLevel.INFO),
    CONCURRENT_REQUESTS_LIMIT_EXCEEDED (HttpStatus.TOO_MANY_REQUESTS, "동시에 여러 토큰 재발급 요청이 감지되었습니다. 잠시 후 다시 시도해주세요.", LogLevel.WARN),

    ;

    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;

}