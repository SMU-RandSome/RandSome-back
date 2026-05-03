package org.smu.randsome.randsomeback.global.config;

import java.time.LocalDateTime;
import org.jspecify.annotations.NonNull;
import org.smu.randsome.randsomeback.global.support.error.CoreException;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ServicePeriodInterceptor implements HandlerInterceptor {

    private static final String MATCHING_PATH_PREFIX = "/v1/matchings";

    private final LocalDateTime serviceOpenDateTime;
    private final LocalDateTime matchingOpenDateTime;
    private final LocalDateTime matchingCloseDateTime;

    public ServicePeriodInterceptor(
            @Value("${service.open-date-time}") String serviceOpenDateTime,
            @Value("${matching.open-date-time}") String matchingOpenDateTime,
            @Value("${matching.close-date-time}") String matchingCloseDateTime
    ) {
        this.serviceOpenDateTime = LocalDateTime.parse(serviceOpenDateTime);
        this.matchingOpenDateTime = LocalDateTime.parse(matchingOpenDateTime);
        this.matchingCloseDateTime = LocalDateTime.parse(matchingCloseDateTime);
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(serviceOpenDateTime)) {
            throw new CoreException(ErrorType.SERVICE_NOT_YET_OPEN);
        }

        if (isMatchingPath(request.getRequestURI())) {
            if (now.isBefore(matchingOpenDateTime)) {
                throw new CoreException(ErrorType.MATCHING_NOT_YET_OPEN);
            }
            if (!now.isBefore(matchingCloseDateTime)) {
                throw new CoreException(ErrorType.MATCHING_PERIOD_CLOSED);
            }
        }

        return true;
    }

    private boolean isMatchingPath(String uri) {
        return uri.startsWith(MATCHING_PATH_PREFIX);
    }

}