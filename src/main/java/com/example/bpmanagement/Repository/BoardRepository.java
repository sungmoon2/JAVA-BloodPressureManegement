package com.example.bpmanagement.Repository;

import com.example.bpmanagement.Entity.Board;
import com.example.bpmanagement.Entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Board 엔티티에 대한 데이터베이스 작업을 처리하는 Spring Data JPA Repository
@Repository // 스프링 컴포넌트로 등록, JPA 리포지토리 역할 수행
public interface BoardRepository extends JpaRepository<Board, Long> {
    // 최신 생성된 게시글 상위 10개를 조회하는 메서드
    List<Board> findTop10ByOrderByCreatedAtDesc();

    // 최신 생성일 기준으로 모든 게시글을 페이지네이션하여 조회하는 메서드
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // BoardRepository에 추가할 메서드
    Page<Board> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);
}
