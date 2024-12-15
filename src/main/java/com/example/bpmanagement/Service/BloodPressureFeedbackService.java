package com.example.bpmanagement.Service;

import java.util.List;

// 혈압 데이터 분석 및 피드백 생성을 위한 서비스 인터페이스
public interface BloodPressureFeedbackService {

    /**
     * 혈압 데이터를 분석하여 지정된 기간(period)에 대한 피드백을 생성합니다.
     *
     * @param data 혈압 데이터 리스트 (CombinedBloodPressureData 타입의 데이터)
     *             - systolic, diastolic, pulse 등의 혈압 정보를 포함.
     * @param period 기간을 나타내는 문자열 (예: "daily", "weekly", "monthly")
     *               - 분석 기간에 따라 피드백을 다르게 생성.
     * @return 분석 결과를 바탕으로 생성된 피드백 문자열.
     */
    String generateFeedback(List<BloodPressureService.CombinedBloodPressureData> data, String period);

    /**
     * API 연결 상태를 테스트하기 위한 메서드입니다.
     *
     * @return API 연결 테스트 결과 메시지 (예: "Connection successful").
     */
    String testApiConnection();
}
