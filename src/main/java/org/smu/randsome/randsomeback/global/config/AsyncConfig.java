package org.smu.randsome.randsomeback.global.config;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import org.smu.randsome.randsomeback.global.support.error.AsyncExceptionHandler;
import org.smu.randsome.randsomeback.global.support.logging.MdcTaskDecorator;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 작업 설정
 *
 * <p>비동기 작업 실행 시:
 * <ul>
 *   <li>MDC 컨텍스트를 전파하여 로그 추적 가능</li>
 *   <li>비동기 작업 중 발생한 예외를 커스텀 핸들러로 처리</li>
 * </ul>
 */
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    // NOTE: Bean으로 등록하면 spring이 afterPropertiesSet() → initialize()를 자동 호출하여 ThreadPoolTaskExecutor가 초기화됩니다.
    @Bean(name = APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("randsome-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 작업을 완료할 때까지 대기
        executor.setAwaitTerminationSeconds(30); // 종료 시 최대 대기 시간 설정
        executor.setRejectedExecutionHandler(new CallerRunsPolicy()); // 작업 거부 시 호출한 스레드에서 실행

        executor.setTaskDecorator(new MdcTaskDecorator()); // MDC 전파

        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

}