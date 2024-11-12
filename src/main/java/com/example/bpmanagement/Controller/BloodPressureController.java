package com.example.bpmanagement.Controller;

// 필요한 클래스들을 import
import com.example.bpmanagement.DTO.BloodPressureDTO;
import com.example.bpmanagement.Entity.BloodPressure;
import com.example.bpmanagement.Service.BloodPressureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller  // 스프링 MVC의 컨트롤러임을 명시
@RequiredArgsConstructor  // final 필드에 대한 생성자 주입 자동 생성
@RequestMapping("/bloodpressure")  // 기본 URL 경로 설정
public class BloodPressureController {

    // BloodPressureService를 주입받아 사용
    private final BloodPressureService bloodPressureService;

    /**
     * 혈압 데이터 추가 페이지를 반환하는 메서드입니다.
     *
     * @return "bloodpressure/addbloodpressure" 뷰 템플릿 이름을 반환합니다.
     */
    @GetMapping("/add")
    public String showAddBloodPressurePage() {
        return "bloodpressure/addbloodpressure";  // 혈압 데이터 추가 페이지로 이동
    }

    /**
     * 사용자가 혈압 데이터를 추가할 때 호출되는 메서드입니다.
     *
     * @param bloodPressureRecordDTO 사용자가 입력한 혈압 데이터 DTO
     * @param redirectAttributes 리다이렉트 후 메시지 전달을 위한 객체
     * @return "redirect:/bloodpressure/data" 추가 성공 시 혈압 데이터 목록 페이지로 리다이렉트
     *         또는 실패 시 "redirect:/bloodpressure/add"로 돌아가도록 설정됩니다.
     */
    @PostMapping("/add")
    public String addBloodPressure(@ModelAttribute BloodPressureDTO bloodPressureRecordDTO,
                                   RedirectAttributes redirectAttributes) {
        try {
            // 혈압 데이터를 서비스 계층으로 전달하여 데이터베이스에 저장
            bloodPressureService.addBloodPressureRecord(bloodPressureRecordDTO);
            // 성공 메시지를 설정하여 리다이렉트 시 표시
            redirectAttributes.addFlashAttribute("message", "혈압 데이터가 추가되었습니다.");
            // 데이터 목록 페이지로 리다이렉트
            return "redirect:/bloodpressure/data";
        } catch (Exception e) {
            // 예외가 발생하면 에러 메시지를 설정하여 리다이렉트 시 표시
            redirectAttributes.addFlashAttribute("error", "혈압 데이터 추가에 실패했습니다.");
            // 다시 추가 페이지로 리다이렉트
            return "redirect:/bloodpressure/add";
        }
    }

    /**
     * 사용자의 혈압 데이터를 목록 형식으로 보여주는 메서드입니다.
     *
     * @param model 뷰로 전달할 데이터를 저장하는 객체
     * @return "bloodpressure/bloodpressuredata" 혈압 데이터 목록을 보여주는 뷰 템플릿 반환
     */
    @GetMapping("/data")
    public String showBloodPressureData(Model model) {
        try {
            // 혈압 데이터 목록을 서비스 계층을 통해 가져옴
            model.addAttribute("bloodpressuredata", bloodPressureService.getBloodPressureRecords());
            return "bloodpressure/bloodpressuredata";  // 혈압 데이터 목록 페이지로 이동
        } catch (Exception e) {
            // 데이터 불러오기에 실패하면 에러 메시지를 모델에 추가
            model.addAttribute("error", "혈압 데이터를 불러오는 데 실패했습니다.");
            // 실패 시 메인 페이지로 리다이렉트
            return "redirect:/";
        }
    }

    /**
     * 혈압 데이터를 기간별로 차트 형식으로 보여주는 메서드입니다.
     *
     * @param model 차트 데이터를 모델에 추가하여 뷰로 전달하는 객체
     * @return "bloodpressure/bloodpressurechart" 혈압 데이터를 차트 형식으로 보여주는 뷰 템플릿 반환
     */
    @GetMapping("/chart")
    public String showChart(Model model) {
        try {
            // 현재 시간 기준으로 각 기간별 혈압 기록을 조회
            LocalDateTime now = LocalDateTime.now();

            // 지난 24시간 동안의 혈압 기록 조회
            List<BloodPressure> last24Hours = bloodPressureService.getBloodPressureRecordsByPeriod(now.minusHours(24), now);
            // 지난 7일 동안의 혈압 기록 조회
            List<BloodPressure> last7Days = bloodPressureService.getBloodPressureRecordsByPeriod(now.minusDays(7), now);
            // 지난 3개월 동안의 혈압 기록 조회
            List<BloodPressure> last3Months = bloodPressureService.getBloodPressureRecordsByPeriod(now.minusMonths(3), now);
            // 지난 6개월 동안의 혈압 기록 조회
            List<BloodPressure> last6Months = bloodPressureService.getBloodPressureRecordsByPeriod(now.minusMonths(6), now);

            // 조회된 데이터를 모델에 추가하여 뷰로 전달
            model.addAttribute("last24Hours", last24Hours);
            model.addAttribute("last7Days", last7Days);
            model.addAttribute("last3Months", last3Months);
            model.addAttribute("last6Months", last6Months);

            return "bloodpressure/bloodpressurechart";  // 혈압 차트 페이지로 이동
        } catch (Exception e) {
            // 차트 데이터를 불러오지 못하면 에러 메시지를 모델에 추가
            model.addAttribute("error", "차트 데이터를 불러오는 데 실패했습니다.");
            // 차트 페이지로 돌아가지 않고 혈압 데이터 목록 페이지로 리다이렉트
            return "redirect:/bloodpressure/data";
        }
    }
}
