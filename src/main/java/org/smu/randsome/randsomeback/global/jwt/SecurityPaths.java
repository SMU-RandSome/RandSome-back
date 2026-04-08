package org.smu.randsome.randsomeback.global.jwt;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPaths {

    private static final String[] PERMIT_ALL = {
            "/v1/auth/login",
            "/v1/auth/reissue",
            "/v1/auth/email/**",
            "/v1/members/sign-up",
            "/v1/members/password",
            "/v1/statistics/dashboard",
            "/swagger/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    private static final String[] ACTUATOR_PERMIT = {
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus"
    };

    private static final String[] ADMIN = {
            "/v1/admin/**",
    };

    private static final List<MethodPermitPath> METHOD_PERMIT_ALL = List.of(
            new MethodPermitPath(HttpMethod.GET, "/v1/feed")
    );

    public static String[] permitAll() {
        return PERMIT_ALL.clone();
    }

    public static String[] actuatorPermit() {
        return ACTUATOR_PERMIT.clone();
    }

    public static String[] admin() {
        return ADMIN.clone();
    }

    public static List<MethodPermitPath> methodPermitAll() {
        return METHOD_PERMIT_ALL;
    }

    public record MethodPermitPath(HttpMethod method, String pathPattern) {
    }

}