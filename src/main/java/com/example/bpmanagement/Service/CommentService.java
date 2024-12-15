package com.example.bpmanagement.Service;

import com.example.bpmanagement.DTO.CommentDTO;
import com.example.bpmanagement.Entity.Board;
import com.example.bpmanagement.Entity.Comment;
import com.example.bpmanagement.Entity.Member;
import com.example.bpmanagement.Repository.BoardRepository;
import com.example.bpmanagement.Repository.CommentRepository;
import com.example.bpmanagement.Repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    // 댓글 목록 조회
    public List<CommentDTO.Response> getComments(Long boardId, String currentUsername) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));

        List<Comment> parentComments = commentRepository.findParentCommentsByBoardId(boardId);
        return parentComments.stream()
                .map(comment -> CommentDTO.Response.from(comment, currentUsername, board.getMember().getUsername()))
                .collect(Collectors.toList());
    }

    // 댓글 작성
    @Transactional
    public CommentDTO.Response createComment(CommentDTO.Request requestDto, String username) {
        Board board = boardRepository.findById(requestDto.getBoardId())
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        Comment parent = null;
        if (requestDto.getParentId() != null) {
            parent = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .board(board)
                .member(member)
                .parent(parent)
                .build();

        if (parent != null) {
            parent.addChild(comment);
        }

        Comment savedComment = commentRepository.save(comment);
        return CommentDTO.Response.from(savedComment, username, board.getMember().getUsername());
    }

    // 댓글 수정
    @Transactional
    public CommentDTO.Response updateComment(Long commentId, CommentDTO.Request requestDto, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getMember().getUsername().equals(username)) {
            throw new IllegalStateException("You don't have permission to update this comment");
        }

        comment.setContent(requestDto.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return CommentDTO.Response.from(updatedComment, username, comment.getBoard().getMember().getUsername());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getMember().getUsername().equals(username)) {
            throw new IllegalStateException("You don't have permission to delete this comment");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }
}