package com.example.bpmanagement.Service;

import java.util.List;

public interface BloodPressureFeedbackService {
    String generateFeedback(List<BloodPressureService.CombinedBloodPressureData> data, String period);
    String testApiConnection();
}

