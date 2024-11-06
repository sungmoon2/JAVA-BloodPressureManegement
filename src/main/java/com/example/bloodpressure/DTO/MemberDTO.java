// 패키지 선언: DTO 클래스는 데이터를 전송하기 위한 객체로 'DTO' 패키지에 포함
package com.example.bloodpressure.DTO;

// Lombok 라이브러리를 사용해 코드 간결화를 위한 애너테이션 import
import com.example.bloodpressure.Entity.Gender;
import lombok.*;  // Lombok에서 제공하는 @Getter, @Setter 등 다양한 애너테이션을 사용하기 위해 패키지 임포트

// 모든 필드에 대한 Getter 메서드를 자동으로 생성해주는 Lombok의 @Getter 애너테이션
@Getter
// 모든 필드에 대한 Setter 메서드를 자동으로 생성해주는 Lombok의 @Setter 애너테이션
@Setter
// 빌더 패턴을 사용해 객체를 생성할 수 있게 해주는 Lombok의 @Builder 애너테이션
@Builder
// 기본 생성자를 자동으로 추가하는 Lombok의 @NoArgsConstructor 애너테이션
@NoArgsConstructor
// 모든 필드를 포함하는 생성자를 자동으로 추가하는 Lombok의 @AllArgsConstructor 애너테이션
@AllArgsConstructor
public class MemberDTO {

    // 사용자 이름 (아이디로 사용될 가능성 있음)
    private String username;

    // 사용자 비밀번호
    private String password;

    // 비밀번호 확인을 위한 필드 (사용자가 비밀번호를 재입력할 때 일치하는지 확인)
    private String passwordConfirm;

    // 사용자 이름
    private String name;

    // 사용자 이메일
    private String email;

    // 사용자 전화번호
    private String phoneNumber;

    // 사용자 나이
    private Integer age;

    // 사용자 키 (단위: cm)
    private Double height;

    // 사용자 체중 (단위: kg)
    private Double weight;

    // 사용자 성별 (예: "M", "F")
    private Gender gender = Gender.UNKNOWN;
}
