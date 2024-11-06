package com.example.bloodpressure.Service;

import com.example.bloodpressure.DTO.MemberDTO;  // DTO 클래스: 회원 데이터 전달 객체
import com.example.bloodpressure.Entity.Member;  // Member 엔티티 클래스: 데이터베이스와 매핑되는 클래스
import com.example.bloodpressure.Repository.MemberRepository;  // 회원 데이터 접근을 위한 리포지토리 인터페이스
import lombok.RequiredArgsConstructor;  // final 필드에 대한 생성자를 자동으로 생성하는 Lombok 애너테이션
import lombok.extern.slf4j.Slf4j;  // 로깅 기능 제공하는 Lombok 애너테이션
import org.springframework.security.crypto.password.PasswordEncoder;  // 비밀번호 암호화 처리를 위한 클래스
import org.springframework.stereotype.Service;  // 서비스 레이어로 등록하기 위한 Spring 애너테이션
import org.springframework.transaction.annotation.Transactional;  // 트랜잭션 관리를 위한 Spring 애너테이션

@Slf4j  // 로깅 기능 활성화: 로그를 사용해 서비스 실행 과정 추적
@Service  // Spring의 서비스 컴포넌트로 이 클래스 등록
@RequiredArgsConstructor  // final 필드를 포함한 생성자를 자동으로 생성
public class LoginService {

    private final MemberService memberService;  // 회원 조회 및 DTO 변환을 위한 서비스 인스턴스
    private final MemberRepository memberRepository;  // 데이터베이스 접근을 위한 리포지토리 인스턴스
    private final PasswordEncoder passwordEncoder;  // 비밀번호 암호화를 위한 PasswordEncoder 인스턴스

    // 회원 정보 조회 메서드
    @Transactional(readOnly = true)  // 읽기 전용 트랜잭션: 조회 성능 최적화
    public MemberDTO getMemberInfo(String username) {
        Member member = memberService.findMemberByUsername(username);  // 사용자명으로 회원 조회
        return memberService.toMemberDTO(member);  // Member 엔티티를 DTO로 변환하여 반환
    }

    // 회원 정보 수정 메서드
    @Transactional  // 데이터 변경 트랜잭션: 수정 및 저장 작업 수행
    public void updateMember(MemberDTO memberDTO) {
        Member member = memberService.findMemberByUsername(memberDTO.getUsername());  // 사용자명으로 회원 조회
        validateMemberUpdate(memberDTO);  // 회원 수정 데이터 유효성 검사
        updateMemberInfo(member, memberDTO);  // 회원 정보 업데이트
    }

    // 회원 수정 시 이메일 및 전화번호 중복 검증
    private void validateMemberUpdate(MemberDTO memberDTO) {
        // 현재 사용자의 아이디 외에 동일한 이메일을 가진 사용자가 있는지 검증
        memberRepository.findByEmailAndUsernameNot(memberDTO.getEmail(), memberDTO.getUsername())
                .ifPresent(m -> {
                    throw new RuntimeException("이미 사용 중인 이메일입니다.");
                });

        // 현재 사용자의 아이디 외에 동일한 전화번호를 가진 사용자가 있는지 검증
        memberRepository.findByPhoneNumberAndUsernameNot(memberDTO.getPhoneNumber(), memberDTO.getUsername())
                .ifPresent(m -> {
                    throw new RuntimeException("이미 사용 중인 전화번호입니다.");
                });
    }

    // 회원 정보 업데이트 메서드
    private void updateMemberInfo(Member member, MemberDTO memberDTO) {
        // 비밀번호 변경이 필요한 경우: 비밀번호 확인 및 암호화 후 업데이트
        if (memberDTO.getPassword() != null && !memberDTO.getPassword().isEmpty()) {
            if (!memberDTO.getPassword().equals(memberDTO.getPasswordConfirm())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");  // 비밀번호 불일치 시 예외 발생
            }
            member.updatePassword(passwordEncoder.encode(memberDTO.getPassword()));  // 비밀번호 암호화 후 저장
        }

        // 회원의 나머지 정보를 업데이트
        member.updateInfo(
                memberDTO.getName(),  // 이름 업데이트
                memberDTO.getEmail(),  // 이메일 업데이트
                memberDTO.getPhoneNumber(),  // 전화번호 업데이트
                memberDTO.getAge(),  // 나이 업데이트
                memberDTO.getHeight(),  // 키 업데이트
                memberDTO.getWeight(),  // 몸무게 업데이트
                memberDTO.getGender()  // 성별 업데이트
        );
    }
}
