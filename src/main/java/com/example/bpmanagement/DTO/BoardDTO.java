package com.example.bpmanagement.DTO;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {
    private Long id;
    private String title;
    private String content;
    private String memberUsername;  // 작성자 username
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MultipartFile> files;  // 파일 업로드를 위한 필드
    private List<BoardFileDTO> existingFiles;  // 기존 파일 정보
    private Integer boardNo;
}