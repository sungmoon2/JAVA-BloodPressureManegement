package com.example.bpmanagement.Service;

import com.example.bpmanagement.DTO.MemberDTO;  // 회원 정보를 주고받기 위한 DTO 클래스 임포트
import com.example.bpmanagement.Entity.Member;  // 회원 정보를 저장하는 엔티티 클래스 임포트
import com.example.bpmanagement.Repository.MemberRepository;  // 회원 데이터베이스 접근을 위한 리포지토리 임포트
import lombok.RequiredArgsConstructor;  // final 필드에 대한 생성자를 자동 생성해주는 Lombok 애너테이션 임포트
import lombok.extern.slf4j.Slf4j;  // 로깅을 위한 Lombok 애너테이션 임포트
import org.springframework.security.crypto.password.PasswordEncoder;  // 비밀번호 암호화를 위한 Spring Security 클래스 임포트
import org.springframework.stereotype.Service;  // 서비스 레이어로 등록하기 위한 Spring 애너테이션 임포트
import org.springframework.transaction.annotation.Transactional;  // 트랜잭션 처리를 위한 애너테이션 임포트

@Slf4j  // 로깅 기능을 활성화하여 로그 메시지를 기록할 수 있도록 함
@Service  // 이 클래스가 Spring의 서비스 레이어 역할을 한다는 것을 나타냄
@RequiredArgsConstructor  // final 필드에 대해 생성자를 자동으로 생성해주는 Lombok 애너테이션
public class RegisterService {

    private final MemberRepository memberRepository;  // 데이터베이스 연동을 위한 멤버 리포지토리
    private final PasswordEncoder passwordEncoder;  // 비밀번호 암호화를 위한 PasswordEncoder 객체

    @Transactional  // 이 메서드가 하나의 트랜잭션으로 실행되도록 보장함
    public void register(MemberDTO memberDTO) {
        log.info("회원가입 시도: {}", memberDTO.getUsername());  // 회원가입 시도 로그 출력
        validateNewMember(memberDTO);  // 회원 정보 중복 여부 검증
        saveMember(memberDTO);  // 회원 정보 저장
        log.info("회원가입 성공: {}", memberDTO.getUsername());  // 회원가입 성공 로그 출력
    }

    // 새로운 회원 데이터 검증 메서드
    private void validateNewMember(MemberDTO memberDTO) {
        log.info("신규 회원 데이터 검증 시작");  // 검증 시작 로그 출력

        // 아이디 중복 검증
        memberRepository.findByUsername(memberDTO.getUsername())
                .ifPresent(m -> {
                    log.error("아이디 중복: {}", memberDTO.getUsername());  // 중복 아이디 로그 출력
                    throw new RuntimeException("이미 존재하는 아이디입니다.");  // 예외 발생
                });

        // 이메일 중복 검증
        memberRepository.findByEmail(memberDTO.getEmail())
                .ifPresent(m -> {
                    log.error("이메일 중복: {}", memberDTO.getEmail());  // 중복 이메일 로그 출력
                    throw new RuntimeException("이미 등록된 이메일입니다.");  // 예외 발생
                });

        // 전화번호 중복 검증
        memberRepository.findByPhoneNumber(memberDTO.getPhoneNumber())
                .ifPresent(m -> {
                    log.error("전화번호 중복: {}", memberDTO.getPhoneNumber());  // 중복 전화번호 로그 출력
                    throw new RuntimeException("이미 등록된 전화번호입니다.");  // 예외 발생
                });

        log.info("신규 회원 데이터 검증 완료");  // 검증 완료 로그 출력
    }

    // 회원 정보 저장 메서드
    private void saveMember(MemberDTO memberDTO) {
        try {
            // Member 엔티티 객체 생성 및 데이터 설정
            Member member = Member.builder()
                    .username(memberDTO.getUsername())  // 사용자명 설정
                    .password(passwordEncoder.encode(memberDTO.getPassword()))  // 비밀번호 암호화 후 설정
                    .name(memberDTO.getName())  // 이름 설정
                    .email(memberDTO.getEmail())  // 이메일 설정
                    .phoneNumber(memberDTO.getPhoneNumber())  // 전화번호 설정
                    .age(memberDTO.getAge())  // 나이 설정
                    .height(memberDTO.getHeight())  // 키 설정
                    .weight(memberDTO.getWeight())  // 몸무게 설정
                    .gender(memberDTO.getGender())  // 성별 설정
                    .build();  // Member 객체 생성 완료

            memberRepository.save(member);  // 생성된 Member 객체를 데이터베이스에 저장
        } catch (Exception e) {
            log.error("회원 저장 실패: {}", e.getMessage());  // 저장 실패 로그 출력
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다.");  // 예외 발생
        }
    }
}