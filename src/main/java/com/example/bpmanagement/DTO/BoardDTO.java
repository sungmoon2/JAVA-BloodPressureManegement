package com.example.bpmanagement.DTO;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

// DTO(Data Transfer Object) 클래스 - Board(게시글) 데이터를 전송하기 위한 객체
@Getter // 모든 필드에 대한 getter 메서드 자동 생성
@Setter // 모든 필드에 대한 setter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 초기화하는 생성자 자동 생성
@Builder // 빌더 패턴을 사용하여 객체 생성 가능

public class BoardDTO {
    private Long id; // 게시글 ID (게시글을 고유하게 식별하기 위한 필드)

    private String title; // 게시글 제목

    private String content; // 게시글 내용

    private String memberUsername; // 게시글 작성자의 사용자명 (username)

    private int viewCount; // 게시글 조회수 (해당 게시글이 몇 번 조회되었는지 기록)

    private LocalDateTime createdAt; // 게시글 생성 날짜 및 시간

    private LocalDateTime updatedAt; // 게시글 수정 날짜 및 시간

    private List<MultipartFile> files; // 업로드된 파일 리스트 (사용자가 첨부한 파일 정보를 저장)

    private List<BoardFileDTO> existingFiles; // 기존에 저장된 파일 정보 리스트 (삭제되지 않고 유지될 파일)

    private Integer boardNo; // 게시판 번호 (게시판을 구분하기 위한 필드)

}
