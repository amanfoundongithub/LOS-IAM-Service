package com.loan_org.identity_and_access_management.middleware.config;

import com.loan_org.identity_and_access_management.middleware.filter.JwtAuthenticationFilter;
import com.loan_org.identity_and_access_management.middleware.filter.MdcHeaderFilter;
import com.loan_org.identity_and_access_management.middleware.filter.RateLimiterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CorsProperties          corsProperties;
    private final RateLimiterFilter       rateLimiterFilter;
    private final MdcHeaderFilter         mdcHeaderFilter;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${api.base_url}")
    private String baseUrl;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(baseUrl + "/register").permitAll()
                        .requestMatchers(baseUrl + "/login").permitAll()
                        .requestMatchers(baseUrl + "/refresh").permitAll()
                        .requestMatchers(baseUrl + "/verify").permitAll()
                        .requestMatchers(baseUrl + "/reset-password-request").permitAll()
                        .requestMatchers(baseUrl + "/change-password").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Add filter ordering here
        http.addFilterBefore(mdcHeaderFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimiterFilter, MdcHeaderFilter.class);

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