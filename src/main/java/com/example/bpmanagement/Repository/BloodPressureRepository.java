package com.example.bpmanagement.Repository;

// JPA 엔티티인 BloodPressure를 관리하기 위한 JpaRepository를 import
import com.example.bpmanagement.Entity.BloodPressure;
import com.example.bpmanagement.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository  // 이 인터페이스가 스프링의 Repository임을 명시
public interface BloodPressureRepository extends JpaRepository<BloodPressure, Long> {

    // 특정 회원(Member)의 혈압 데이터를 날짜 기준으로 내림차순 정렬하여 조회
    List<BloodPressure> findAllByMemberOrderByMeasureDatetimeDesc(Member member);

    List<BloodPressure> findByMemberAndMeasureDatetimeBetweenOrderByMeasureDatetimeAsc(
            Member member, LocalDateTime startDate, LocalDateTime endDate);

    List<BloodPressure> findByMeasureDatetimeBetweenOrderByMeasureDatetimeDesc(LocalDateTime startDate, LocalDateTime endDate);


};


