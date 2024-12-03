package com.example.bpmanagement.Service;

import com.example.bpmanagement.Service.BloodPressureService.CombinedBloodPressureData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
public class MockBloodPressureFeedbackService implements OpenAIBloodPressureFeedbackService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String generateFeedback(List<CombinedBloodPressureData> data, String period) {
        try {
            StringBuilder feedback = new StringBuilder();
            feedback.append("[ 혈압 데이터 분석 보고서 ]\n");
            feedback.append("분석 기간: ").append(period).append("\n\n");

            // 1. 기본 통계 분석
            appendBasicStats(feedback, data);

            // 2. 상황별 혈압 패턴 분석
            appendPatternAnalysis(feedback, data);

            // 3. 전체 평가
            appendOverallAssessment(feedback, data);

            // 4. 관리 권장사항
            appendManagementRecommendations(feedback, data);

            return feedback.toString();

        } catch (Exception e) {
            log.error("피드백 생성 중 오류 발생", e);
            return "피드백 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private void appendBasicStats(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        DoubleSummaryStatistics systolicStats = data.stream()
                .mapToDouble(CombinedBloodPressureData::getSystolic)
                .summaryStatistics();
        DoubleSummaryStatistics diastolicStats = data.stream()
                .mapToDouble(CombinedBloodPressureData::getDiastolic)
                .summaryStatistics();

        feedback.append("1. 기본 통계 분석\n");
        feedback.append(String.format("⦁ 수축기 혈압: %.1f-%.1f mmHg (평균: %.1f mmHg)\n",
                systolicStats.getMin(), systolicStats.getMax(), systolicStats.getAverage()));
        feedback.append(String.format("⦁ 이완기 혈압: %.1f-%.1f mmHg (평균: %.1f mmHg)\n",
                diastolicStats.getMin(), diastolicStats.getMax(), diastolicStats.getAverage()));
        feedback.append("\n");
    }

    private void appendPatternAnalysis(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        feedback.append("2. 상황별 혈압 패턴 분석\n");

        // 시간대별 데이터 분류
        Map<String, List<CombinedBloodPressureData>> categoryData = new HashMap<>();
        for (CombinedBloodPressureData bp : data) {
            String timeCategory = categorizeTimeAndRemark(bp);
            categoryData.computeIfAbsent(timeCategory, k -> new ArrayList<>()).add(bp);
        }

        // 기상 시 혈압
        appendCategoryAnalysis(feedback, categoryData.get("기상"), "기상 후 혈압",
                "천천히 기상하고, 가벼운 아침 스트레칭 하기");

        // 식사 관련 혈압
        appendCategoryAnalysis(feedback, categoryData.get("식사"), "식사 관련 혈압",
                "염분 섭취 줄이기, 식후 가벼운 산책하기");

        // 활동 관련 혈압
        appendCategoryAnalysis(feedback, categoryData.get("활동"), "신체활동 관련 혈압",
                "운동 강도 조절, 충분한 준비운동 하기");

        // 취침 전 혈압
        appendCategoryAnalysis(feedback, categoryData.get("취침"), "취침 전 혈압",
                "현재의 취침 전 루틴 유지하기");

        feedback.append("\n");
    }

    private void appendCategoryAnalysis(StringBuilder feedback, List<CombinedBloodPressureData> categoryData,
                                        String categoryName, String baseRecommendation) {
        if (categoryData == null || categoryData.isEmpty()) return;

        BPRange range = calculateBPRange(categoryData);
        feedback.append(String.format("⦁ %s (%s)\n", categoryName, range.toString()));

        // 월별 분석을 위한 데이터 그룹화
        Map<String, Double> monthlyAverages = categoryData.stream()
                .collect(Collectors.groupingBy(
                        bp -> bp.getMeasureDatetime().format(DateTimeFormatter.ofPattern("MM")),
                        Collectors.averagingDouble(CombinedBloodPressureData::getSystolic)
                ));

        // 특이사항이 있는 월 찾기
        String highestMonth = monthlyAverages.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        if (!highestMonth.isEmpty()) {
            feedback.append(String.format("   %s월에 높은 수치 관찰됨\n", highestMonth));
        }

        feedback.append("   권장: ").append(baseRecommendation).append("\n\n");
    }

    private void appendOverallAssessment(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        DoubleSummaryStatistics systolicStats = data.stream()
                .mapToDouble(CombinedBloodPressureData::getSystolic)
                .summaryStatistics();
        DoubleSummaryStatistics diastolicStats = data.stream()
                .mapToDouble(CombinedBloodPressureData::getDiastolic)
                .summaryStatistics();

        feedback.append("3. 전체 평가\n");

        // 혈압 단계 평가
        feedback.append("⦁ 혈압 수준 평가:\n");
        String bpLevel = assessBPLevel(systolicStats.getAverage(), diastolicStats.getAverage());
        feedback.append(String.format("   - 현재 상태: %s\n", bpLevel));
        feedback.append(String.format("   - 수축기 혈압 평균: %.1f mmHg\n", systolicStats.getAverage()));
        feedback.append(String.format("   - 이완기 혈압 평균: %.1f mmHg\n", diastolicStats.getAverage()));

        // 변동성 평가
        feedback.append("\n⦁ 혈압 변동성 평가:\n");
        double systolicVariation = systolicStats.getMax() - systolicStats.getMin();
        double diastolicVariation = diastolicStats.getMax() - diastolicStats.getMin();

        feedback.append(String.format("   - 수축기 혈압 변동폭: %.1f mmHg ", systolicVariation));
        appendVariationAssessment(feedback, systolicVariation);

        feedback.append(String.format("   - 이완기 혈압 변동폭: %.1f mmHg ", diastolicVariation));
        appendVariationAssessment(feedback, diastolicVariation);

        // 일중 변동 패턴
        feedback.append("\n⦁ 일중 변동 패턴:\n");
        appendDailyPattern(feedback, data);

        // 위험도 평가
        feedback.append("\n⦁ 심혈관 위험도 평가:\n");
        appendRiskAssessment(feedback, systolicStats.getAverage(), diastolicStats.getAverage());

        feedback.append("\n");
    }

    private String assessBPLevel(double avgSystolic, double avgDiastolic) {
        if (avgSystolic < 120 && avgDiastolic < 80) {
            return "정상 혈압 (수축기 120 mmHg 미만 및 이완기 80 mmHg 미만)";
        } else if (avgSystolic < 130 && avgDiastolic < 85) {
            return "주의 혈압 (수축기 120-129 mmHg 또는 이완기 80-84 mmHg)";
        } else if (avgSystolic < 140 && avgDiastolic < 90) {
            return "고혈압 전단계 (수축기 130-139 mmHg 또는 이완기 85-89 mmHg)";
        } else {
            return "고혈압 의심 (수축기 140 mmHg 이상 또는 이완기 90 mmHg 이상)";
        }
    }

    private void appendVariationAssessment(StringBuilder feedback, double variation) {
        if (variation > 30) {
            feedback.append("(주의 필요)\n   → 혈압 변동폭이 큰 편으로, 생활습관이나 스트레스 관리가 필요할 수 있습니다.\n");
        } else if (variation > 20) {
            feedback.append("(경계선)\n   → 일반적인 범위이나, 지속적인 모니터링이 권장됩니다.\n");
        } else {
            feedback.append("(안정적)\n   → 매우 안정적인 혈압 관리가 이루어지고 있습니다.\n");
        }
    }

    private void appendDailyPattern(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        Map<Integer, DoubleSummaryStatistics> hourlyStats = data.stream()
                .collect(Collectors.groupingBy(
                        bp -> bp.getMeasureDatetime().getHour(),
                        Collectors.summarizingDouble(CombinedBloodPressureData::getSystolic)
                ));

        // 아침 시간대 (6-9시)
        feedback.append("   - 아침 혈압: ");
        analyzePeriod(feedback, hourlyStats, 6, 9);

        // 주간 시간대 (10-17시)
        feedback.append("   - 주간 혈압: ");
        analyzePeriod(feedback, hourlyStats, 10, 17);

        // 저녁 시간대 (18-23시)
        feedback.append("   - 저녁 혈압: ");
        analyzePeriod(feedback, hourlyStats, 18, 23);
    }

    private void analyzePeriod(StringBuilder feedback,
                               Map<Integer, DoubleSummaryStatistics> hourlyStats,
                               int startHour,
                               int endHour) {
        DoubleSummaryStatistics periodStats = new DoubleSummaryStatistics();
        for (int hour = startHour; hour <= endHour; hour++) {
            if (hourlyStats.containsKey(hour)) {
                periodStats.combine(hourlyStats.get(hour));
            }
        }

        if (periodStats.getCount() > 0) {
            feedback.append(String.format("평균 수축기 %.1f mmHg", periodStats.getAverage()));
            if (periodStats.getAverage() > 140) {
                feedback.append(" (주의 필요)");
            } else if (periodStats.getAverage() > 130) {
                feedback.append(" (경계선)");
            } else {
                feedback.append(" (양호)");
            }
            feedback.append("\n");
        } else {
            feedback.append("해당 시간대 측정 데이터 없음\n");
        }
    }

    private void appendRiskAssessment(StringBuilder feedback, double avgSystolic, double avgDiastolic) {
        // 심혈관 위험도 평가
        int riskLevel = calculateRiskLevel(avgSystolic, avgDiastolic);
        feedback.append("   - 위험도 수준: ");
        switch (riskLevel) {
            case 1:
                feedback.append("낮음 (정상 혈압 범위)\n");
                feedback.append("   → 현재의 건강한 생활습관을 유지하시면 좋겠습니다.\n");
                break;
            case 2:
                feedback.append("경계성 (주의 관찰 필요)\n");
                feedback.append("   → 생활습관 개선을 통한 예방이 권장됩니다.\n");
                feedback.append("   → 정기적인 혈압 모니터링을 지속해주세요.\n");
                break;
            case 3:
                feedback.append("다소 높음 (적극적 관리 필요)\n");
                feedback.append("   → 식습관과 운동 습관의 개선이 필요합니다.\n");
                feedback.append("   → 전문의 상담을 통한 정기적인 건강검진을 권장드립니다.\n");
                break;
            case 4:
                feedback.append("높음 (즉각적인 관리 필요)\n");
                feedback.append("   → 가까운 시일 내에 전문의 상담을 받으시기를 권장드립니다.\n");
                feedback.append("   → 식습관, 운동, 스트레스 등 전반적인 생활습관 개선이 시급합니다.\n");
                break;
        }

        // 추가적인 건강 관리 조언
        feedback.append("\n   - 건강관리 조언:\n");
        if (avgSystolic > 130 || avgDiastolic > 85) {
            feedback.append("   → 정기적인 혈압 측정과 기록이 매우 중요합니다.\n");
            feedback.append("   → 하루 30분 이상의 규칙적인 운동을 권장드립니다.\n");
            feedback.append("   → 저염식 식단과 금연, 절주가 도움이 될 수 있습니다.\n");
            if (avgSystolic > 140 || avgDiastolic > 90) {
                feedback.append("   → 전문의와 상담하여 적절한 치료 계획을 수립하시기를 권장드립니다.\n");
            }
        }
    }

    private int calculateRiskLevel(double avgSystolic, double avgDiastolic) {
        if (avgSystolic < 120 && avgDiastolic < 80) return 1;
        if (avgSystolic < 130 && avgDiastolic < 85) return 2;
        if (avgSystolic < 140 && avgDiastolic < 90) return 3;
        return 4;
    }

    private void appendManagementRecommendations(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        feedback.append("4. 관리 권장사항\n");

        // 식사 관련 권장사항
        feedback.append("⦁ 식사 관리:\n");
        // 직접 데이터를 전달
        appendMealRecommendations(feedback, data);

        // 운동 관련 권장사항
        feedback.append("\n⦁ 운동 관리:\n");
        appendExerciseRecommendations(feedback, data);

        // 스트레스 관리 권장사항
        feedback.append("\n⦁ 스트레스 관리:\n");
        appendStressRecommendations(feedback, data);

        // 생활습관 권장사항
        feedback.append("\n⦁ 생활습관 관리:\n");
        appendLifestyleRecommendations(feedback, data);
    }

    private String categorizeTimeAndRemark(CombinedBloodPressureData bp) {
        String remark = bp.getRemark() != null ? bp.getRemark().toLowerCase() : "";
        int hour = bp.getMeasureDatetime().getHour();

        if (hour >= 5 && hour <= 9) return "기상";
        if (hour >= 11 && hour <= 14 || hour >= 17 && hour <= 20) return "식사";
        if (hour >= 21 && hour <= 23) return "취침";

        if (remark.contains("운동") || remark.contains("활동")) return "활동";
        if (remark.contains("스트레스")) return "스트레스";

        return "기타";
    }

    private static class BPRange {
        int minSystolic, maxSystolic, minDiastolic, maxDiastolic;

        @Override
        public String toString() {
            return String.format("%d-%d/%d-%d mmHg",
                    minSystolic, maxSystolic, minDiastolic, maxDiastolic);
        }
    }

    private BPRange calculateBPRange(List<CombinedBloodPressureData> data) {
        BPRange range = new BPRange();
        range.minSystolic = data.stream().mapToInt(CombinedBloodPressureData::getSystolic).min().orElse(0);
        range.maxSystolic = data.stream().mapToInt(CombinedBloodPressureData::getSystolic).max().orElse(0);
        range.minDiastolic = data.stream().mapToInt(CombinedBloodPressureData::getDiastolic).min().orElse(0);
        range.maxDiastolic = data.stream().mapToInt(CombinedBloodPressureData::getDiastolic).max().orElse(0);
        return range;
    }

    private String assessBPLevel(double avgSystolic) {
        if (avgSystolic < 120) return "정상 혈압";
        if (avgSystolic < 130) return "주의 혈압";
        if (avgSystolic < 140) return "고혈압 전단계";
        return "고혈압";
    }

    private Map<String, Double> analyzeMealTimeReadings(List<CombinedBloodPressureData> data) {
        return data.stream()
                .filter(bp -> bp.getRemark() != null && bp.getRemark().toLowerCase().contains("식사"))
                .collect(Collectors.groupingBy(
                        bp -> bp.getMeasureDatetime().format(DateTimeFormatter.ofPattern("MM")),
                        Collectors.averagingDouble(CombinedBloodPressureData::getSystolic)
                ));
    }

    private void appendMealRecommendations(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        List<CombinedBloodPressureData> mealData = data.stream()
                .filter(bp -> bp.getRemark() != null &&
                        bp.getRemark().toLowerCase().contains("식사"))
                .collect(Collectors.toList());

        if (mealData.isEmpty()) {
            feedback.append("   식사 관련 데이터가 충분하지 않습니다.\n");
            return;
        }

        // 기본 통계 계산
        DoubleSummaryStatistics systolicStats = mealData.stream()
                .mapToDouble(CombinedBloodPressureData::getSystolic)
                .summaryStatistics();
        DoubleSummaryStatistics diastolicStats = mealData.stream()
                .mapToDouble(CombinedBloodPressureData::getDiastolic)
                .summaryStatistics();

        feedback.append("   식후 혈압 분석:\n");
        feedback.append(String.format("   - 평균: 수축기 %.1f mmHg, 이완기 %.1f mmHg\n",
                systolicStats.getAverage(), diastolicStats.getAverage()));
        feedback.append(String.format("   - 최고: 수축기 %.1f mmHg, 이완기 %.1f mmHg\n",
                systolicStats.getMax(), diastolicStats.getMax()));
        feedback.append(String.format("   - 최저: 수축기 %.1f mmHg, 이완기 %.1f mmHg\n",
                systolicStats.getMin(), diastolicStats.getMin()));

        // 월별 분석
        Map<String, DoubleSummaryStatistics> monthlyStats = mealData.stream()
                .collect(Collectors.groupingBy(
                        bp -> bp.getMeasureDatetime().format(DateTimeFormatter.ofPattern("MM")),
                        Collectors.summarizingDouble(CombinedBloodPressureData::getSystolic)
                ));

        feedback.append("\n   월별 식후 혈압 변화:\n");
        monthlyStats.forEach((month, stats) -> {
            feedback.append(String.format("   - %s월 평균: %.1f mmHg", month, stats.getAverage()));
            if (stats.getAverage() > 140) {
                feedback.append(" (주의 필요)");
            } else if (stats.getAverage() > 130) {
                feedback.append(" (경계선)");
            }
            feedback.append("\n");
        });

        // 식사 시간대별 분석
        feedback.append("\n   식사 시간대별 분석:\n");
        analyzeMealTimePattern(feedback, mealData, 11, 14, "점심");
        analyzeMealTimePattern(feedback, mealData, 17, 20, "저녁");

        // 종합 평가 및 권장사항
        feedback.append("\n   ※ 종합 평가 및 권장사항:\n");

        // 혈압 수준에 따른 맞춤 권장사항
        if (systolicStats.getAverage() > 140 || diastolicStats.getAverage() > 90) {
            feedback.append("   1. 식사 관리 - 즉각적인 개선 필요\n");
            feedback.append("      - 일일 나트륨 섭취량 2,000mg 이하로 엄격한 제한\n");
            feedback.append("      - 식사량 조절: 한 끼 식사량 20% 감소 권장\n");
            feedback.append("      - DASH 식단 적극 도입 권장\n");
            feedback.append("      - 가공식품 섭취 최소화\n");
        } else if (systolicStats.getAverage() > 130 || diastolicStats.getAverage() > 85) {
            feedback.append("   1. 식사 관리 - 주의 관찰 필요\n");
            feedback.append("      - 일일 나트륨 섭취량 2,300mg 이하 유지\n");
            feedback.append("      - 균형 잡힌 식단 구성 필요\n");
            feedback.append("      - 채소와 과일 섭취 증가 권장\n");
        } else {
            feedback.append("   1. 식사 관리 - 현재 상태 유지\n");
            feedback.append("      - 현재의 식단 패턴 유지\n");
            feedback.append("      - 지속적인 모니터링 권장\n");
        }

        feedback.append("\n   2. 식사 습관 개선 권장사항:\n");
        feedback.append("      - 식사 시간: 규칙적인 식사 시간 유지\n");
        feedback.append("      - 식사 속도: 20-30분에 걸쳐 천천히 섭취\n");
        feedback.append("      - 포화지방 섭취 제한\n");
        feedback.append("      - 식후 30분 가벼운 산책 권장\n");

        feedback.append("\n   3. 영양소 관리 지침:\n");
        feedback.append("      - 칼륨이 풍부한 식품 섭취 권장\n");
        feedback.append("      - 현미, 잡곡 등 통곡물 위주의 식단\n");
        feedback.append("      - 등푸른 생선 주 2회 이상 섭취 권장\n");
        feedback.append("      - 채소와 과일 하루 5회 이상 섭취\n");

        // 시간대별 특이사항이 있는 경우 추가 권장사항
        if (hasHighEveningBP(mealData)) {
            feedback.append("\n   4. 저녁 식사 특별 관리:\n");
            feedback.append("      - 저녁 식사 시간 앞당기기 (취침 3시간 전까지)\n");
            feedback.append("      - 저녁 식사량 감소 (하루 섭취량의 25% 이하)\n");
            feedback.append("      - 저녁 카페인 섭취 제한\n");
        }
    }

    private void analyzeMealTimePattern(StringBuilder feedback,
                                        List<CombinedBloodPressureData> mealData,
                                        int startHour,
                                        int endHour,
                                        String mealType) {
        List<CombinedBloodPressureData> timeData = mealData.stream()
                .filter(bp -> {
                    int hour = bp.getMeasureDatetime().getHour();
                    return hour >= startHour && hour <= endHour;
                })
                .collect(Collectors.toList());

        if (!timeData.isEmpty()) {
            DoubleSummaryStatistics stats = timeData.stream()
                    .mapToDouble(CombinedBloodPressureData::getSystolic)
                    .summaryStatistics();

            feedback.append(String.format("   - %s 식사 후 평균 수축기 혈압: %.1f mmHg ",
                    mealType, stats.getAverage()));

            if (stats.getAverage() > 140) {
                feedback.append("(주의 필요)\n");
                feedback.append(String.format("     → %s 식사량 조절 및 염분 제한 권장\n", mealType));
            } else if (stats.getAverage() > 130) {
                feedback.append("(경계선)\n");
                feedback.append(String.format("     → %s 식사 패턴 모니터링 필요\n", mealType));
            } else {
                feedback.append("(정상)\n");
            }
        }
    }

    private boolean hasHighEveningBP(List<CombinedBloodPressureData> mealData) {
        return mealData.stream()
                .filter(bp -> bp.getMeasureDatetime().getHour() >= 17)
                .mapToDouble(CombinedBloodPressureData::getSystolic)
                .average()
                .orElse(0) > 135;
    }

    private void appendExerciseRecommendations(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        List<CombinedBloodPressureData> exerciseData = data.stream()
                .filter(bp -> bp.getRemark() != null &&
                        bp.getRemark().toLowerCase().contains("운동"))
                .collect(Collectors.toList());

        if (exerciseData.isEmpty()) {
            feedback.append("   운동 관련 데이터가 충분하지 않습니다.\n");
            return;
        }

        DoubleSummaryStatistics systolicStats = exerciseData.stream()
                .mapToDouble(CombinedBloodPressureData::getSystolic)
                .summaryStatistics();
        DoubleSummaryStatistics diastolicStats = exerciseData.stream()
                .mapToDouble(CombinedBloodPressureData::getDiastolic)
                .summaryStatistics();

        feedback.append(String.format("   운동 시 평균 혈압: 수축기 %.1f mmHg, 이완기 %.1f mmHg\n",
                systolicStats.getAverage(), diastolicStats.getAverage()));

        // 운동 강도 평가
        double systolicIncrease = systolicStats.getMax() - systolicStats.getMin();
        double diastolicIncrease = diastolicStats.getMax() - diastolicStats.getMin();

        feedback.append("   운동 중 혈압 변화:\n");
        feedback.append(String.format("   - 수축기 혈압 변동폭: %.1f mmHg\n", systolicIncrease));
        feedback.append(String.format("   - 이완기 혈압 변동폭: %.1f mmHg\n", diastolicIncrease));

        // 운동 강도 평가 및 권장사항
        if (systolicStats.getMax() > 180 || diastolicStats.getMax() > 100) {
            feedback.append("\n   ※ 주의 필요: 운동 중 과도한 혈압 상승이 관찰됩니다.\n");
            feedback.append("   권장사항:\n");
            feedback.append("   1. 운동 강도를 현재의 70-80% 수준으로 낮추어 주세요.\n");
            feedback.append("   2. 준비운동 시간을 10-15분으로 늘려주세요.\n");
            feedback.append("   3. 고강도 운동은 피하고, 중강도의 유산소 운동을 권장합니다.\n");
            feedback.append("   4. 운동 중 정기적인 휴식과 혈압 체크를 해주세요.\n");
        } else if (systolicStats.getMax() > 160 || diastolicStats.getMax() > 90) {
            feedback.append("\n   ※ 경계선 수준: 운동 강도 조절이 필요합니다.\n");
            feedback.append("   권장사항:\n");
            feedback.append("   1. 현재 운동 강도의 80-90% 수준을 유지해주세요.\n");
            feedback.append("   2. 준비운동을 충분히 하고 단계적으로 강도를 높여주세요.\n");
            feedback.append("   3. 유산소 운동과 근력 운동을 적절히 병행하되, 과도한 무산소 운동은 피해주세요.\n");
        } else {
            feedback.append("\n   ※ 안정적: 현재의 운동 강도는 적절한 수준입니다.\n");
            feedback.append("   권장사항:\n");
            feedback.append("   1. 현재의 운동 패턴을 유지해주세요.\n");
            feedback.append("   2. 점진적으로 운동 시간을 늘려가실 수 있습니다.\n");
            feedback.append("   3. 주 3-5회, 회당 30-60분의 운동을 지속해주세요.\n");
        }

        // 운동 시간대별 분석
        Map<Integer, List<CombinedBloodPressureData>> exerciseByHour = exerciseData.stream()
                .collect(Collectors.groupingBy(bp -> bp.getMeasureDatetime().getHour()));

        feedback.append("\n   운동 시간대 분석:\n");

        // 아침 운동 (5-9시)
        analyzeExerciseTimePattern(feedback, exerciseByHour, 5, 9, "아침");
        // 오후 운동 (14-17시)
        analyzeExerciseTimePattern(feedback, exerciseByHour, 14, 17, "오후");
        // 저녁 운동 (18-22시)
        analyzeExerciseTimePattern(feedback, exerciseByHour, 18, 22, "저녁");
    }

    private void analyzeExerciseTimePattern(StringBuilder feedback,
                                            Map<Integer, List<CombinedBloodPressureData>> exerciseByHour,
                                            int startHour,
                                            int endHour,
                                            String timeLabel) {
        List<CombinedBloodPressureData> timeData = new ArrayList<>();
        for (int hour = startHour; hour <= endHour; hour++) {
            if (exerciseByHour.containsKey(hour)) {
                timeData.addAll(exerciseByHour.get(hour));
            }
        }

        if (!timeData.isEmpty()) {
            DoubleSummaryStatistics stats = timeData.stream()
                    .mapToDouble(CombinedBloodPressureData::getSystolic)
                    .summaryStatistics();

            feedback.append(String.format("   - %s 운동 시 평균 수축기 혈압: %.1f mmHg ",
                    timeLabel, stats.getAverage()));

            if (stats.getAverage() > 150) {
                feedback.append("(다른 시간대 운동 권장)\n");
            } else if (stats.getAverage() > 140) {
                feedback.append("(주의 관찰)\n");
            } else {
                feedback.append("(적정)\n");
            }
        }
    }

    private void appendStressRecommendations(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        List<CombinedBloodPressureData> stressData = data.stream()
                .filter(bp -> bp.getRemark() != null &&
                        bp.getRemark().toLowerCase().contains("스트레스"))
                .collect(Collectors.toList());

        if (!stressData.isEmpty()) {
            double avgStressBP = stressData.stream()
                    .mapToDouble(CombinedBloodPressureData::getSystolic)
                    .average()
                    .orElse(0);

            feedback.append(String.format("   스트레스 상황 평균 혈압: %.1f mmHg\n", avgStressBP));
            feedback.append("   권장: 스트레스 관리 기법 습득 (명상, 호흡법 등)\n");
        }
    }

    private void appendLifestyleRecommendations(StringBuilder feedback, List<CombinedBloodPressureData> data) {
        // 취침 전 혈압 분석
        List<CombinedBloodPressureData> eveningData = data.stream()
                .filter(bp -> bp.getMeasureDatetime().getHour() >= 20 &&
                        bp.getMeasureDatetime().getHour() <= 23)
                .collect(Collectors.toList());

        if (!eveningData.isEmpty()) {
            double avgEveningBP = eveningData.stream()
                    .mapToDouble(CombinedBloodPressureData::getSystolic)
                    .average()
                    .orElse(0);

            feedback.append(String.format("   취침 전 평균 혈압: %.1f mmHg\n", avgEveningBP));
            if (avgEveningBP < 130) {
                feedback.append("   평가: 양호한 취침 전 혈압 관리\n");
            } else {
                feedback.append("   권장: 취침 전 릴랙스 루틴 도입 필요\n");
            }
        }
    }
}