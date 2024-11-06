package com.example.bloodpressure.Service;

import com.example.bloodpressure.DTO.MemberDTO;  // DTO 클래스: 데이터를 전달할 때 사용
import com.example.bloodpressure.Entity.Member;  // 회원 엔티티 클래스: 데이터베이스와 매핑되는 클래스
import com.example.bloodpressure.Repository.MemberRepository;  // 회원 데이터 접근을 위한 리포지토리 인터페이스
import lombok.RequiredArgsConstructor;  // final 필드에 대한 생성자를 자동으로 생성해주는 Lombok 애너테이션
import lombok.extern.slf4j.Slf4j;  // 로깅 기능 제공하는 Lombok 애너테이션
import org.springframework.security.core.userdetails.UsernameNotFoundException;  // 사용자 미발견 예외 클래스
import org.springframework.stereotype.Service;  // 서비스 레이어로 등록하기 위한 Spring 애너테이션

@Slf4j  // 로깅 기능 활성화: 로그를 사용해 서비스 실행 과정 추적
@Service  // Spring의 서비스 컴포넌트로 이 클래스 등록
@RequiredArgsConstructor  // final 필드를 포함한 생성자를 자동으로 생성
public class MemberService {

    private final MemberRepository memberRepository;  // 데이터베이스 접근을 위한 리포지토리 인스턴스

    // 사용자 이름으로 회원 정보를 조회하는 공통 메서드
    public Member findMemberByUsername(String username) {
        // 데이터베이스에서 사용자명을 기준으로 회원 정보 조회
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));  // 사용자 미발견 시 예외 발생
    }

    // Member 엔티티를 MemberDTO로 변환하는 메서드
    public MemberDTO toMemberDTO(Member member) {
        // Member 엔티티의 필드를 MemberDTO로 변환하여 반환
        return MemberDTO.builder()
                .username(member.getUsername())  // 사용자명 설정
                .name(member.getName())  // 이름 설정
                .email(member.getEmail())  // 이메일 설정
                .phoneNumber(member.getPhoneNumber())  // 전화번호 설정
                .age(member.getAge())  // 나이 설정
                .height(member.getHeight())  // 키 설정
                .weight(member.getWeight())  // 몸무게 설정
                .gender(member.getGender())  // 성별 설정
                .build();  // MemberDTO 객체 생성 및 반환
    }
}
