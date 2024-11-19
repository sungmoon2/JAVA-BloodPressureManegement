package com.example.bpmanagement.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 설정
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        // 1. 정적 리소스 설정
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/assets/**"
                        ).permitAll()

                        // 2. 공개 페이지 설정
                        .requestMatchers(
                                "/",
                                "/members/login",
                                "/members/register",
                                "/login-process",
                                "/error"
                        ).permitAll()

                        // 3. 혈압 데이터 관련 설정
                        .requestMatchers(HttpMethod.PUT, "/bloodpressure/update").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/bloodpressure/delete/**").authenticated()
                        .requestMatchers("/bloodpressure/**").authenticated()

                        // 4. API 엔드포인트 설정
                        .requestMatchers("/api/bloodpressure/**").authenticated()

                        // 5. 차트 관련 설정
                        .requestMatchers("/chart/**").authenticated()

                        // 6. 마이페이지 관련 설정
                        .requestMatchers(
                                "/members/mypage/**",
                                "/members/update/**"
                        ).authenticated()

                        // 7. 나머지 모든 요청
                        .anyRequest().authenticated()
                )

                // 로그인 설정
                .formLogin(form -> form
                        .loginPage("/members/login")
                        .loginProcessingUrl("/login-process")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/members/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/members/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}