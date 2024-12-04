package com.example.bpmanagement.Service;

import com.example.bpmanagement.Service.BloodPressureService.CombinedBloodPressureData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiFeedbackService implements BloodPressureFeedbackService {



    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public GeminiFeedbackService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateFeedback(List<CombinedBloodPressureData> data, String period) {

        try {
            // 데이터 최적화
            List<CombinedBloodPressureData> limitedData = limitDataForAnalysis(data);
            String prompt = createPrompt(limitedData, period);

            // API 요청 본문 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();
            ArrayNode parts = objectMapper.createArrayNode();

            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", prompt);
            parts.add(part);

            content.set("parts", parts);
            contents.add(content);
            requestBody.set("contents", contents);

            // 디버그 로그 추가
            log.debug("Request Body: {}", requestBody.toString());
            log.debug("API URL: {}", GEMINI_API_URL + apiKey);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // API 요청
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GEMINI_API_URL + apiKey,
                    request,
                    String.class
            );

            // 응답 파싱
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            log.debug("API Response: {}", responseNode.toString());

            // 응답 처리
            if (responseNode.has("candidates") && responseNode.get("candidates").isArray()
                    && responseNode.get("candidates").size() > 0) {
                JsonNode candidate = responseNode.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")
                        && candidate.get("content").get("parts").isArray()
                        && candidate.get("content").get("parts").size() > 0) {
                    String rawResponse = candidate.get("content").get("parts").get(0).get("text").asText();
                    return formatResponse(rawResponse);
                }
            }
            return "피드백 생성 오류: 예상한 응답을 받지 못했습니다.";

        } catch (Exception e) {
            log.error("피드백 생성 중 오류 발생", e);
            return "피드백 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private List<CombinedBloodPressureData> limitDataForAnalysis(List<CombinedBloodPressureData> data) {
        List<CombinedBloodPressureData> significantData = new ArrayList<>();

        // 중요 데이터 선택 (고혈압, 저혈압, 스트레스 상황)
        List<CombinedBloodPressureData> abnormalData = data.stream()
                .filter(bp -> bp.getRemark() != null &&
                        (bp.getRemark().contains("고혈압") ||
                                bp.getRemark().contains("저혈압") ||
                                bp.getRemark().contains("스트레스"))).limit(5)
                .collect(Collectors.toList());
        significantData.addAll(abnormalData);

        // 최근 데이터 추가
        if (significantData.size() < 10) {
            List<CombinedBloodPressureData> recentData = data.stream()
                    .sorted(Comparator.comparing(CombinedBloodPressureData::getMeasureDatetime).reversed())
                    .limit(10 - significantData.size())
                    .collect(Collectors.toList());
            significantData.addAll(recentData);
        }

        return significantData;
    }

    private String createPrompt(List<CombinedBloodPressureData> data, String period) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("숙련된 고혈압 전문의의 관점에서 다음 혈압 데이터를 분석해 주세요.\n\n");
        prompt.append("분석 기간: ").append(period).append("\n\n");

        // 통계 정보를 더 상세하게
        DoubleSummaryStatistics systolicStats = data.stream()
                .mapToDouble(CombinedBloodPressureData::getSystolic)
                .summaryStatistics();
        DoubleSummaryStatistics diastolicStats = data.stream()
                .mapToDouble(CombinedBloodPressureData::getDiastolic)
                .summaryStatistics();
        DoubleSummaryStatistics pulseStats = data.stream()
                .mapToDouble(CombinedBloodPressureData::getPulse)
                .summaryStatistics();

        prompt.append("통계 정보:\n");
        prompt.append(String.format("수축기 혈압: 평균 %.1f mmHg (최저 %.1f, 최고 %.1f)\n",
                systolicStats.getAverage(), systolicStats.getMin(), systolicStats.getMax()));
        prompt.append(String.format("이완기 혈압: 평균 %.1f mmHg (최저 %.1f, 최고 %.1f)\n",
                diastolicStats.getAverage(), diastolicStats.getMin(), diastolicStats.getMax()));
        prompt.append(String.format("맥박: 평균 %.1f bpm (최저 %.1f, 최고 %.1f)\n\n",
                pulseStats.getAverage(), pulseStats.getMin(), pulseStats.getMax()));

        // 주요 측정값 (시간대별 패턴 포함)
        prompt.append("주요 측정값:\n");
        data.forEach(bp ->
                prompt.append(String.format("- %s: %d/%d mmHg, 맥박 %d, 상태: %s\n",
                        bp.getMeasureDatetime().format(formatter),
                        bp.getSystolic(),
                        bp.getDiastolic(),
                        bp.getPulse(),
                        bp.getRemark() != null ? bp.getRemark() : "일반"
                ))
        );

        prompt.append("\n다음 항목들에 대해 상세히 분석해주세요:\n");
        prompt.append("1. 전반적인 혈압 상태 분석\n");
        prompt.append("   - 현재 혈압의 심각성 정도\n");
        prompt.append("   - 고혈압/저혈압 위험도 평가\n");
        prompt.append("   - 혈압 변동성 분석\n");
        prompt.append("   - 맥박 상태 평가\n\n");

        prompt.append("2. 주목할 만한 패턴 분석\n");
        prompt.append("   - 시간대별 혈압 변화\n");
        prompt.append("   - 이상수치 발생 패턴\n");
        prompt.append("   - 스트레스와 혈압의 관계\n\n");

        prompt.append("3. 건강 위험 평가\n");
        prompt.append("   - 현재 상태가 건강에 미치는 영향\n");
        prompt.append("   - 잠재적 위험 요소\n");
        prompt.append("   - 긴급한 의료 상담이 필요한 징후\n\n");

        prompt.append("4. 맞춤형 생활습관 개선 방안\n");
        prompt.append("   - 식사 관리 방법\n");
        prompt.append("   - 운동 추천 (종류, 강도, 시간)\n");
        prompt.append("   - 스트레스 관리 전략\n");
        prompt.append("   - 수면 관리 방법\n\n");

        prompt.append("5. 장기적 관리 계획\n");
        prompt.append("   - 단계별 혈압 개선 목표\n");
        prompt.append("   - 정기적인 모니터링 방법\n");
        prompt.append("   - 의료진과 상담이 필요한 시점\n\n");

        prompt.append("환자의 건강과 삶의 질 향상을 위해, 실천 가능하고 구체적인 조언을 의학적 근거와 함께 제시해주세요. ");
        prompt.append("전문적이면서도 이해하기 쉽게 설명해주시고, 가능하다면 연구 결과나 의학적 통계도 포함해주세요.\n");

        return prompt.toString();
    }

    private String formatResponse(String response) {
        // 기본적인 HTML 스타일 추가
        StringBuilder formatted = new StringBuilder();
        formatted.append("<div style='font-family: Arial, sans-serif; line-height: 1.6;'>");

        // 마크다운 스타일의 볼드 텍스트 변환
        String htmlText = response.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");

        // 줄바꿈 처리
        htmlText = htmlText.replaceAll("\n\n", "</p><p>");
        htmlText = htmlText.replaceAll("\n", "<br>");

        // 불릿 포인트 처리
        htmlText = htmlText.replaceAll("\\* ", "• ");

        formatted.append("<p>").append(htmlText).append("</p></div>");

        return formatted.toString();
    }

    @Override
    public String testApiConnection() {
        try {
            // 요청 본문 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.putArray("contents")
                    .addObject()
                    .put("role", "user")
                    .putArray("parts")
                    .add("Say hello"); // 수정된 형식

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // API 요청
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GEMINI_API_URL + apiKey,
                    request,
                    String.class
            );

            // 응답 파싱
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            return responseNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            log.error("API 테스트 중 오류 발생", e);
            return "Error: " + e.getMessage();
        }
    }
}
