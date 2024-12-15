package com.example.bpmanagement.Repository;

import com.example.bpmanagement.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시글의 최상위 댓글만 조회 (답글 제외)
    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.children WHERE c.board.id = :boardId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findParentCommentsByBoardId(@Param("boardId") Long boardId);

    // 게시글의 모든 댓글 수 조회
    long countByBoardId(Long boardId);
}