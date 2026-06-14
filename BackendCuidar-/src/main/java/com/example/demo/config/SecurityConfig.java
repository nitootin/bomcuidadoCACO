package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.demo.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/auth/login",
                                "/auth/idoso/login",
                                "/auth/verificar-2fa",
                                "/auth/reenviar-codigo",
                                "/swagger-ui/**",
                                "/auth/recuperar-senha",
                                "/auth/verificar-recuperacao",
                                "/auth/nova-senha",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/administrador/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR") //
                        .requestMatchers("/instituicao/**").hasAnyRole("ADMINISTRADOR", "INSTITUICAO")
                        .requestMatchers("/cuidador/**").hasAnyRole("CUIDADOR", "INSTITUICAO")
                        .requestMatchers("/idoso/**").hasAnyRole("CUIDADOR", "INSTITUICAO")
                        .requestMatchers("/remedio/**").hasRole("CUIDADOR")
                        .requestMatchers("/prescricao/**").hasRole("CUIDADOR")
                        .requestMatchers(HttpMethod.GET, "/alertas/me", "/alerta/me").hasRole("IDOSO")
                        .requestMatchers(HttpMethod.PUT, "/alertas/*/confirmar").hasRole("IDOSO")
                        .requestMatchers("/alerta/**").hasRole("CUIDADOR")
                        .requestMatchers("/alertas/**").hasRole("CUIDADOR")
                        .requestMatchers("/contato/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
