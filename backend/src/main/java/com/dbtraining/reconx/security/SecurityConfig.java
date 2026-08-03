package com.dbtraining.reconx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================================
 * SecurityConfig — TICKET-ADV073 + TICKET-ADV074
 * ============================================================================
 * WHAT:    Spring Security filter chain. Stateless JWT auth + method-level
 *          RBAC across ADMIN / TRADER / VIEWER / RECON_ANALYST roles.
 * HOW:     One SecurityFilterChain @Bean + PasswordEncoder @Bean +
 *          @EnableMethodSecurity. The JwtAuthenticationFilter is registered
 *          before UsernamePasswordAuthenticationFilter.
 * WHY:     Day 6 needs role-based protection on every endpoint, and the
 *          frontend uses bearer tokens issued at /auth/login.
 * OBSERVE: GET /api/v1/trades without a token -> 401.
 *
 * NOTE: `/api` context-path is set in application.yml, so paths here are
 *       relative to that (e.g. /v1/trades resolves to /api/v1/trades).
 * ============================================================================
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Without explicit handlers, a stateless JWT-only chain (no
                // formLogin/httpBasic) can send BOTH "no token" and "wrong role"
                // through the same default path. Wire them explicitly: missing/
                // invalid token -> 401 (ADV073), authenticated-but-wrong-role -> 403
                // (ADV074's RBAC matrix, e.g. VIEWER attempting a POST).
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new AccessDeniedHandlerImpl()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2/**",
                                // Spring Boot forwards to /error internally to render the
                                // error body after a filter chain denial. Without this, that
                                // forwarded request gets re-secured as anonymous and its
                                // (wrong) verdict overrides the original 403/401 the client
                                // should actually see.
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,    "/v1/trades/**").hasAnyRole("VIEWER", "TRADER", "RECON_ANALYST", "ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/v1/trades").hasAnyRole("TRADER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/v1/trades/**").hasAnyRole("TRADER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/v1/trades/**").hasAnyRole("TRADER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/trades/**").hasRole("ADMIN")
                        .requestMatchers("/v1/recon/**").hasAnyRole("RECON_ANALYST", "ADMIN")
                        .requestMatchers("/v1/audit/**").hasAnyRole("RECON_ANALYST", "ADMIN")
                        .anyRequest().authenticated()
                )
                .headers(h -> h.frameOptions(f -> f.disable())) // allow /h2 in dev
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
