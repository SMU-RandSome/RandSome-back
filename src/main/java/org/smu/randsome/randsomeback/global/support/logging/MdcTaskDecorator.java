package org.smu.randsome.randsomeback.global.support.logging;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * 비동기 작업 실행 시 MDC 컨텍스트를 전파하는 TaskDecorator
 *
 * <p>ThreadLocal 기반의 MDC는 스레드가 바뀌면 컨텍스트가 전파되지 않으므로,
 * 비동기 작업(@Async) 실행 전에 부모 스레드의 MDC를 복사하여 자식 스레드로 전달합니다.
 *
 * <p>사용 예시:
 * <pre>
 * &#64;Bean
 * public Executor asyncExecutor() {
 *     ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *     executor.setTaskDecorator(new MdcTaskDecorator());
 *     return executor;
 * }
 * </pre>
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 현재 스레드(부모)의 MDC 컨텍스트 복사
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
                // 자식 스레드에 MDC 컨텍스트 설정
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                // 원래 작업 실행
                runnable.run();
            } finally {
                if (previousContext != null) {
                    MDC.setContextMap(previousContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }

}