package com.example.bpmanagement.Config;

import com.example.bpmanagement.Service.BloodPressureFeedbackService;
import com.example.bpmanagement.Service.GeminiFeedbackService;
import com.example.bpmanagement.Service.MockFeedbackService;
//import com.example.bpmanagement.Service.OpenAIBloodPressureFeedbackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FeedbackServiceConfig {

    @Value("${feedback.service.type}")
    private String serviceType;

    @Bean
    @Primary
    public BloodPressureFeedbackService feedbackService(
            MockFeedbackService mockService,
            GeminiFeedbackService geminiService) {
        return "gemini".equalsIgnoreCase(serviceType) ? geminiService : mockService;
    }
//            OpenAIBloodPressureFeedbackService openAIService) {
//        return "openai".equalsIgnoreCase(serviceType) ? openAIService : mockService;
//    }
}