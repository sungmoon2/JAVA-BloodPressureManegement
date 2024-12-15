package com.example.bpmanagement.DTO;

import lombok.*;

// 파일 정보를 전송하기 위한 DTO(Data Transfer Object) 클래스
@Getter // 모든 필드에 대한 getter 메서드 자동 생성
@Setter // 모든 필드에 대한 setter 메서드 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 초기화하는 생성자 자동 생성
@Builder // 빌더 패턴을 사용하여 객체 생성 가능

public class BoardFileDTO {
    private Long id; // 파일 ID (고유하게 파일을 식별하기 위한 필드)

    private String originalFileName; // 원본 파일 이름 (사용자가 업로드한 파일의 원래 이름)

    private String storedFileName; // 저장된 파일 이름 (서버에 저장된 파일의 이름)

    private String filePath; // 파일 경로 (파일이 저장된 위치를 나타내는 경로)

    private String fileType; // 파일 유형 (예: "image/png", "application/pdf" 등)

    private Long fileSize; // 파일 크기 (파일의 바이트 단위 크기)
}
