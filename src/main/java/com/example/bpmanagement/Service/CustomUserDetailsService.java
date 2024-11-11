package com.example.bpmanagement.Service;  // 해당 클래스가 위치할 패키지 선언

// 필요한 클래스들을 임포트
import com.example.bpmanagement.Entity.Member;  // Member 엔티티
import com.example.bpmanagement.Repository.MemberRepository;  // 회원 정보 접근을 위한 레포지토리
import lombok.RequiredArgsConstructor;  // final 필드에 대한 생성자 자동 생성
import org.springframework.security.core.userdetails.User;  // Spring Security의 User 클래스
import org.springframework.security.core.userdetails.UserDetails;  // 사용자 상세 정보 인터페이스
import org.springframework.security.core.userdetails.UserDetailsService;  // Spring Security의 사용자 정보 로드 인터페이스
import org.springframework.security.core.userdetails.UsernameNotFoundException;  // 사용자를 찾지 못했을 때의 예외
import org.springframework.stereotype.Service;  // 이 클래스가 서비스임을 명시

@Service  // 이 클래스가 서비스 계층의 컴포넌트임을 나타냄
@RequiredArgsConstructor  // final 필드에 대한 생성자 자동 생성
public class CustomUserDetailsService implements UserDetailsService {  // Spring Security의 인증을 위한 사용자 정보 로드 구현

    private final MemberRepository memberRepository;  // 회원 정보 접근을 위한 레포지토리

    @Override  // UserDetailsService 인터페이스의 메서드 구현
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 사용자명으로 회원 정보를 조회하고, 없으면 예외 발생
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Spring Security에서 사용할 User 객체 생성하여 반환
        return User.builder()
                .username(member.getUsername())  // 사용자명 설정
                .password(member.getPassword())  // 암호화된 비밀번호 설정
                .roles("USER")  // 기본 사용자 권한 설정
                .build();
    }
}