// Member.java
package com.example.bpmanagement.Entity;

import jakarta.persistence.*; // JPA 애노테이션 사용을 위한 import
import lombok.*; // Lombok 라이브러리 사용을 위한 import (자동으로 getter, setter 등을 생성)
import com.fasterxml.jackson.annotation.JsonIgnore; // JSON 직렬화에서 특정 필드를 무시하기 위한 import

import java.time.LocalDateTime; // 날짜와 시간을 다루기 위한 import
import java.util.ArrayList; // 리스트 초기화를 위한 import
import java.util.List; // 다수의 BloodPressure 엔티티와 연관 관계를 위한 import

@Entity // JPA에서 이 클래스를 엔티티로 선언
@Getter // Lombok을 사용해 자동으로 getter 메서드 생성
@Setter // Lombok을 사용해 자동으로 setter 메서드 생성
@Builder // Lombok을 사용해 빌더 패턴 적용 가능
@NoArgsConstructor // 파라미터 없는 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드 값을 받는 생성자 자동 생성
@Table(name = "members") // DB의 "members" 테이블과 매핑
public class Member {

    @Id // 기본 키(primary key) 필드 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 값이 자동으로 증가 (IDENTITY 전략 사용)
    private Long id;

    @Column(unique = true, nullable = false) // "username" 필드는 유일하며, null 값 허용 안 됨
    private String username;

    @Column(nullable = false) // "password" 필드는 null 값 허용 안 됨
    private String password;

    @Column // "name" 필드는 null 값을 허용
    private String name;

    @Column // "age" 필드는 null 값을 허용
    private Integer age;

    @Column // "height" 필드는 null 값을 허용
    private Double height;

    @Column // "weight" 필드는 null 값을 허용
    private Double weight;

    @Convert(converter = GenderConverter.class) // Gender 타입을 데이터베이스에 맞게 변환 (GenderConverter 사용)
    private Gender gender;

    @Column(unique = true) // "phoneNumber" 필드는 유일하며, null 값 허용 안 됨
    private String phoneNumber;

    @Column(unique = true) // "email" 필드는 유일하며, null 값 허용 안 됨
    private String email;

    @Column(updatable = false) // "createdAt" 필드는 업데이트할 수 없으며, 레코드가 처음 생성될 때만 값이 할당됨
    private LocalDateTime createdAt;

    @Column // "updatedAt" 필드는 레코드가 수정될 때마다 값이 갱신됨
    private LocalDateTime updatedAt;

    // 양방향 관계 설정 (Member -> BloodPressure)
    @JsonIgnore  // 순환 참조 방지를 위한 설정. JSON 직렬화 시 이 필드는 무시됨
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL) // BloodPressure 엔티티와 양방향 관계 설정
    private List<BloodPressure> bloodPressures = new ArrayList<>(); // Member와 연관된 BloodPressure 목록을 저장

    // 비밀번호 변경 메서드
    public void updatePassword(String newPassword) {
        this.password = newPassword; // 새 비밀번호로 기존 비밀번호를 갱신
    }

    // 회원 정보 업데이트 메서드
    public void updateInfo(String name, String email, String phoneNumber,
                           Integer age, Double height, Double weight, Gender gender) {
        if (name != null) this.name = name; // 이름이 null이 아니면 업데이트
        if (email != null) this.email = email; // 이메일이 null이 아니면 업데이트
        if (phoneNumber != null) this.phoneNumber = phoneNumber; // 전화번호가 null이 아니면 업데이트
        if (age != null) this.age = age; // 나이가 null이 아니면 업데이트
        if (height != null) this.height = height; // 키가 null이 아니면 업데이트
        if (weight != null) this.weight = weight; // 몸무게가 null이 아니면 업데이트
        if (gender != null) {
            this.gender = gender; // 성별이 null이 아니면 업데이트
        } else {
            this.gender = Gender.UNKNOWN; // 성별이 null이면 "UNKNOWN"으로 설정
        }
        this.updatedAt = LocalDateTime.now(); // 정보를 업데이트한 시간 기록
    }

    // 엔티티가 DB에 처음 저장되기 전에 호출되는 메서드
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // 생성된 시간 기록
        updatedAt = LocalDateTime.now(); // 수정된 시간 기록 (처음 저장 시)
    }

    // 엔티티가 DB에서 업데이트되기 전에 호출되는 메서드
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // 수정된 시간 기록
    }
}
