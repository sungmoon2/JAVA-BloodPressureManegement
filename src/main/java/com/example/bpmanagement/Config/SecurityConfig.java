package com.example.bpmanagement.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // PasswordEncoder Bean 추가
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 활성화 (기본값)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")) // REST API 엔드포인트는 CSRF 검사 제외

                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/webjars/**", "/assets/**").permitAll()

                        // 공개 페이지 허용
                        .requestMatchers(
                                "/",
                                "/members/login",
                                "/members/register",
                                "/login-process",
                                "/error"
                        ).permitAll()

                        // API 엔드포인트 설정
                        .requestMatchers("/api/bloodpressure/**").authenticated()

                        // 혈압 관련 페이지 설정
                        .requestMatchers("/bloodpressure/**").authenticated()

                        // 차트 관련 설정
                        .requestMatchers("/chart/**").authenticated()

                        // 마이페이지 관련 설정
                        .requestMatchers("/members/mypage/**",
                                "/members/update/**").authenticated()

                        // 그 외 모든 요청은 인증 필요
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