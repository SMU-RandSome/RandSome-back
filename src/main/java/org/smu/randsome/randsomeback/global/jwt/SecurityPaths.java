package org.smu.randsome.randsomeback.global.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPaths {

    private static final String[] PERMIT_ALL = {
            "/v1/auth/login",
            "/v1/auth/reissue",
            "/v1/auth/email/**",
            "/v1/members/sign-up",
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

    public static String[] permitAll() {
        return PERMIT_ALL.clone();
    }

    public static String[] actuatorPermit() {
        return ACTUATOR_PERMIT.clone();
    }

    public static String[] admin() {
        return ADMIN.clone();
    }

}