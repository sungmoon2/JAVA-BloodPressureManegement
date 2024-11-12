package com.example.bpmanagement.Service;

// 필요한 라이브러리와 클래스들을 import 합니다.
import com.example.bpmanagement.DTO.BloodPressureDTO; // 혈압 데이터 전송 객체 (DTO)
import com.example.bpmanagement.Entity.BloodPressure; // 혈압 엔터티 클래스 (Entity)
import com.example.bpmanagement.Entity.Member; // 사용자 엔터티 클래스
import com.example.bpmanagement.Repository.BloodPressureRepository; // 혈압 리포지토리 인터페이스
import com.example.bpmanagement.Repository.MemberRepository; // 사용자 리포지토리 인터페이스
import lombok.extern.slf4j.Slf4j; // 로깅 기능 제공 (Lombok 라이브러리)
import org.springframework.beans.factory.annotation.Autowired; // 의존성 주입을 위한 애너테이션
import org.springframework.http.ResponseEntity; // HTTP 응답 객체
import org.springframework.security.core.Authentication; // 사용자 인증 정보 객체
import org.springframework.security.core.context.SecurityContextHolder; // 보안 컨텍스트에서 인증 정보 가져오기
import org.springframework.security.core.userdetails.UserDetails; // 사용자 세부 정보 객체
import org.springframework.stereotype.Service; // 이 클래스가 서비스 레이어임을 나타내는 애너테이션
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 처리를 위한 애너테이션
import org.springframework.web.bind.MethodArgumentNotValidException; // 유효성 검사 예외 클래스
import org.springframework.web.bind.annotation.ExceptionHandler; // 예외 처리 메서드에 사용하는 애너테이션

import java.sql.SQLException; // SQL 관련 예외 처리
import java.time.LocalDateTime; // 날짜와 시간 객체
import java.time.format.DateTimeFormatter; // 날짜 형식 지정 객체
import java.time.format.DateTimeParseException; // 날짜 파싱 실패 시 예외 처리
import java.util.List; // 리스트 데이터 구조

// Slf4j 로그 기능 활성화 및 서비스 클래스 정의
@Slf4j
@Service
@Transactional // 클래스 또는 메서드 단위에서 트랜잭션 처리를 보장
public class BloodPressureService {

    // 유효성 검사 실패 시 예외 처리 메서드
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Invalid date format: {}", e.getMessage()); // 로그 출력
        // 잘못된 날짜 형식에 대한 오류 응답 반환
        return ResponseEntity.badRequest().body("Invalid date format. Please use 'yyyy-MM-ddTHH:mm:ss' format.");
    }

    // 리포지토리 필드 선언: 데이터베이스 연동을 위한 객체들
    private final BloodPressureRepository repository;
    private final MemberRepository memberRepository;

    // 지원하는 날짜 형식들을 미리 정의 (다양한 형식의 입력을 처리하기 위함)
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // 형식 1
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),   // 형식 2
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")   // 형식 3
    };

    // 생성자: 의존성 주입을 통해 리포지토리 초기화
    @Autowired
    public BloodPressureService(BloodPressureRepository repository, MemberRepository memberRepository) {
        this.repository = repository;
        this.memberRepository = memberRepository;
    }

    // 모든 혈압 기록을 조회하는 메서드
    public List<BloodPressure> getBloodPressureRecords() throws SQLException {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;

        // 인증된 사용자 이름 가져오기
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        // 사용자 엔터티 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 사용자별 혈압 기록 리스트 조회
        List<BloodPressure> records = repository.findAllByMemberOrderByMeasureDatetimeDesc(member);
        log.info("Retrieved {} blood pressure records for user {}", records.size(), username);
        return records;
    }

    // 혈압 기록 추가 메서드
    public void addBloodPressureRecord(BloodPressureDTO recordDTO) throws SQLException {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;

        // 사용자 이름 가져오기
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        // 사용자 엔터티 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 새로운 혈압 기록 생성 및 설정
        BloodPressure record = new BloodPressure();
        record.setMember(member);
        record.setSystolic(recordDTO.getSystolic());
        record.setDiastolic(recordDTO.getDiastolic());
        record.setPulse(recordDTO.getPulse());
        record.setRemark(recordDTO.getRemark());

        // 측정 날짜 시간 설정
        String inputDate = String.valueOf(recordDTO.getMeasureDatetime());
        if (inputDate != null && !inputDate.isEmpty()) {
            boolean parsed = false;
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    LocalDateTime measureDatetime = LocalDateTime.parse(inputDate, formatter);
                    record.setMeasureDatetime(measureDatetime);
                    parsed = true;
                    break;
                } catch (DateTimeParseException e) {
                    log.debug("Date parsing failed: {}", inputDate);
                }
            }
            if (!parsed) throw new SQLException("Invalid date format: " + inputDate);
        } else {
            record.setMeasureDatetime(LocalDateTime.now());
        }

        // 데이터베이스에 기록 저장
        try {
            repository.save(record);
            log.info("Record saved successfully");
        } catch (Exception e) {
            log.error("Save error", e);
            throw new SQLException("Error saving data", e);
        }
    }

    // 특정 기간의 혈압 기록 조회 메서드
    public List<BloodPressure> getBloodPressureRecordsByPeriod(LocalDateTime start, LocalDateTime end) throws SQLException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("사용자 정보를 찾을 수 없습니다."));

        return repository.findByMemberAndMeasureDatetimeBetweenOrderByMeasureDatetimeAsc(member, start, end);
    }

    // 엔터티에서 DTO로 변환 메서드
    private BloodPressureDTO convertToDTO(BloodPressure bloodPressure) {
        BloodPressureDTO dto = new BloodPressureDTO();
        dto.setMeasureDatetime(bloodPressure.getMeasureDatetime());
        dto.setSystolic(bloodPressure.getSystolic());
        dto.setDiastolic(bloodPressure.getDiastolic());
        dto.setPulse(bloodPressure.getPulse());
        dto.setRemark(bloodPressure.getRemark());
        return dto;
    }
}
