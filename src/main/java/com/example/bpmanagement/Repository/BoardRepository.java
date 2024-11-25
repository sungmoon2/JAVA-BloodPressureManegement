package com.example.bpmanagement.Repository;

import com.example.bpmanagement.Entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;  // 이 부분이 수정됨
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findTop10ByOrderByCreatedAtDesc();
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);
}