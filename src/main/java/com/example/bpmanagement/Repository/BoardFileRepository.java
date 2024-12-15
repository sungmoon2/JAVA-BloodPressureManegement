package com.example.bpmanagement.Repository;

import com.example.bpmanagement.Entity.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// BoardFile 엔티티에 대한 데이터베이스 작업을 처리하는 Spring Data JPA Repository
@Repository // 스프링 컴포넌트로 등록, JPA 리포지토리 역할 수행
public interface BoardFileRepository extends JpaRepository<BoardFile, Long> {
    // JpaRepository<엔티티, 기본키 타입>을 상속받아 CRUD 및 페이징 기능 지원
    // 기본적인 CRUD 메서드는 별도로 구현할 필요 없음 (Spring Data JPA가 자동 생성)
}
