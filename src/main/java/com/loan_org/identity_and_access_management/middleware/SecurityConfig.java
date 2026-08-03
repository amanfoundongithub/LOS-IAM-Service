package com.loan_org.identity_and_access_management.middleware;

import com.loan_org.identity_and_access_management.middleware.filters.AccountStatusFilter;
import com.loan_org.identity_and_access_management.middleware.filters.JwtAuthenticationFilter;
import com.loan_org.identity_and_access_management.middleware.filters.MdcHeaderFilter;
import com.loan_org.identity_and_access_management.middleware.filters.RateLimiterFilter;
import com.loan_org.identity_and_access_management.middleware.filters.RequestLoggingFilter;
import com.loan_org.identity_and_access_management.middleware.handler.JwtAccessDeniedHandler;
import com.loan_org.identity_and_access_management.middleware.handler.JwtAuthenticationEntryPoint;
import com.loan_org.identity_and_access_management.userEntity.entity.UserRole;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;

    private final MdcHeaderFilter mdcHeaderFilter;
    private final RateLimiterFilter rateLimiterFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final AccountStatusFilter accountStatusFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // Uncomment once implemented
    private final JwtAccessDeniedHandler accessDeniedHandler;

    private final String authBaseUrl;
    private final String adminBaseUrl;

    public SecurityConfig(
            CorsProperties corsProperties,
            MdcHeaderFilter mdcHeaderFilter,
            RateLimiterFilter rateLimiterFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RequestLoggingFilter requestLoggingFilter,
            AccountStatusFilter accountStatusFilter,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            @Value("${api.auth.base_url}") String authBaseUrl,
            @Value("${api.admin.base_url}") String adminBaseUrl) {

        this.corsProperties = corsProperties;
        this.mdcHeaderFilter = mdcHeaderFilter;
        this.rateLimiterFilter = rateLimiterFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.requestLoggingFilter = requestLoggingFilter;
        this.accountStatusFilter  = accountStatusFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.accessDeniedHandler = jwtAccessDeniedHandler;
        this.authBaseUrl = authBaseUrl;
        this.adminBaseUrl = adminBaseUrl;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @SuppressWarnings("null")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                authBaseUrl + "/register",
                                authBaseUrl + "/login",
                                authBaseUrl + "/refresh",
                                authBaseUrl + "/refresh/login",
                                authBaseUrl + "/verify",
                                authBaseUrl + "/reset-password-request",
                                authBaseUrl + "/allUserRoles"
                        ).permitAll()

                        .requestMatchers(
                                authBaseUrl + "/change-password"
                        ).authenticated()

                        .requestMatchers(
                                adminBaseUrl + "/**"
                        ).hasRole(UserRole.ADMIN.name())

                        .requestMatchers(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**"
).permitAll()

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        mdcHeaderFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterAfter(
                        rateLimiterFilter,
                        MdcHeaderFilter.class
                )

                .addFilterAfter(
                        jwtAuthenticationFilter,
                        RateLimiterFilter.class
                )

                .addFilterAfter(
                        accountStatusFilter,
                        JwtAuthenticationFilter.class
                )

                .addFilterAfter(
                        requestLoggingFilter,
                        AccountStatusFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}