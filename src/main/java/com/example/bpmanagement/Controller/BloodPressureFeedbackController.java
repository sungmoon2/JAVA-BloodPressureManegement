package com.example.bpmanagement.Controller;

import com.example.bpmanagement.Service.OpenAIBloodPressureFeedbackService;
import com.example.bpmanagement.Service.BloodPressureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class BloodPressureFeedbackController {

    private final OpenAIBloodPressureFeedbackService feedbackService;
    private final BloodPressureService bloodPressureService;

    @GetMapping("/{period}")
    public ResponseEntity<?> getFeedback(@PathVariable String period) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start;
            String periodName;

            switch (period) {
                case "7d":
                    start = now.minusDays(7);
                    periodName = "최근 7일";
                    break;
                case "3m":
                    start = now.minusMonths(3);
                    periodName = "최근 3개월";
                    break;
                case "6m":
                    start = now.minusMonths(6);
                    periodName = "최근 6개월";
                    break;
                case "1y":
                    start = now.minusYears(1);
                    periodName = "최근 1년";
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid period");
            }

            var data = bloodPressureService.getBloodPressureRecordsByPeriod(start, now);
            String feedback = feedbackService.generateFeedback(data, periodName);

            Map<String, String> response = new HashMap<>();
            response.put("feedback", feedback);
            response.put("period", periodName);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error generating feedback");
        }
    }
}