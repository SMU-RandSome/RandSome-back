package org.smu.randsome.randsomeback.global.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    // COMMON
    BAD_REQUEST        (HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", LogLevel.INFO),
    UNAUTHORIZED_ERROR (HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", LogLevel.WARN),
    FORBIDDEN_ERROR    (HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", LogLevel.WARN),
    FORBIDDEN_MODIFY   (HttpStatus.FORBIDDEN, "해당 리소스를 수정할 권한이 없습니다.", LogLevel.WARN),
    FORBIDDEN_DELETE   (HttpStatus.FORBIDDEN, "해당 리소스를 삭제할 권한이 없습니다.", LogLevel.WARN),
    NOT_FOUND          (HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", LogLevel.INFO),
    DUPLICATE          (HttpStatus.CONFLICT, "이미 존재하는 리소스입니다.", LogLevel.INFO),
    DEFAULT_ERROR      (HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),

    // TERMS
    REQUIRED_TERMS_NOT_AGREED (HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해야 합니다.", LogLevel.INFO),

    // MEMBER
    INVALID_STUDENT_ID_FORMAT                (HttpStatus.BAD_REQUEST, "학번 형식이 올바르지 않습니다. (숫자 9자리)", LogLevel.INFO),
    INVALID_STUDENT_ID_YEAR                  (HttpStatus.BAD_REQUEST, "서비스를 이용하실 수 없는 학번입니다", LogLevel.INFO),
    INVALID_ACCOUNT                          (HttpStatus.UNAUTHORIZED, "계정 정보가 일치하지 않습니다.", LogLevel.WARN),
    NOT_FOUND_MEMBER                         (HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.", LogLevel.INFO),
    NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN (HttpStatus.NOT_FOUND, "요청하신 Refresh Token 으로 활성화 된 회원을 찾을 수 없습니다.", LogLevel.INFO),
    DUPLICATE_EMAIL                          (HttpStatus.CONFLICT, "이미 사용 중인 Email 입니다.", LogLevel.INFO),

    // CANDIDATE
    NOT_FOUND_CANDIDATE                  (HttpStatus.NOT_FOUND, "후보자를 찾을 수 없습니다.", LogLevel.INFO),
    DUPLICATE_CANDIDATE                  (HttpStatus.CONFLICT, "이미 등록된 후보자 입니다.", LogLevel.INFO),
    NOT_ALLOW_WITHDRAW_NON_APPROVED      (HttpStatus.BAD_REQUEST, "승인된 후보자만 철회할 수 있습니다.", LogLevel.INFO),

    // PAYMENT
    INVALID_PERSON_COUNT                (HttpStatus.BAD_REQUEST, "인원 수가 유효하지 않습니다.", LogLevel.INFO),
    NOT_ALLOW_ALREADY_CONFIRMED_PAYMENT (HttpStatus.BAD_REQUEST, "이미 확정된 결제는 거절이 불가능합니다.", LogLevel.INFO),
    NOT_ALLOW_ALREADY_APPROVED_REGISTRATION  (HttpStatus.BAD_REQUEST, "이미 승인된 후보자 등록은 거절이 불가능합니다.", LogLevel.INFO),
    NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다.", LogLevel.INFO),

    // AUTH
    INVALID_EMAIL_DOMAIN        (HttpStatus.BAD_REQUEST, "상명대학교 이메일(@sangmyung.kr)만 사용 가능합니다.", LogLevel.INFO),
    VERIFICATION_CODE_NOT_FOUND (HttpStatus.BAD_REQUEST, "인증 코드를 먼저 요청해주세요.", LogLevel.INFO),
    VERIFICATION_CODE_EXPIRED   (HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다. 다시 요청해주세요.", LogLevel.INFO),
    VERIFICATION_CODE_MISMATCH  (HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다.", LogLevel.INFO),
    INVALID_SIGNUP_REQUEST      (HttpStatus.BAD_REQUEST, "회원가입 요청이 유효하지 않습니다. 이메일 인증을 먼저 완료해주세요.", LogLevel.INFO),
    EMAIL_SEND_FAILED           (HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요.", LogLevel.ERROR),

    // JWT
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