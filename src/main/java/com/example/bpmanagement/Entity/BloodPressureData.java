package com.example.bpmanagement.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// JPA 엔티티 클래스
@Entity
@Table(name = "bloodpressuredata")  // 데이터베이스의 bloodpressuredata 테이블과 매핑
@Getter
@Setter
@NoArgsConstructor  // 파라미터 없는 기본 생성자 생성 (Lombok 사용)
@AllArgsConstructor // 모든 필드를 포함하는 생성자 생성 (Lombok 사용)
public class BloodPressureData {

    // 기본 키 (Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 자동 증가 (IDENTITY 전략 사용)
    @Column(name = "id", nullable = false)
    private Long id;

    // 회원 ID (Foreign Key)
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // 측정 날짜 및 시간
    @Column(name = "measure_datetime", nullable = false)
    private LocalDateTime measureDatetime;

    // 수축기 혈압 (Systolic)
    @Column(nullable = false)
    private int systolic;

    // 이완기 혈압 (Diastolic)
    @Column(nullable = false)
    private int diastolic;

    // 맥박 (Pulse)
    @Column(nullable = false)
    private int pulse;

    // 추가적인 메모나 주석 (Remark)
    private String remark;

    // 생성 시각
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "measure_datetime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)  // timestamp 타입 명시

    // 엔티티가 생성되기 전에 자동으로 실행되는 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // 현재 시간을 생성 시각으로 설정
    }
}