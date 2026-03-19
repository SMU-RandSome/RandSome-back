package org.smu.randsome.randsomeback.security;

import lombok.RequiredArgsConstructor;
import org.smu.randsome.randsomeback.global.jwt.SecurityPaths;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(SecurityPaths.actuatorPermit()).permitAll();
                    auth.requestMatchers("/actuator/**").denyAll();
                    auth.requestMatchers(SecurityPaths.permitAll()).permitAll();
                    SecurityPaths.methodPermitAll().forEach(endpoint ->
                            auth.requestMatchers(endpoint.method(), endpoint.pathPattern()).permitAll());
                    auth.requestMatchers(SecurityPaths.admin()).hasRole("ADMIN");
                    auth.anyRequest().hasAnyRole("MEMBER", "CANDIDATE", "ADMIN");
                });

        return http.build();
    }

}