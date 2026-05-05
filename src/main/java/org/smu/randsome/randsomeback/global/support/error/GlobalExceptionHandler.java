package org.smu.randsome.randsomeback.global.support.error;

import jakarta.persistence.LockTimeoutException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smu.randsome.randsomeback.global.support.notification.ErrorNotificationSender;
import org.smu.randsome.randsomeback.global.support.response.ApiResponse;
import org.springframework.boot.logging.LogLevel;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorNotificationSender errorNotificationSender;

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CoreException e) {
        ErrorType errorType = e.getErrorType();
        Object data = e.getData();

        String logMessage = String.format("[%s] %s (Data: %s)",
                errorType.name(),
                e.getMessage(),
                data != null ? data.toString() : "null"
        );

        switch (errorType.getLogLevel()) {
            case LogLevel.ERROR -> {
                log.error(logMessage, e);
                errorNotificationSender.sendErrorNotification(logMessage, e);
            }
            case LogLevel.WARN ->  log.warn(logMessage, e);
            default ->             log.info(logMessage, e);
        }

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, data));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("[Exception]: {}", e.getMessage(), e);
        errorNotificationSender.sendErrorNotification("[Exception]: " + e.getMessage(), e);

        ErrorType errorType = ErrorType.DEFAULT_ERROR;
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;

        Map<String, String> validationData = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + ", " + replacement
                ));

        log.warn("[ConstraintViolationException] @RequestParam 유효성 검사 실패. (ValidationData={})", validationData, e);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, validationData));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;

        Map<String, String> validationData = e.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효성 검사 실패",
                        (existing, replacement) -> existing + ", " + replacement
                ));

        log.warn("[MethodArgumentNotValidException] @Valid 실패. (ValidationData={})", validationData, e);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, validationData));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;
        String paramName = e.getParameter().getParameterName() != null
                ? e.getParameter().getParameterName()
                : "unknown";
        String paramType = e.getParameter().getParameterType().getSimpleName();
        String detailMessage = e.getMessage();
        String message = "[" + paramName + "] 파라미터는 " + paramType + " 타입이어야 합니다. 상세: " + detailMessage;

        log.warn("[MethodArgumentTypeMismatchException]: {}", message);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;
        String paramName = e.getParameterName();
        String paramType = e.getParameterType();
        String message = paramType + " 타입의" + " [ " + paramName + " ] " + "파라미터가 누락되었습니다.";

        log.warn("[MissingServletRequestParameterException]: {}", message);

        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType, message));
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiResponse<?>> handleOptimisticLock(Exception e) {
        log.warn("[OptimisticLock] 동시 처리 충돌 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorType.CONCURRENT_UPDATE_CONFLICT));
    }

    @ExceptionHandler({PessimisticLockingFailureException.class, LockTimeoutException.class})
    public ResponseEntity<ApiResponse<?>> handlePessimisticLock(Exception e) {
        log.warn("[PessimisticLock] 락 획득 실패 (타임아웃): {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorType.LOCK_ACQUISITION_TIMEOUT));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorType errorType = ErrorType.BAD_REQUEST;
        log.warn("[HttpMessageNotReadableException] 요청 본문을 파싱할 수 없습니다: {}", e.getMessage());
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        ErrorType errorType = ErrorType.METHOD_NOT_ALLOWED;
        log.warn("[HttpRequestMethodNotSupportedException] 지원하지 않는 메서드: {}", e.getMethod());
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFoundException(Exception e) {
        ErrorType errorType = ErrorType.NOT_FOUND;
        log.warn("[{}] 존재하지 않는 엔드포인트: {}", e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity
                .status(errorType.getStatus())
                .body(ApiResponse.error(errorType));
    }

}