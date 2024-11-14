package com.example.bpmanagement.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bloodpressuredata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloodPressureData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "measure_datetime", nullable = false)
    private LocalDateTime measureDatetime;

    @Column(nullable = false)
    private int systolic;

    @Column(nullable = false)
    private int diastolic;

    @Column(nullable = false)
    private int pulse;

    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}