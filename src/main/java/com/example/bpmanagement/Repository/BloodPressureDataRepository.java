package com.example.bpmanagement.Repository;

import com.example.bpmanagement.Entity.BloodPressureData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloodPressureDataRepository extends JpaRepository<BloodPressureData, Long> {
    // 회원별 혈압 데이터 조회 (최신순)
    List<BloodPressureData> findByMemberIdOrderByMeasureDatetimeDesc(Long memberId);

    // 회원별 특정 기간 혈압 데이터 조회 (시간순)
    List<BloodPressureData> findByMemberIdAndMeasureDatetimeBetweenOrderByMeasureDatetimeAsc(
            Long memberId, LocalDateTime start, LocalDateTime end);

    // 최근 N개의 데이터를 조회하는 메서드 수정
    List<BloodPressureData> findByMemberId(Long memberId, Pageable pageable);
}