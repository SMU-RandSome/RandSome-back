package org.smu.randsome.randsomeback.global.support.error;

import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.logging.LogLevel;

/**
 * 비동기 작업 중 발생한 예외를 처리하는 핸들러
 *
 * <p>@Async 메서드에서 발생한 예외를 잡아서 로깅합니다.
 * AsyncConfig 에서 이 핸들러를 등록하여 사용합니다.
 */
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(@NonNull Throwable throwable, @NonNull Method method, Object @NonNull ... params) {
        if (throwable instanceof CoreException e) {
            String data = e.getData() != null ? e.getData().toString() : "null";

            switch (e.getErrorType().getLogLevel()) {
                case LogLevel.ERROR -> log.error("비동기 작업 중 CoreException 발생 - Method: {}, ErrorType: {}, Message: {}, Data: {}",
                        method.getName(), e.getErrorType().name(), e.getMessage(), data, e);
                case LogLevel.WARN ->  log.warn("비동기 작업 중 CoreException 발생 - Method: {}, ErrorType: {}, Message: {}, Data: {}",
                        method.getName(), e.getErrorType().name(), e.getMessage(), data, e);
                default ->             log.info("비동기 작업 중 CoreException 발생 - Method: {}, ErrorType: {}, Message: {}, Data: {}",
                        method.getName(), e.getErrorType().name(), e.getMessage(), data, e);
            }
        } else {
            log.error("비동기 작업 중 Exception 발생 - Method: {}, Args: {}, Error: {}",
                    method.getName(),
                    Arrays.toString(params),
                    throwable.getMessage(),
                    throwable
            );
        }
    }

}