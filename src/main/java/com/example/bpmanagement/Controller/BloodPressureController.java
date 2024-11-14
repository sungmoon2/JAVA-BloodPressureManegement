package com.example.bpmanagement.Controller;

// 필요한 클래스 및 패키지를 import
import com.example.bpmanagement.DTO.BloodPressureDTO;
import com.example.bpmanagement.Service.BloodPressureService;
import com.example.bpmanagement.Service.BloodPressureService.CombinedBloodPressureData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

// @Controller: Spring MVC에서 컨트롤러 역할을 하는 클래스임을 나타냄
@Controller

// @RequiredArgsConstructor: Lombok을 사용하여 final 필드에 대한 생성자를 자동으로 생성함
@RequiredArgsConstructor

// @RequestMapping: "/bloodpressure"로 시작하는 모든 요청을 이 컨트롤러에서 처리함
@RequestMapping("/bloodpressure")
public class BloodPressureController {

    // BloodPressureService 인스턴스를 주입받음 (DI)
    private final BloodPressureService bloodPressureService;

    /**
     * GET 요청: "/bloodpressure/add" URL을 통해 혈압 데이터를 추가하는 페이지를 반환
     */
    @GetMapping("/add")
    public String showAddBloodPressurePage() {
        // "bloodpressure/addbloodpressure" 뷰를 반환 (HTML 페이지를 표시)
        return "bloodpressure/addbloodpressure";
    }

    /**
     * POST 요청: "/bloodpressure/add" URL을 통해 사용자가 입력한 혈압 데이터를 서버에 전송
     * @param bloodPressureRecordDTO 사용자가 입력한 혈압 데이터를 담은 DTO 객체
     * @param redirectAttributes 리다이렉트 시 전달할 메시지를 담는 객체
     * @return 리다이렉트할 URL
     */
    @PostMapping("/add")
    public String addBloodPressure(@ModelAttribute BloodPressureDTO bloodPressureRecordDTO,
                                   RedirectAttributes redirectAttributes) {
        try {
            // bloodPressureService를 통해 혈압 데이터를 추가함
            bloodPressureService.addBloodPressureRecord(bloodPressureRecordDTO);

            // 성공 메시지를 추가하고 "/bloodpressure/data" 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", "혈압 데이터가 추가되었습니다.");
            return "redirect:/bloodpressure/data";
        } catch (Exception e) {
            // 예외가 발생하면 에러 메시지를 추가하고 "/bloodpressure/add" 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("error", "혈압 데이터 추가에 실패했습니다.");
            return "redirect:/bloodpressure/add";
        }
    }

    /**
     * GET 요청: "/bloodpressure/data" URL을 통해 저장된 모든 혈압 데이터를 표시
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return 혈압 데이터 페이지를 반환하거나 에러 시 메인 페이지로 리다이렉트
     */
    @GetMapping("/data")
    public String showBloodPressureData(Model model) {
        try {
            // BloodPressureService에서 모든 혈압 데이터를 가져와서 모델에 추가
            model.addAttribute("bloodpressuredata", bloodPressureService.getBloodPressureRecords());
            // "bloodpressure/bloodpressuredata" 뷰를 반환
            return "bloodpressure/bloodpressuredata";
        } catch (Exception e) {
            // 데이터 로드 실패 시 에러 메시지를 추가하고 메인 페이지로 리다이렉트
            model.addAttribute("error", "혈압 데이터를 불러오는 데 실패했습니다.");
            return "redirect:/";
        }
    }

    /**
     * GET 요청: "/bloodpressure/chart" URL을 통해 혈압 데이터 차트를 표시
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return 차트 페이지를 반환하거나 에러 시 데이터 페이지로 리다이렉트
     */
    @GetMapping("/chart")
    public String showChart(Model model) {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 데이터 조회
            List<CombinedBloodPressureData> last24Hours =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusHours(24), now);
            List<CombinedBloodPressureData> last7Days =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusDays(7), now);
            List<CombinedBloodPressureData> last3Months =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusMonths(3), now);
            List<CombinedBloodPressureData> last6Months =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusMonths(6), now);

            // 데이터 로깅
            System.out.println("24시간 데이터 수: " + last24Hours.size());
            System.out.println("7일 데이터 수: " + last7Days.size());
            System.out.println("3개월 데이터 수: " + last3Months.size());
            System.out.println("6개월 데이터 수: " + last6Months.size());

            // 샘플 데이터 출력
            if (!last24Hours.isEmpty()) {
                System.out.println("24시간 첫 번째 데이터: " +
                        last24Hours.get(0).getMeasureDatetime() + ", " +
                        last24Hours.get(0).getSystolic() + ", " +
                        last24Hours.get(0).getDiastolic());
            }

            model.addAttribute("last24Hours", last24Hours);
            model.addAttribute("last7Days", last7Days);
            model.addAttribute("last3Months", last3Months);
            model.addAttribute("last6Months", last6Months);

            return "bloodpressure/bloodpressurechart";
        } catch (Exception e) {
            e.printStackTrace();  // 상세한 에러 로그
            model.addAttribute("error", "차트 데이터를 불러오는 데 실패했습니다.");
            return "redirect:/bloodpressure/data";
        }
    }
}
