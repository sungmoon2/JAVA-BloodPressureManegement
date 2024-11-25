package com.example.bpmanagement.Service;

import com.example.bpmanagement.DTO.BoardDTO;
import com.example.bpmanagement.DTO.BoardFileDTO;
import com.example.bpmanagement.Entity.Board;
import com.example.bpmanagement.Entity.BoardFile;
import com.example.bpmanagement.Entity.Member;
import com.example.bpmanagement.Repository.BoardFileRepository;
import com.example.bpmanagement.Repository.BoardRepository;
import com.example.bpmanagement.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final MemberRepository memberRepository;

    @Value("${spring.servlet.multipart.location}")
    private String fileDirectory;

    // 게시글 생성
    public Long createPost(BoardDTO boardDTO, String username) throws Exception {
        try {
            log.debug("게시글 생성 시작 - 작성자: {}", username);

            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new Exception("회원을 찾을 수 없습니다."));

            Board board = Board.builder()
                    .title(boardDTO.getTitle())
                    .content(boardDTO.getContent())
                    .member(member)
                    .viewCount(0)
                    .build();

            // 파일 처리
            if (boardDTO.getFiles() != null && !boardDTO.getFiles().isEmpty()) {
                log.debug("파일 처리 시작 - 파일 수: {}", boardDTO.getFiles().size());

                // 업로드 디렉토리 생성
                File uploadDir = new File(fileDirectory);
                if (!uploadDir.exists()) {
                    log.debug("업로드 디렉토리 생성: {}", fileDirectory);
                    uploadDir.mkdirs();
                }

                for (MultipartFile file : boardDTO.getFiles()) {
                    if (!file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                        String filePath = fileDirectory + File.separator + storedFilename;

                        log.debug("파일 저장 시도 - 원본명: {}, 저장명: {}, 경로: {}",
                                originalFilename, storedFilename, filePath);

                        // 파일 저장
                        File dest = new File(filePath);
                        file.transferTo(dest);

                        // 파일 정보 저장
                        BoardFile boardFile = BoardFile.builder()
                                .board(board)
                                .originalFileName(originalFilename)
                                .storedFileName(storedFilename)
                                .filePath("/uploads/" + storedFilename)  // URL 경로로 저장
                                .fileSize(file.getSize())
                                .fileType(file.getContentType())
                                .build();

                        board.addFile(boardFile);
                        log.debug("파일 정보 저장 완료 - 파일명: {}", originalFilename);
                    }
                }
            }

            boardRepository.save(board);
            log.debug("게시글 저장 완료 - 게시글 ID: {}", board.getId());

            return board.getId();

        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생", e);
            throw new Exception("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("게시글 저장 중 오류 발생", e);
            throw new Exception("게시글 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 게시글 상세 조회
    @Transactional
    public BoardDTO getPost(Long id) throws Exception {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception("게시글을 찾을 수 없습니다."));

        // 조회수 증가
        board.setViewCount(board.getViewCount() + 1);
        boardRepository.saveAndFlush(board);  // 즉시 저장 및 동기화

        return convertToDTO(board);
    }

    // 게시글 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<BoardDTO> getPostList(Pageable pageable) {
        Page<Board> boardPage = boardRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 전체 게시글 수 조회
        long totalElements = boardPage.getTotalElements();

        return boardPage.map(board -> {
            BoardDTO dto = convertToDTO(board);
            // 가상 번호 = 전체 게시글 수 - (현재 페이지 * 페이지 크기 + 현재 페이지 내에서의 위치)
            int boardNo = (int) (totalElements - (pageable.getPageNumber() * pageable.getPageSize()
                    + boardPage.getContent().indexOf(board)));
            dto.setBoardNo(boardNo);
            return dto;
        });
    }

    // 최근 게시글 조회
    @Transactional(readOnly = true)
    public List<BoardDTO> getRecentPosts(int count) {
        return boardRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(board -> BoardDTO.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .memberUsername(board.getMember().getUsername())
                        .createdAt(board.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 게시글 수정
    public void updatePost(Long id, BoardDTO boardDTO, String username) throws Exception {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception("게시글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!board.getMember().getUsername().equals(username)) {
            throw new Exception("게시글 수정 권한이 없습니다.");
        }

        // 기본 정보 수정
        board.setTitle(boardDTO.getTitle());
        board.setContent(boardDTO.getContent());

        // 새로운 파일 추가
        if (boardDTO.getFiles() != null && !boardDTO.getFiles().isEmpty()) {
            for (MultipartFile file : boardDTO.getFiles()) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                    String filePath = fileDirectory + File.separator + storedFilename;

                    // 파일 저장
                    File dest = new File(filePath);
                    file.transferTo(dest);

                    BoardFile boardFile = BoardFile.builder()
                            .board(board)
                            .originalFileName(originalFilename)
                            .storedFileName(storedFilename)
                            .filePath("/uploads/" + storedFilename)  // URL 경로로 저장
                            .fileSize(file.getSize())
                            .fileType(file.getContentType())
                            .build();

                    board.getFiles().add(boardFile);
                }
            }
        }

        boardRepository.save(board);
    }

    // 게시글 삭제
    public void deletePost(Long id, String username) throws Exception {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception("게시글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!board.getMember().getUsername().equals(username)) {
            throw new Exception("게시글 삭제 권한이 없습니다.");
        }

        // 첨부 파일 삭제
        for (BoardFile file : board.getFiles()) {
            File physicalFile = new File(fileDirectory + File.separator + file.getStoredFileName());
            if (physicalFile.exists()) {
                physicalFile.delete();
            }
        }

        boardRepository.delete(board);
    }

    // 파일 다운로드를 위한 파일 정보 조회
    @Transactional(readOnly = true)
    public BoardFile getFile(Long fileId) throws Exception {
        return boardFileRepository.findById(fileId)
                .orElseThrow(() -> new Exception("파일을 찾을 수 없습니다."));
    }

    // Entity를 DTO로 변환
    private BoardDTO convertToDTO(Board board) {
        List<BoardFileDTO> fileList = board.getFiles().stream()
                .map(file -> {
                    String filePath = String.format("/uploads/%s", file.getStoredFileName());
                    return BoardFileDTO.builder()
                            .id(file.getId())
                            .originalFileName(file.getOriginalFileName())
                            .storedFileName(file.getStoredFileName())
                            .filePath(filePath)
                            .fileType(file.getFileType())
                            .fileSize(file.getFileSize())
                            .build();
                })
                .collect(Collectors.toList());

        return BoardDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .memberUsername(board.getMember().getUsername())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .existingFiles(fileList)
                .build();
    }
}