package com.example.bloodpressure.Config;

import org.springframework.context.annotation.Bean;  // Spring Bean 객체를 생성하기 위한 애너테이션을 임포트
import org.springframework.context.annotation.Configuration;  // 해당 클래스가 설정 클래스임을 나타내는 애너테이션을 임포트
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // HTTP 보안 설정을 위한 클래스
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // Spring Security 활성화를 위한 애너테이션
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  // BCryptPasswordEncoder 클래스를 이용하여 비밀번호 암호화
import org.springframework.security.crypto.password.PasswordEncoder;  // 비밀번호 인코딩을 위한 인터페이스
import org.springframework.security.web.SecurityFilterChain;  // 보안 필터 체인을 설정하기 위한 클래스

@Configuration  // 이 클래스는 Spring의 설정 클래스임을 나타내는 애너테이션
@EnableWebSecurity  // Spring Security를 활성화하는 애너테이션. 이 애너테이션이 있어야 Spring Security의 기능을 사용할 수 있다.
public class SecurityConfig {

    @Bean  // 이 메서드는 Spring의 Bean으로 등록될 객체를 생성하는 메서드임을 나타낸다.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // HttpSecurity 객체를 통해 HTTP 요청에 대한 보안을 설정하는 메서드

        http
                .csrf(csrf -> csrf.disable())  // CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화
                // CSRF 보호는 공격자가 다른 사용자의 권한을 가장해 요청을 보내는 것을 방지하기 위한 기능
                // 이 기능을 비활성화하는 이유는 RESTful API나 외부에서의 요청이 필요할 수 있기 때문일 수 있다.

                .authorizeHttpRequests(auth -> auth
                        // 모든 요청에 대해 보안 설정을 추가한다.
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()  // CSS, JS, 이미지와 같은 정적 리소스에 대한 요청은 인증 없이 접근 허용
                        .requestMatchers(
                                "/",  // 루트 페이지는 인증 없이 접근 허용
                                "/members/login",  // 로그인 페이지는 인증 없이 접근 허용
                                "/members/register",  // 회원가입 페이지도 인증 없이 접근 허용
                                "/login-process",  // 로그인 처리 URL도 인증 없이 접근 허용
                                "/error"  // 오류 페이지도 인증 없이 접근 허용
                        ).permitAll()  // 위의 경로에 대해서는 모두 인증 없이 접근을 허용하도록 설정
                        .requestMatchers("/members/mypage/**").authenticated()  // "/members/mypage/**" URL 경로는 인증된 사용자만 접근 가능하도록 설정
                        .anyRequest().authenticated()  // 위에서 설정되지 않은 모든 다른 요청은 인증된 사용자만 접근 가능하도록 설정
                )
                .formLogin(form -> form
                        .loginPage("/members/login")  // 사용자 정의 로그인 페이지 URL을 지정
                        .loginProcessingUrl("/login-process")  // 로그인 처리를 위한 URL을 지정
                        .defaultSuccessUrl("/", true)  // 로그인 성공 시 이동할 기본 URL 설정 (두 번째 인자 true는 무조건적으로 이동하도록 설정)
                        .failureUrl("/members/login?error=true")  // 로그인 실패 시 이동할 URL을 설정 (에러 메시지를 쿼리 매개변수로 전달)
                        .usernameParameter("username")  // 로그인 폼에서 아이디를 받을 파라미터 이름을 "username"으로 설정
                        .passwordParameter("password")  // 로그인 폼에서 비밀번호를 받을 파라미터 이름을 "password"로 설정
                        .permitAll()  // 로그인 폼에 대해서는 인증 없이 누구나 접근 가능하도록 설정
                )
                .logout(logout -> logout
                        .logoutUrl("/members/logout")  // 로그아웃을 처리할 URL을 지정
                        .logoutSuccessUrl("/")  // 로그아웃 성공 시 이동할 URL을 지정
                        .invalidateHttpSession(true)  // 로그아웃 후 세션을 무효화 시킨다.
                        .deleteCookies("JSESSIONID")  // 로그아웃 후 "JSESSIONID" 쿠키를 삭제하여 사용자 세션을 종료
                        .permitAll()  // 로그아웃에 대해서도 누구나 접근할 수 있도록 설정
                );

        return http.build();  // 설정된 보안 필터 체인 객체를 반환
    }

    @Bean  // 이 메서드는 Spring에서 관리할 객체를 Bean으로 등록한다.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호 암호화를 위한 PasswordEncoder를 BCryptPasswordEncoder로 설정
        // BCryptPasswordEncoder는 비밀번호를 암호화하기 위한 알고리즘으로, 보안성이 높고 널리 사용된다.
    }
}
