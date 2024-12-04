package com.example.bpmanagement.Service;

import com.example.bpmanagement.Service.BloodPressureService.CombinedBloodPressureData;
import java.util.List;

public interface OpenAIBloodPressureFeedbackService {
    String generateFeedback(List<CombinedBloodPressureData> data, String period);
}
// -------------- 이 위 코드는 API Key를 발급받지 않고도 오프라인에서 피드백을 제공 받을 수 있는 mock 서비스 코드임 --------------


//나중에 OpenAI API를 사용하고 싶을 때는:
//
//OpenAIBloodPressureFeedbackService의 주석을 해제
//MockBloodPressureFeedbackService에서 @Primary 어노테이션 제거


// -------------- 이 아래 코드는 OpenAI API Feedback Service Code임 --------------
//package com.example.bpmanagement.Service;
//
//import com.example.bpmanagement.Service.BloodPressureService.CombinedBloodPressureData;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@Slf4j
//@Service
//public class BloodPressureFeedbackService {
//
//    @Value("${openai.api.key}")
//    private String apiKey;
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
//    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//
//    public BloodPressureFeedbackService() {
//        this.restTemplate = new RestTemplate();
//        this.objectMapper = new ObjectMapper();
//    }
//
//    public String generateFeedback(List<CombinedBloodPressureData> data, String period) {
//        try {
//            // 프롬프트 생성
//            StringBuilder prompt = new StringBuilder();
//            prompt.append("다음은 혈압 측정 데이터입니다. 각 데이터를 분석하여 다음 형식으로 피드백을 제공해주세요:\n\n");
//            prompt.append("기간: ").append(period).append("\n");
//            prompt.append("측정 데이터:\n");
//
//            for (CombinedBloodPressureData record : data) {
//                prompt.append(String.format("- 날짜: %s, 수축기: %d, 이완기: %d, 맥박: %d, 비고: %s\n",
//                        record.getMeasureDatetime().format(formatter),
//                        record.getSystolic(),
//                        record.getDiastolic(),
//                        record.getPulse(),
//                        record.getRemark() != null ? record.getRemark() : "없음"));
//            }
//
//            prompt.append("\n다음 형식으로 분석해주세요:\n");
//            prompt.append("1. 주목할 만한 측정값과 그 시점의 특이사항\n");
//            prompt.append("2. 전반적인 혈압 상태 평가\n");
//            prompt.append("3. 개선이 필요한 부분\n");
//            prompt.append("4. 권장 사항 및 생활 수칙\n");
//            prompt.append("\n의료 전문가의 관점에서 자세하게 분석해주세요.");
//
//            // API 요청 본문 생성
//            ObjectNode requestBody = objectMapper.createObjectNode();
//            requestBody.put("model", "gpt-3.5-turbo");
//            ArrayNode messagesArray = requestBody.putArray("messages");
//
//            ObjectNode systemMessage = objectMapper.createObjectNode();
//            systemMessage.put("role", "system");
//            systemMessage.put("content", "당신은 혈압 관리 전문가입니다. 환자의 혈압 데이터를 분석하고 전문적인 조언을 제공합니다.");
//            messagesArray.add(systemMessage);
//
//            ObjectNode userMessage = objectMapper.createObjectNode();
//            userMessage.put("role", "user");
//            userMessage.put("content", prompt.toString());
//            messagesArray.add(userMessage);
//
//            // HTTP 헤더 설정
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setBearerAuth(apiKey);
//
//            // API 요청
//            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
//            String response = restTemplate.postForObject(OPENAI_API_URL, request, String.class);
//
//            // 응답 파싱
//            JsonNode responseNode = objectMapper.readTree(response);
//            return responseNode.get("choices").get(0).get("message").get("content").asText();
//
//        } catch (Exception e) {
//            log.error("피드백 생성 중 오류 발생", e);
//            return "피드백 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
//        }
//    }
//}