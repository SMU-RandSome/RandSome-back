package org.smu.randsome.randsomeback.global.support.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP 요청/응답을 로깅하는 필터
 * - MdcFilter 다음 순서로 실행되어 traceId가 MDC에 있는 상태에서 로그 출력
 * - Body 미포함 (개인정보 노출 위험, 메모리 비용)
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[HTTP] {} {} → {} ({}ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    elapsed);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs");
    }
}