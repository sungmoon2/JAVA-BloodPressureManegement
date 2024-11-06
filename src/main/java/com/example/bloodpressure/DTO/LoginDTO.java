// 패키지 선언: 이 클래스는 DTO (Data Transfer Object)로, 'DTO'라는 패키지에 포함되어 있습니다.
// DTO는 계층 간 데이터를 전달하는 객체로 사용됩니다.
package com.example.bloodpressure.DTO;

// Lombok의 @Data 애너테이션을 임포트합니다.
// @Data는 Lombok에서 제공하는 애너테이션으로, 아래 클래스에 대해 
// 1. getter, setter 메서드 자동 생성
// 2. toString(), equals(), hashCode() 메서드 자동 생성
// 3. 생성자 생성 (매개변수가 없는 기본 생성자, 모든 필드를 매개변수로 받는 생성자)
// 등 여러 메서드를 자동으로 생성해줍니다.
import lombok.Data;

// LoginDTO 클래스 선언: 로그인 관련 정보를 전송하기 위한 DTO 클래스입니다.
// 이 클래스는 사용자 이름(username)과 비밀번호(password)만 포함됩니다.
@Data
public class LoginDTO {

    // 사용자 이름을 나타내는 필드입니다.
    // 이 필드는 사용자가 로그인할 때 입력한 사용자 이름(아이디)을 저장합니다.
    // 예를 들어, "john_doe"와 같은 값을 가질 수 있습니다.
    private String username;

    // 비밀번호를 나타내는 필드입니다.
    // 이 필드는 사용자가 로그인할 때 입력한 비밀번호를 저장합니다.
    // 예를 들어, "password123"과 같은 값을 가질 수 있습니다.
    private String password;
}
