// Member 엔티티를 위한 Repository 인터페이스입니다. 
// 이 인터페이스는 Spring Data JPA의 JpaRepository를 상속하여 CRUD와 관련된 데이터베이스 접근 기능을 제공합니다.

package com.example.bloodpressure.Repository;  // Repository 패키지에 속해 있는 클래스임을 선언

// Member 엔티티를 import 하여 데이터베이스 접근 시 사용
import com.example.bloodpressure.Entity.Member;

// Spring Data JPA에서 제공하는 JpaRepository를 import하여 데이터베이스의 CRUD 작업을 자동으로 지원
import org.springframework.data.jpa.repository.JpaRepository;

// @Query 애너테이션을 사용하여 사용자 정의 JPQL 쿼리를 정의하기 위해 import
import org.springframework.data.jpa.repository.Query;

// @Param 애너테이션을 사용하여 JPQL 쿼리의 매개변수를 설정하기 위해 import
import org.springframework.data.repository.query.Param;

// 이 인터페이스를 Repository로 인식하도록 하는 @Repository 애너테이션을 import
import org.springframework.stereotype.Repository;

import java.util.Optional;  // 데이터가 존재할 수도, 존재하지 않을 수도 있는 경우를 처리하기 위한 Optional 클래스 import

// @Repository 애너테이션을 사용하여 Spring이 이 인터페이스를 데이터 접근 계층의 빈으로 인식하도록 함
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // JpaRepository를 확장하여 기본적인 CRUD 기능과 페이징 및 정렬 기능 제공
    // Member 엔티티와 연관된 데이터 접근을 위해 사용하며, 기본 키의 타입은 Long으로 설정

    // username을 기준으로 회원을 검색, 결과는 Optional로 반환
    Optional<Member> findByUsername(String username);  // 회원의 username을 사용해 데이터베이스에서 특정 회원 검색

    // phoneNumber를 기준으로 회원을 검색, 결과는 Optional로 반환
    Optional<Member> findByPhoneNumber(String phoneNumber);  // 회원의 전화번호를 사용해 특정 회원을 검색

    // email을 기준으로 회원을 검색, 결과는 Optional로 반환
    Optional<Member> findByEmail(String email);  // 회원의 이메일을 사용해 특정 회원을 검색

    // 특정 이메일이 이미 사용 중인지 확인하기 위해 JPQL 쿼리를 사용하여 username을 제외한 다른 사용자가 있는지 검사
    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.username != :username")
    Optional<Member> findByEmailAndUsernameNot(@Param("email") String email, @Param("username") String username);
    // @Query: JPQL을 사용하여 데이터베이스에서 직접 쿼리를 수행
    // @Param: 메서드 매개변수를 JPQL 쿼리에서 사용할 파라미터에 바인딩함

    // 특정 전화번호가 이미 사용 중인지 확인하기 위해 JPQL 쿼리를 사용하여 username을 제외한 다른 사용자가 있는지 검사
    @Query("SELECT m FROM Member m WHERE m.phoneNumber = :phoneNumber AND m.username != :username")
    Optional<Member> findByPhoneNumberAndUsernameNot(@Param("phoneNumber") String phoneNumber, @Param("username") String username);
    // JPQL 쿼리를 통해 특정 회원이 아닌 다른 회원의 중복된 전화번호가 있는지 확인
}
