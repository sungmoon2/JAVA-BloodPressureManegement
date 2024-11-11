package com.example.bpmanagement.Entity;

// Gender는 성별을 나타내는 열거형(enum) 클래스입니다.
// 성별 정보는 기본적으로 고정된 값을 가지므로 Enum을 사용하여 값을 명확히 정의합니다.
public enum Gender {
    // 'MALE'은 남성 성별을 나타내며, 'FEMALE'은 여성 성별을 나타냅니다.
    // 'UNKNOWN'은 성별이 정의되지 않았거나 알 수 없는 상태를 나타냅니다.
    MALE("남"),   // '남'은 'MALE'을 표현하는 라벨 값입니다.
    FEMALE("여"), // '여'는 'FEMALE'을 표현하는 라벨 값입니다.
    UNKNOWN("알 수 없음"); // '알 수 없음'은 성별 정보가 없는 상태입니다.

    private String label; // 성별을 저장하는 라벨 (DB에 저장될 값)

    // 생성자: enum 값을 초기화할 때 라벨 값을 설정합니다.
    Gender(String label) {
        this.label = label;
    }

    // 라벨 값을 반환하는 메서드 (DB에서 가져온 성별 값을 매핑할 때 사용)
    public String getLabel() {
        return label;
    }


}
