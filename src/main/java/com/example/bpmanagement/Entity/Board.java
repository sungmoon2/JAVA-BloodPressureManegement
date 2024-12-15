package com.example.bpmanagement.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // files 리스트를 new ArrayList<>()로 초기화
    @Builder.Default  // Builder 패턴 사용시에도 초기화된 값을 유지
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardFile> files = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    private int viewCount;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 파일 추가를 위한 편의 메서드
    public void addFile(BoardFile file) {
        files.add(file);
        file.setBoard(this);
    }

    // 파일 제거를 위한 편의 메서드
    public void removeFile(BoardFile file) {
        files.remove(file);
        file.setBoard(null);
    }

    // 댓글 추가를 위한 편의 메서드
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setBoard(this);
    }

    // 댓글 제거를 위한 편의 메서드
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setBoard(null);
    }
}