package com.example.bpmanagement.Controller;

import com.example.bpmanagement.DTO.CommentDTO;
import com.example.bpmanagement.Service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentDTO.Response>> getComments(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<CommentDTO.Response> comments = commentService.getComments(boardId, userDetails.getUsername());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<CommentDTO.Response> createComment(
            @RequestBody CommentDTO.Request requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommentDTO.Response response = commentService.createComment(requestDto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO.Response> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentDTO.Request requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommentDTO.Response response = commentService.updateComment(commentId, requestDto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}