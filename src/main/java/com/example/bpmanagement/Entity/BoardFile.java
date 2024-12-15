package com.example.bpmanagement.Entity;

import jakarta.persistence.*;
import lombok.*;

// 게시글에 첨부된 파일 데이터를 저장하기 위한 JPA 엔티티 클래스
@Entity // JPA에서 관리하는 엔티티임을 나타냄
@Getter // 모든 필드에 대한 getter 메서드를 자동 생성
@Setter // 모든 필드에 대한 setter 메서드를 자동 생성
@NoArgsConstructor // 기본 생성자를 자동 생성
@AllArgsConstructor // 모든 필드를 초기화하는 생성자를 자동 생성
@Builder // 빌더 패턴을 사용하여 객체 생성 가능
@Table(name = "board_files") // 데이터베이스에서 "board_files"라는 테이블과 매핑
public class BoardFile {

    @Id // 기본 키(Primary Key) 필드로 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 기본 키 값을 데이터베이스가 자동 생성 (Auto Increment)
    private Long id; // 파일 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    // 다대일 관계 설정: 여러 파일이 하나의 게시글(Board)에 연결됨
    @JoinColumn(name = "board_id")
    // "board_id"라는 외래 키(Foreign Key)를 통해 Board 테이블과 연결
    private Board board;
    // 이 파일이 연결된 게시글 정보

    @Column(nullable = false)
    // 원본 파일 이름은 null 값을 허용하지 않음
    private String originalFileName;
    // 업로드된 파일의 원본 이름 (예: example.jpg)

    @Column(nullable = false)
    // 저장된 파일 이름은 null 값을 허용하지 않음
    private String storedFileName;
    // 서버에 저장된 파일 이름 (예: unique_id_example.jpg)

    @Column(nullable = false)
    // 파일 경로는 null 값을 허용하지 않음
    private String filePath;
    // 서버 내 파일 저장 경로 (예: /uploads/2024/12/)

    private Long fileSize;
    // 파일 크기 (단위: 바이트), null 값을 허용함

    @Column(nullable = false)
    // 파일 MIME 타입은 null 값을 허용하지 않음
    private String fileType;
    // 파일의 MIME 타입 (예: image/jpeg, application/pdf 등)
}
