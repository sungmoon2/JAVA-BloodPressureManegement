package com.example.bpmanagement.DTO;

import com.example.bpmanagement.Entity.Comment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long boardId;
        private String content;
        private Long parentId;    // 답글인 경우 부모 댓글 ID
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String content;
        private String memberUsername;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isDeleted;
        private Long parentId;
        private boolean isWriter;  // 게시글 작성자와 댓글 작성자가 같은지 여부
        private List<Response> children;  // 답글 목록

        public static Response from(Comment comment, String currentUsername, String boardWriterUsername) {
            if (comment == null) return null;

            return Response.builder()
                    .id(comment.getId())
                    .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                    .memberUsername(comment.getMember().getUsername())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .isDeleted(comment.isDeleted())
                    .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                    .children(comment.getChildren().stream()
                            .map(child -> from(child, currentUsername, boardWriterUsername))
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}