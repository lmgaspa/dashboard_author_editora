package com.dianaglobal.loginregisterdashboardeditora.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // stateless API (JWT, nada de sessão server-side)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CSRF desabilitado - usando apenas JWT para autenticação
                .csrf(AbstractHttpConfigurer::disable)

                // CORS usa o bean corsConfigurationSource() lá embaixo
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        // liberar todas as preflight OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // swagger / docs públicos (se você expõe isso)
                        .requestMatchers(
                                "/",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger",
                                "/api-docs",
                                "/api-docs/**"
                        ).permitAll()
                        
                        // --- ROTAS PÚBLICAS DA V1 ---
                        // login/logout (register agora é apenas ADMIN)
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/auth/forgot-password").permitAll()
                        .requestMatchers("/api/v1/auth/reset-password").permitAll()
                        
                        // REGISTRO - APENAS ADMIN (endpoint movido para /api/v1/admin/users)
                        .requestMatchers("/api/v1/auth/register").hasRole("ADMIN")

                        // esqueci-minha-senha e reset já estão dentro de /api/v1/auth/**,
                        // então já estão cobertas acima.

                        // Painel admin - apenas ADMIN (agora em v1)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        
                        // Painel user - apenas USER ou ADMIN (agora em v1)
                        .requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN")

                        // tudo o resto precisa de JWT válido
                        .anyRequest().authenticated()
                )

                // injeta seu filtro JWT pra validar Authorization: Bearer <token>
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // domínios que podem chamar sua API
        cfg.setAllowedOriginPatterns(List.of(
                "https://www.dashboard-author-editora.vercel.app",
                "https://dashboard-author-editora.vercel.app",
                "http://localhost:4200" // dev local Angular
        ));

        // métodos liberados
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));

        // headers que o front-end pode mandar
        cfg.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // permitir cookies (se necessário no futuro)
        cfg.setAllowCredentials(true);

        // headers expostos na resposta
        cfg.setExposedHeaders(List.of());

        // cache do preflight
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

//
