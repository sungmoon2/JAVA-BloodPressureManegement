package com.example.bpmanagement.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity  // JPA 엔티티로 정의, 이 클래스는 데이터베이스 테이블에 매핑됩니다.
@Getter  // Lombok을 사용하여 게터 메서드를 자동으로 생성합니다.
@Setter  // Lombok을 사용하여 세터 메서드를 자동으로 생성합니다.
@Builder  // Lombok을 사용하여 빌더 패턴을 지원하는 메서드를 자동으로 생성합니다.
@NoArgsConstructor  // Lombok을 사용하여 기본 생성자를 자동으로 생성합니다.
@AllArgsConstructor  // Lombok을 사용하여 모든 필드를 받는 생성자를 자동으로 생성합니다.
@Table(name = "bloodpressures")  // 이 클래스는 "bloodpressures"라는 테이블과 매핑됩니다.
@Data  // Lombok을 사용하여 `toString`, `equals`, `hashCode` 등의 메서드를 자동으로 생성합니다.
public class BloodPressure {

    @Id  // 이 필드는 기본 키를 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 기본 키 값은 데이터베이스에서 자동 증가됩니다.
    private Long id;

    @JsonIgnore  // 이 필드는 JSON 직렬화 시 무시됩니다. (순환 참조 방지)
    @ManyToOne(fetch = FetchType.LAZY)  // 여러 혈압 기록은 하나의 사용자(Member)와 연결됩니다.
    @JoinColumn(name = "member_id", nullable = false)  // 외래 키 설정, 혈압 기록이 특정 사용자와 연결되도록 합니다.
    private Member member;

    @Column(nullable = false)  // 측정 일시는 필수 입력 사항입니다.
    private LocalDateTime measureDatetime;  // 혈압 측정 날짜 및 시간

    @Column(nullable = false)  // 수축기 혈압은 필수 입력 사항입니다.
    private int systolic;  // 수축기 혈압 (예: 120)

    @Column(nullable = false)  // 이완기 혈압은 필수 입력 사항입니다.
    private int diastolic;  // 이완기 혈압 (예: 80)

    @Column(nullable = false)  // 맥박은 필수 입력 사항입니다.
    private int pulse;  // 맥박 (예: 72)

    private String remark;  // 혈압 측정에 대한 추가 설명 (선택 사항)
}
