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

    // MEMBER
    INVALID_ACCOUNT                          (HttpStatus.UNAUTHORIZED, "계정 정보가 일치하지 않습니다.", LogLevel.WARN),
    NOT_FOUND_MEMBER                         (HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.", LogLevel.INFO),
    NOT_FOUND_ACTIVE_MEMBER_BY_REFRESH_TOKEN (HttpStatus.NOT_FOUND, "요청하신 Refresh Token 으로 활성화 된 회원을 찾을 수 없습니다.", LogLevel.INFO),
    DUPLICATE_EMAIL                          (HttpStatus.CONFLICT, "이미 사용 중인 Email 입니다.", LogLevel.INFO),

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