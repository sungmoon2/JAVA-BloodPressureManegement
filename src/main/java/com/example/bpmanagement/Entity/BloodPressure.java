package com.example.bpmanagement.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

// 혈압 데이터를 저장하기 위한 JPA 엔티티 클래스
@Entity // 이 클래스가 JPA에서 관리하는 엔티티임을 나타냄
@Getter // 모든 필드에 대한 getter 메서드 자동 생성
@Setter // 모든 필드에 대한 setter 메서드 자동 생성
@Builder // 빌더 패턴을 사용하여 객체 생성 가능
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 초기화하는 생성자 자동 생성
@Table(name = "bloodpressures") // 데이터베이스의 테이블 이름을 "bloodpressures"로 지정
@Data // Lombok을 사용해 equals, hashCode, toString 메서드 자동 생성

public class BloodPressure {

    @Id // 기본 키(Primary Key) 필드임을 나타냄
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 기본 키의 값을 데이터베이스가 자동 생성 (Auto Increment)
    private Long id; // 혈압 데이터의 고유 ID

    // ID 값을 반환하는 getter 메서드 (명시적으로 작성됨)
    public Long getId() {
        return this.id;
    }

    @JsonIgnore // JSON 직렬화/역직렬화 시 무시되도록 설정
    @ManyToOne(fetch = FetchType.LAZY)
    // 다대일 관계 설정: 여러 혈압 데이터가 하나의 회원(Member)에 연결됨
    @JoinColumn(name = "member_id", nullable = false)
    // "member_id"라는 외래 키(Foreign Key)를 통해 Member 테이블과 연결
    private Member member; // 혈압 데이터를 등록한 회원 정보

    @Column(name = "measure_datetime", nullable = false)
    // 데이터베이스의 "measure_datetime" 컬럼과 매핑, null 값 허용 안 함
    @Temporal(TemporalType.TIMESTAMP)
    // 날짜 및 시간 정보를 저장하는 TIMESTAMP 타입으로 지정
    private LocalDateTime measureDatetime;
    // 혈압 측정 날짜와 시간을 저장

    @Column(nullable = false)
    // null 값 허용하지 않음
    private int systolic;
    // 수축기 혈압 (상압)

    @Column(nullable = false)
    // null 값 허용하지 않음
    private int diastolic;
    // 이완기 혈압 (하압)

    @Column(nullable = false)
    // null 값 허용하지 않음
    private int pulse;
    // 맥박 수

    private String remark;
    // 혈압 데이터와 관련된 추가 설명 (예: 운동 후 측정, 안정 상태 등)

    // 혈압 데이터를 업데이트하는 메서드
    public void updateData(Integer systolic, Integer diastolic, Integer pulse, String remark) {
        this.systolic = systolic; // 수축기 혈압 값 업데이트
        this.diastolic = diastolic; // 이완기 혈압 값 업데이트
        this.pulse = pulse; // 맥박 수 업데이트
        this.remark = remark; // 비고 업데이트
    }
}
