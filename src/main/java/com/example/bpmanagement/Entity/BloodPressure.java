package com.example.bpmanagement.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bloodpressures")
@Data
public class BloodPressure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return this.id;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // measureDatetime 필드 하나로 통합
    @Column(name = "measure_datetime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime measureDatetime;

    @Column(nullable = false)
    private int systolic;

    @Column(nullable = false)
    private int diastolic;

    @Column(nullable = false)
    private int pulse;

    private String remark;

    public void updateData(Integer systolic, Integer diastolic, Integer pulse, String remark) {
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.remark = remark;
    }
}