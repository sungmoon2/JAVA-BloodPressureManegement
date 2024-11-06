// 패키지 선언: 엔티티 클래스는 주로 도메인 모델을 나타내므로 'Entity' 패키지에 포함
package com.example.bloodpressure.Entity;

// JPA 애너테이션 관련 라이브러리를 사용하기 위해 필요한 import 문
import jakarta.persistence.*;  // JPA에서 제공하는 애너테이션(@Entity, @Id 등)을 사용하기 위한 패키지

// Lombok 라이브러리를 사용해 코드 간결화를 위한 애너테이션 import
import lombok.*;  // Lombok에서 제공하는 @Getter, @Builder 등 다양한 애너테이션을 사용하기 위해 패키지 임포트

// 날짜와 시간을 다루기 위한 LocalDateTime 클래스 import
import java.time.LocalDateTime;  // 엔티티의 생성일자와 수정일자에 사용되는 LocalDateTime 클래스를 사용하기 위해 임포트

// JPA 엔티티 클래스임을 나타내는 @Entity 애너테이션
@Entity
// 클래스 내 모든 필드에 대한 Getter 메서드를 자동으로 생성해주는 Lombok의 @Getter 애너테이션
@Getter
// 빌더 패턴을 사용해 객체를 생성할 수 있게 해주는 Lombok의 @Builder 애너테이션
@Builder
// 기본 생성자를 자동으로 추가하는 Lombok의 @NoArgsConstructor 애너테이션
@NoArgsConstructor
// 모든 필드를 포함하는 생성자를 자동으로 추가하는 Lombok의 @AllArgsConstructor 애너테이션
@AllArgsConstructor
// 테이블 이름을 "members"로 지정하는 @Table 애너테이션
@Table(name = "members")
public class Member {

    // 기본 키 필드. @Id는 필드가 테이블의 기본 키임을 나타냄
    @Id
    // 기본 키 값 자동 생성 전략을 IDENTITY로 지정. 데이터베이스가 키 값을 자동 생성하도록 함
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 회원의 고유 ID. DB에서 자동으로 생성됨

    // unique 제약 조건을 적용하여 username이 중복되지 않도록 하고, null을 허용하지 않음
    @Column(unique = true, nullable = false)
    private String username;  // 회원의 유일한 사용자명. 로그인 시 사용

    // null을 허용하지 않음. 패스워드는 암호화된 문자열로 저장
    @Column(nullable = false)
    private String password;  // 암호화된 회원 비밀번호

    @Column  // 이름은 필수 필드가 아니므로 nullable을 명시하지 않음
    private String name;  // 회원의 이름

    @Column  // 나이도 선택적 정보로 설정
    private Integer age;  // 회원의 나이

    @Column  // 키 정보. 선택적 정보로 설정
    private Double height;  // 회원의 키(단위: cm)

    @Column  // 체중 정보. 선택적 정보로 설정
    private Double weight;  // 회원의 체중(단위: kg)

    // Gender 필드는 'GenderConverter'를 사용하여 enum을 데이터베이스에 저장할 때 '남', '여', '알 수 없음'으로 변환하고,
    // 데이터베이스에서 조회할 때는 다시 'MALE', 'FEMALE', 'UNKNOWN' 값을 가져옵니다.
    @Convert(converter = GenderConverter.class) // GenderConverter를 사용하여 자동 변환
    private Gender gender; // 성별을 나타내는 Gender enum 필드

    // 전화번호는 unique 제약 조건을 추가하여 중복되지 않도록 설정
    @Column(unique = true)
    private String phoneNumber;  // 회원의 전화번호. 중복되지 않도록 설정

    // 이메일도 unique 제약 조건을 추가하여 중복되지 않도록 설정
    @Column(unique = true)
    private String email;  // 회원의 이메일 주소

    // 회원 생성 시에만 설정되며, 수정되지 않도록 updatable을 false로 설정
    @Column(updatable = false)
    private LocalDateTime createdAt;  // 회원 가입일

    @Column  // 수정일은 수정 시에만 업데이트되므로 기본 설정
    private LocalDateTime updatedAt;  // 회원 정보 수정일

    // 비밀번호를 업데이트하는 메서드로, 비밀번호 필드를 새로운 값으로 변경
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // 회원 정보를 업데이트하는 메서드. 전달받은 값이 null이 아닐 때만 해당 필드를 갱신
    public void updateInfo(String name, String email, String phoneNumber,
                           Integer age, Double height, Double weight, Gender gender) {
        if (name != null) this.name = name;  // 이름이 null이 아닐 경우 이름 업데이트
        if (email != null) this.email = email;  // 이메일이 null이 아닐 경우 이메일 업데이트
        if (phoneNumber != null) this.phoneNumber = phoneNumber;  // 전화번호가 null이 아닐 경우 전화번호 업데이트
        if (age != null) this.age = age;  // 나이가 null이 아닐 경우 나이 업데이트
        if (height != null) this.height = height;  // 키가 null이 아닐 경우 키 업데이트
        if (weight != null) this.weight = weight;  // 체중이 null이 아닐 경우 체중 업데이트
        if (gender != null) {
            this.gender = gender;
        } else {
            this.gender = Gender.UNKNOWN;  // gender가 null일 경우 기본 값 설정
        }



        this.updatedAt = LocalDateTime.now();  // 수정 시각을 현재 시각으로 갱신
    }

    // 엔티티가 최초 저장되기 전 호출되는 메서드. createdAt과 updatedAt 필드를 현재 시간으로 설정
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();  // 생성 시각을 현재 시각으로 설정
        updatedAt = LocalDateTime.now();  // 수정 시각도 현재 시각으로 초기화
    }

    // 엔티티가 업데이트되기 전 호출되는 메서드. updatedAt 필드를 현재 시간으로 갱신
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();  // 수정 시각을 현재 시각으로 갱신
    }
}
