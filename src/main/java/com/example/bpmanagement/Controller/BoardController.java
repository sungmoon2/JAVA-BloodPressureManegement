package com.example.bpmanagement.Controller;

import com.example.bpmanagement.DTO.BoardDTO;
import com.example.bpmanagement.Entity.BoardFile;
import com.example.bpmanagement.Service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Value;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import java.util.List;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @Value("${spring.servlet.multipart.location}")
    private String fileDirectory;


    // 글쓰기 페이지 보여주기
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("boardDTO", new BoardDTO());
        return "board/write";
    }

    // 글쓰기 처리
    @PostMapping("/write")
    public String write(@ModelAttribute BoardDTO boardDTO,
                        @RequestParam(value = "files", required = false) List<MultipartFile> files,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        try {
            // 파일이 있는 경우에만 DTO에 설정
            if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
                boardDTO.setFiles(files);
            }

            Long boardId = boardService.createPost(boardDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 등록되었습니다.");
            return "redirect:/board/view/" + boardId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "게시글 등록에 실패했습니다: " + e.getMessage());
            return "redirect:/board/write";
        }
    }

    // 게시글 상세 보기
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            BoardDTO board = boardService.getPost(id);
            model.addAttribute("board", board);
            return "board/view";
        } catch (Exception e) {
            return "redirect:/board/list";
        }
    }

    // 게시글 목록
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<BoardDTO> boardPage = boardService.getPostList(PageRequest.of(page, size));

        model.addAttribute("boards", boardPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", boardPage.getTotalPages());

        return "board/list";
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws Exception {
        BoardFile boardFile = boardService.getFile(fileId);
        // 전체 파일 경로 생성
        Path filePath = Paths.get(fileDirectory, boardFile.getStoredFileName());
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            throw new Exception("파일을 찾을 수 없습니다");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(boardFile.getOriginalFileName(), StandardCharsets.UTF_8)
                .build());
        headers.add(HttpHeaders.CONTENT_TYPE, boardFile.getFileType());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    // 게시글 삭제
    @GetMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            boardService.deletePost(id, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
            return "redirect:/board/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/board/view/" + id;
        }
    }

    // 게시글 수정 페이지로 이동
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            BoardDTO board = boardService.getPost(id);
            // 작성자 확인
            if (!board.getMemberUsername().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
                return "redirect:/board/view/" + id;
            }
            model.addAttribute("board", board);
            return "board/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/board/list";
        }
    }

    // 게시글 수정 처리
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute BoardDTO boardDTO,
                       @RequestParam(required = false) List<MultipartFile> files,
                       Authentication authentication,
                       RedirectAttributes redirectAttributes) {
        try {
            boardDTO.setFiles(files);
            boardService.updatePost(id, boardDTO, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
            return "redirect:/board/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/board/edit/" + id;
        }
    }
}