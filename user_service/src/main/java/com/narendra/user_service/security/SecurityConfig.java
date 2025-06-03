package com.narendra.user_service.security;

import com.narendra.user_service.exception.BusinessValidationException;
import com.narendra.user_service.jwt.JwtAuthFilter;
import com.narendra.user_service.jwt.JwtAuthenticationEntryPoint;
import com.narendra.user_service.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws BusinessValidationException {
        try {
            http
                    .httpBasic(Customizer.withDefaults()) // Optional basic auth
                    .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for API simplicity
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/**").permitAll() // Allow unauthenticated access to auth APIs
                            .anyRequest().authenticated()               // All other requests require authentication
                    )
                    .exceptionHandling(exception ->
                            exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    )
                    .sessionManagement(session ->
                            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    );

            http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

            return http.build();

        } catch (Exception e) {
            log.error("Failed to configure SecurityFilterChain", e);
            throw new BusinessValidationException(e.getMessage());
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configure AuthenticationManager with your DaoAuthenticationProvider explicitly
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = authenticationProvider();
        return new ProviderManager(provider);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration conf = new CorsConfiguration().applyPermitDefaultValues();
        conf.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        conf.setAllowedOriginPatterns(Collections.singletonList("*"));
        conf.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", conf);
        return source;
    }
}
