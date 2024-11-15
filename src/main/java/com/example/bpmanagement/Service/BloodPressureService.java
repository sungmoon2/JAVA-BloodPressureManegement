package com.example.bpmanagement.Service;  // 서비스 계층의 패키지 선언

// 각 import문의 역할:
// DTO: 데이터 전송 객체 클래스
import com.example.bpmanagement.DTO.BloodPressureDTO;
// Entity: 데이터베이스 테이블과 매핑되는 엔티티 클래스들
import com.example.bpmanagement.Entity.BloodPressure;          // 수기 입력 혈압 데이터
import com.example.bpmanagement.Entity.BloodPressureData;      // 자동 측정 혈압 데이터
import com.example.bpmanagement.Entity.Member;                 // 회원 정보
// Repository: 데이터 접근 계층 인터페이스들
import com.example.bpmanagement.Repository.BloodPressureRepository;        // 수기 입력 데이터 저장소
import com.example.bpmanagement.Repository.BloodPressureDataRepository;    // 자동 측정 데이터 저장소
import com.example.bpmanagement.Repository.MemberRepository;               // 회원 정보 저장소

// Lombok: 보일러플레이트 코드 감소를 위한 어노테이션
import lombok.Getter;                // getter 메서드 자동 생성
import lombok.Setter;                // setter 메서드 자동 생성
import lombok.extern.slf4j.Slf4j;    // 로깅 기능 자동 구현

// Spring Framework 관련 기능들
import org.springframework.beans.factory.annotation.Autowired;              // 의존성 자동 주입
import org.springframework.data.domain.PageRequest;                        // 페이징 처리
import org.springframework.data.domain.Sort;                              // 정렬 처리
import org.springframework.http.ResponseEntity;                           // HTTP 응답 래퍼
import org.springframework.security.core.Authentication;                   // 인증 정보
import org.springframework.security.core.context.SecurityContextHolder;    // 보안 컨텍스트
import org.springframework.security.core.userdetails.UserDetails;         // 사용자 상세 정보
import org.springframework.stereotype.Service;                            // 서비스 계층 명시
import org.springframework.transaction.annotation.Transactional;          // 트랜잭션 처리
import org.springframework.web.bind.MethodArgumentNotValidException;      // 유효성 검증 예외
import org.springframework.web.bind.annotation.ExceptionHandler;          // 예외 처리기

// Java 표준 라이브러리
import java.sql.SQLException;                    // SQL 예외 처리
import java.time.LocalDateTime;                  // 날짜/시간 처리
import java.time.format.DateTimeFormatter;        // 날짜/시간 형식 지정
import java.time.format.DateTimeParseException;   // 날짜/시간 파싱 예외
import java.util.ArrayList;                       // 동적 배열
import java.util.List;                           // 리스트 인터페이스

@Slf4j          // 로깅을 위한 Logger 객체를 자동으로 생성
@Service        // 이 클래스가 서비스 계층임을 Spring에 알림
@Transactional  // 모든 public 메서드에 트랜잭션 처리를 자동으로 적용
public class BloodPressureService {

    // 수기 입력과 자동 측정 데이터를 통합하여 표현하기 위한 내부 클래스
    @Getter @Setter  // 모든 필드에 대한 getter/setter 메서드 자동 생성
    public class CombinedBloodPressureData {
        private LocalDateTime measureDatetime;  // 측정 일시를 저장
        private int systolic;                   // 수축기 혈압 값을 저장
        private int diastolic;                  // 이완기 혈압 값을 저장
        private int pulse;                      // 맥박 수를 저장
        private String remark;                  // 비고 사항을 저장
        private String source;                  // 데이터 출처 (수기입력/자동측정)를 구분하여 저장
    }

    // 회원별 최근 측정 기록을 조회하는 메서드
    public List<BloodPressureData> getRecentRecords(Long memberId, int count) {
        // 페이징 처리와 정렬 조건을 설정
        // 0: 첫 페이지, count: 조회할 개수, Sort: 측정일시 기준 내림차순 정렬
        PageRequest pageRequest = PageRequest.of(0, count,
                Sort.by(Sort.Direction.DESC, "measureDatetime"));
        // 설정된 조건으로 데이터베이스에서 조회
        return dataRepository.findByMemberId(memberId, pageRequest);
    }

    // 유효성 검증 실패 시 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException e) {
        // 에러 로그 기록
        log.error("Invalid date format: {}", e.getMessage());
        // 클라이언트에게 400 Bad Request 응답과 함께 에러 메시지 반환
        return ResponseEntity.badRequest().body("Invalid date format. Please use 'yyyy-MM-ddTHH:mm:ss' format.");
    }

    // Repository 객체들을 final로 선언하여 불변성 보장
    private final BloodPressureRepository repository;            // 수기 입력 데이터 처리
    private final BloodPressureDataRepository dataRepository;    // 자동 측정 데이터 처리
    private final MemberRepository memberRepository;             // 회원 정보 처리

    // 지원하는 날짜 형식들을 상수 배열로 정의
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            // "2024-01-01 12:34:56" 형식
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            // "2024-01-01T12:34" 형식 (ISO 형식)
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            // "2024/01/01 12:34:56" 형식
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    };

    // 생성자를 통한 의존성 주입
    @Autowired
    public BloodPressureService(BloodPressureRepository repository,
                                BloodPressureDataRepository dataRepository,
                                MemberRepository memberRepository) {
        // 각 Repository를 필드에 할당
        this.repository = repository;
        this.dataRepository = dataRepository;
        this.memberRepository = memberRepository;
    }

    // 모든 혈압 기록을 조회하는 메서드
    public List<CombinedBloodPressureData> getBloodPressureRecords() throws SQLException {
        // 결과를 저장할 리스트 생성
        List<CombinedBloodPressureData> combinedData = new ArrayList<>();

        // Spring Security에서 현재 인증된 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;

        // Principal 객체가 UserDetails 타입인지 확인하여 사용자명 추출
        if (authentication.getPrincipal() instanceof UserDetails) {
            // UserDetails 타입이면 해당 메서드로 사용자명 가져옴
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            // 그 외의 경우 직접 getName() 호출
            username = authentication.getName();
        }

        // 사용자명으로 회원 정보를 조회, 없으면 예외 발생
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 1. 수기 입력 데이터 처리
        // 회원의 모든 수기 입력 데이터를 측정일시 내림차순으로 조회
        List<BloodPressure> manualRecords = repository.findAllByMemberOrderByMeasureDatetimeDesc(member);
        // 각 수기 입력 데이터를 통합 데이터 형식으로 변환
        for (BloodPressure record : manualRecords) {
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());
            combined.setSystolic(record.getSystolic());
            combined.setDiastolic(record.getDiastolic());
            combined.setPulse(record.getPulse());
            combined.setRemark(record.getRemark());
            combined.setSource("수기입력");  // 데이터 출처 표시
            combinedData.add(combined);
        }

        // 2. 자동 측정 데이터 처리
        // 회원의 모든 자동 측정 데이터를 측정일시 내림차순으로 조회
        List<BloodPressureData> autoRecords = dataRepository.findByMemberIdOrderByMeasureDatetimeDesc(member.getId());
        // 각 자동 측정 데이터를 통합 데이터 형식으로 변환
        for (BloodPressureData record : autoRecords) {
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());
            combined.setSystolic(record.getSystolic());
            combined.setDiastolic(record.getDiastolic());
            combined.setPulse(record.getPulse());
            combined.setRemark(record.getRemark());
            combined.setSource("자동측정");  // 데이터 출처 표시
            combinedData.add(combined);
        }

        // 모든 데이터를 측정일시 기준 내림차순으로 정렬 (최신순)
        combinedData.sort((a, b) -> b.getMeasureDatetime().compareTo(a.getMeasureDatetime()));

        // 조회 결과 로그 기록
        log.info("Retrieved {} total blood pressure records for user {}", combinedData.size(), username);
        return combinedData;
    }

    // 새로운 혈압 기록을 추가하는 메서드
    public void addBloodPressureRecord(BloodPressureDTO recordDTO) throws SQLException {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;

        // Principal 객체 타입에 따라 사용자명 추출
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        // 사용자명으로 회원 정보 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 새로운 혈압 기록 엔티티 생성
        BloodPressure record = new BloodPressure();
        record.setMember(member);                           // 회원 정보 설정
        record.setSystolic(recordDTO.getSystolic());        // 수축기 혈압 설정
        record.setDiastolic(recordDTO.getDiastolic());      // 이완기 혈압 설정
        record.setPulse(recordDTO.getPulse());              // 맥박 설정
        record.setRemark(recordDTO.getRemark());            // 비고 설정

        // 측정일시 처리
        String inputDate = String.valueOf(recordDTO.getMeasureDatetime());
        if (inputDate != null && !inputDate.isEmpty()) {
            boolean parsed = false;
            // 정의된 모든 날짜 형식으로 파싱 시도
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    LocalDateTime measureDatetime = LocalDateTime.parse(inputDate, formatter);
                    record.setMeasureDatetime(measureDatetime);
                    parsed = true;
                    break;
                } catch (DateTimeParseException e) {
                    // 파싱 실패 시 디버그 로그 기록
                    log.debug("Date parsing failed with formatter {}: {}", formatter, inputDate);
                }
            }
            // 모든 형식으로 파싱 실패 시 예외 발생
            if (!parsed) throw new SQLException("Invalid date format: " + inputDate);
        } else {
            // 측정일시가 없으면 현재 시간으로 설정
            record.setMeasureDatetime(LocalDateTime.now());
        }

        // 데이터베이스에 저장 시도
        try {
            repository.save(record);
            // 저장 성공 로그
            log.info("Blood pressure record saved successfully for user {}", username);
        } catch (Exception e) {
            // 저장 실패 시 에러 로그 기록 후 예외 발생
            log.error("Error saving blood pressure record for user {}: {}", username, e.getMessage());
            throw new SQLException("Error saving blood pressure data", e);
        }
    }

    // 특정 기간의 혈압 기록을 조회하는 메서드
    public List<CombinedBloodPressureData> getBloodPressureRecordsByPeriod(LocalDateTime start, LocalDateTime end)
            throws SQLException {
        // 결과를 저장할 리스트 생성
        List<CombinedBloodPressureData> combinedData = new ArrayList<>();

        // 현재 인증된 사용자 정보 가져오기
        // Spring Security의 SecurityContext에서 현재 인증된 사용자의 Authentication 객체를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Authentication 객체에서 사용자명을 직접 추출
        String username = authentication.getName();

        // memberRepository를 사용하여 사용자명으로 회원 정보를 조회
        // Optional에서 값이 없는 경우 SQLException을 발생시킴
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("사용자 정보를 찾을 수 없습니다."));

        // 1. 수기 입력 데이터 조회
        // 특정 회원의 지정된 기간(start~end) 내의 수기 입력 데이터를 측정일시 오름차순으로 조회
        List<BloodPressure> manualRecords = repository
                .findByMemberAndMeasureDatetimeBetweenOrderByMeasureDatetimeAsc(member, start, end);
        // 각 수기 입력 데이터를 CombinedBloodPressureData 형식으로 변환하여 저장
        for (BloodPressure record : manualRecords) {
            // 새로운 통합 데이터 객체 생성
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());   // 측정일시 설정
            combined.setSystolic(record.getSystolic());                 // 수축기 혈압 설정
            combined.setDiastolic(record.getDiastolic());               // 이완기 혈압 설정
            combined.setPulse(record.getPulse());                       // 맥박 설정
            combined.setRemark(record.getRemark());                     // 비고 설정
            combined.setSource("수기입력");                              // 데이터 출처를 수기입력으로 표시
            combinedData.add(combined);                                 // 결과 리스트에 추가
        }

        // 2. 자동 측정 데이터 조회
        // 특정 회원의 지정된 기간(start~end) 내의 자동 측정 데이터를 측정일시 오름차순으로 조회
        List<BloodPressureData> autoRecords = dataRepository
                .findByMemberIdAndMeasureDatetimeBetweenOrderByMeasureDatetimeAsc(member.getId(), start, end);
        // 각 자동 측정 데이터를 CombinedBloodPressureData 형식으로 변환하여 저장
        for (BloodPressureData record : autoRecords) {
            // 새로운 통합 데이터 객체 생성
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());   // 측정일시 설정
            combined.setSystolic(record.getSystolic());                 // 수축기 혈압 설정
            combined.setDiastolic(record.getDiastolic());               // 이완기 혈압 설정
            combined.setPulse(record.getPulse());                       // 맥박 설정
            combined.setRemark(record.getRemark());                     // 비고 설정
            combined.setSource("자동측정");                              // 데이터 출처를 자동측정으로 표시
            combinedData.add(combined);                                 // 결과 리스트에 추가
        }

        // 수기입력과 자동측정 데이터가 모두 포함된 전체 리스트를 측정일시 기준으로 오름차순 정렬
        // (a, b) -> a.getMeasureDatetime().compareTo(b.getMeasureDatetime()): 람다식을 사용한 Comparator 구현
        combinedData.sort((a, b) -> a.getMeasureDatetime().compareTo(b.getMeasureDatetime()));

        // 조회 결과에 대한 로그 기록
        // - 전체 데이터 개수
        // - 조회 기간의 시작과 끝
        // - 조회한 사용자명
        log.info("Retrieved {} blood pressure records between {} and {} for user {}",
                combinedData.size(), start, end, username);
        // 정렬된 전체 데이터 반환
        return combinedData;
    }

    // BloodPressure 엔티티를 BloodPressureDTO로 변환하는 유틸리티 메서드
    private BloodPressureDTO convertToDTO(BloodPressure bloodPressure) {
        // 새로운 DTO 객체 생성
        BloodPressureDTO dto = new BloodPressureDTO();
        dto.setMeasureDatetime(bloodPressure.getMeasureDatetime());   // 측정일시 복사
        dto.setSystolic(bloodPressure.getSystolic());                 // 수축기 혈압 복사
        dto.setDiastolic(bloodPressure.getDiastolic());               // 이완기 혈압 복사
        dto.setPulse(bloodPressure.getPulse());                       // 맥박 복사
        dto.setRemark(bloodPressure.getRemark());                     // 비고 복사
        return dto;                                                   // 변환된 DTO 반환
    }
}