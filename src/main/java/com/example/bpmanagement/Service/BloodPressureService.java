package com.example.bpmanagement.Service;

import com.example.bpmanagement.DTO.BloodPressureDTO;
import com.example.bpmanagement.Entity.BloodPressure;
import com.example.bpmanagement.Entity.BloodPressureData;
import com.example.bpmanagement.Entity.Member;
import com.example.bpmanagement.Repository.BloodPressureRepository;
import com.example.bpmanagement.Repository.BloodPressureDataRepository;
import com.example.bpmanagement.Repository.MemberRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class BloodPressureService {

    // 수기 입력 데이터와 자동 측정 데이터를 합쳐서 보여주기 위한 DTO 클래스
    @Getter
    @Setter
    public class CombinedBloodPressureData {
        private LocalDateTime measureDatetime;
        private int systolic;
        private int diastolic;
        private int pulse;
        private String remark;
        private String source;
    }

    public List<BloodPressureData> getRecentRecords(Long memberId, int count) {
        PageRequest pageRequest = PageRequest.of(0, count,
                Sort.by(Sort.Direction.DESC, "measureDatetime"));
        return dataRepository.findByMemberId(memberId, pageRequest);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Invalid date format: {}", e.getMessage());
        return ResponseEntity.badRequest().body("Invalid date format. Please use 'yyyy-MM-ddTHH:mm:ss' format.");
    }

    private final BloodPressureRepository repository;
    private final BloodPressureDataRepository dataRepository;
    private final MemberRepository memberRepository;

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    };

    @Autowired
    public BloodPressureService(BloodPressureRepository repository,
                                BloodPressureDataRepository dataRepository,
                                MemberRepository memberRepository) {
        this.repository = repository;
        this.dataRepository = dataRepository;
        this.memberRepository = memberRepository;
    }

    // 모든 혈압 기록을 조회하는 메서드 (수기 입력과 자동 측정 데이터 모두 포함)
    public List<CombinedBloodPressureData> getBloodPressureRecords() throws SQLException {
        List<CombinedBloodPressureData> combinedData = new ArrayList<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;

        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("로그인한 사용자 정보를 찾을 수 없습니다."));

        // 1. 수기 입력 데이터 가져오기
        List<BloodPressure> manualRecords = repository.findAllByMemberOrderByMeasureDatetimeDesc(member);
        for (BloodPressure record : manualRecords) {
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());
            combined.setSystolic(record.getSystolic());
            combined.setDiastolic(record.getDiastolic());
            combined.setPulse(record.getPulse());
            combined.setRemark(record.getRemark());
            combined.setSource("수기입력");
            combinedData.add(combined);
        }

        // 2. 자동 측정 데이터 가져오기
        List<BloodPressureData> autoRecords = dataRepository.findByMemberIdOrderByMeasureDatetimeDesc(member.getId());
        for (BloodPressureData record : autoRecords) {
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());
            combined.setSystolic(record.getSystolic());
            combined.setDiastolic(record.getDiastolic());
            combined.setPulse(record.getPulse());
            combined.setRemark(record.getRemark());
            combined.setSource("자동측정");
            combinedData.add(combined);
        }

        combinedData.sort((a, b) -> b.getMeasureDatetime().compareTo(a.getMeasureDatetime()));

        log.info("Retrieved {} total blood pressure records for user {}", combinedData.size(), username);
        return combinedData;
    }

    public void addBloodPressureRecord(BloodPressureDTO recordDTO) throws SQLException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;

        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("로그인한 사용자 정보를 찾을 수 없습니다."));

        BloodPressure record = new BloodPressure();
        record.setMember(member);
        record.setSystolic(recordDTO.getSystolic());
        record.setDiastolic(recordDTO.getDiastolic());
        record.setPulse(recordDTO.getPulse());
        record.setRemark(recordDTO.getRemark());

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
                    log.debug("Date parsing failed with formatter {}: {}", formatter, inputDate);
                }
            }
            if (!parsed) throw new SQLException("Invalid date format: " + inputDate);
        } else {
            record.setMeasureDatetime(LocalDateTime.now());
        }

        try {
            repository.save(record);
            log.info("Blood pressure record saved successfully for user {}", username);
        } catch (Exception e) {
            log.error("Error saving blood pressure record for user {}: {}", username, e.getMessage());
            throw new SQLException("Error saving blood pressure data", e);
        }
    }

    public List<CombinedBloodPressureData> getBloodPressureRecordsByPeriod(LocalDateTime start, LocalDateTime end)
            throws SQLException {
        List<CombinedBloodPressureData> combinedData = new ArrayList<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new SQLException("사용자 정보를 찾을 수 없습니다."));

        // 1. 수기 입력 데이터 조회
        List<BloodPressure> manualRecords = repository
                .findByMemberAndMeasureDatetimeBetweenOrderByMeasureDatetimeAsc(member, start, end);
        for (BloodPressure record : manualRecords) {
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());
            combined.setSystolic(record.getSystolic());
            combined.setDiastolic(record.getDiastolic());
            combined.setPulse(record.getPulse());
            combined.setRemark(record.getRemark());
            combined.setSource("수기입력");
            combinedData.add(combined);
        }

        // 2. 자동 측정 데이터 조회
        List<BloodPressureData> autoRecords = dataRepository
                .findByMemberIdAndMeasureDatetimeBetweenOrderByMeasureDatetimeAsc(member.getId(), start, end);
        for (BloodPressureData record : autoRecords) {
            CombinedBloodPressureData combined = new CombinedBloodPressureData();
            combined.setMeasureDatetime(record.getMeasureDatetime());
            combined.setSystolic(record.getSystolic());
            combined.setDiastolic(record.getDiastolic());
            combined.setPulse(record.getPulse());
            combined.setRemark(record.getRemark());
            combined.setSource("자동측정");
            combinedData.add(combined);
        }

        combinedData.sort((a, b) -> a.getMeasureDatetime().compareTo(b.getMeasureDatetime()));

        log.info("Retrieved {} blood pressure records between {} and {} for user {}",
                combinedData.size(), start, end, username);
        return combinedData;
    }

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