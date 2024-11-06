package com.example.bloodpressure.Entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// GenderConverter는 AttributeConverter 인터페이스를 구현한 클래스입니다.
// 이 클래스는 엔티티의 Gender enum 값을 데이터베이스의 String 값으로 변환하거나, 그 반대로 변환하는 로직을 담당합니다.

@Converter // 이 클래스가 JPA 변환기에 의해 자동으로 인식되도록 지정
public class GenderConverter implements AttributeConverter<Gender, String> {

    // convertToDatabaseColumn은 Gender enum 값을 데이터베이스에 저장할 수 있는 String 값으로 변환합니다.
    @Override
    public String convertToDatabaseColumn(Gender gender) {
        if (gender == null) {
            return null; // 성별이 null이면 데이터베이스에 저장할 값은 null
        }
        // '남', '여', '알 수 없음'을 각각 "MALE", "FEMALE", "UNKNOWN"에 대응시키기 위해 getLabel() 사용
        return gender.getLabel(); // gender.getLabel()은 enum에서 정의된 성별 라벨(예: '남', '여', '알 수 없음')을 반환
    }

    // convertToEntityAttribute는 데이터베이스에서 가져온 String 값을 Gender enum 값으로 변환합니다.
    @Override
    public Gender convertToEntityAttribute(String dbData) {
        // 데이터베이스에서 가져온 값이 '남', '여', '알 수 없음'일 경우 해당 enum 값으로 변환
        for (Gender gender : Gender.values()) {
            // dbData가 '남', '여', '알 수 없음'일 경우 일치하는 enum 값으로 변환
            if (gender.getLabel().equals(dbData)) {
                return gender;
            }
        }
        // dbData가 '남', '여', '알 수 없음'에 해당하지 않으면 기본값으로 UNKNOWN을 반환
        return Gender.UNKNOWN; // 만약에 DB 값이 알 수 없는 값이라면 기본값으로 UNKNOWN 반환
    }
}
