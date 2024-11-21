// `com.example.bpmanagement.Controller` 패키지 안에 있는 클래스를 정의합니다.
package com.example.bpmanagement.Controller;

// 필요한 클래스 및 패키지를 import 합니다.

// `BloodPressureDTO`는 혈압 데이터를 전송하거나 받기 위해 사용하는 Data Transfer Object 클래스입니다.
import com.example.bpmanagement.DTO.BloodPressureDTO;

// `BloodPressureService`는 비즈니스 로직을 처리하는 서비스 클래스입니다.
import com.example.bpmanagement.Entity.Member;
import com.example.bpmanagement.Repository.MemberRepository;
import com.example.bpmanagement.Service.BloodPressureService;

// `CombinedBloodPressureData`는 혈압 데이터를 조합하여 사용할 때 사용하는 내부 클래스입니다.
import com.example.bpmanagement.Service.BloodPressureService.CombinedBloodPressureData;

// `RequiredArgsConstructor`는 Lombok 라이브러리를 사용하여 final 필드에 대한 생성자를 자동으로 생성해 줍니다.
import lombok.RequiredArgsConstructor;

// `Controller`는 Spring MVC에서 이 클래스가 컨트롤러 역할을 한다는 것을 나타냅니다.
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

// `Model`은 뷰(HTML) 페이지로 데이터를 전달할 때 사용하는 객체입니다.
import org.springframework.ui.Model;

// `RequestMapping`, `GetMapping`, `PostMapping`은 각각 URL 요청을 처리하는 어노테이션입니다.
import org.springframework.web.bind.annotation.*;

// `RedirectAttributes`는 리다이렉트할 때 메시지(알림 메시지 등)를 함께 전달하기 위해 사용합니다.
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// `LocalDateTime`은 현재 날짜와 시간을 다루기 위해 사용하는 Java 클래스입니다.
import java.time.LocalDateTime;

// `List`는 데이터를 리스트 형태로 저장하고 관리하기 위해 사용합니다.
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j

// @Controller 어노테이션을 사용하면 Spring이 이 클래스를 웹 요청을 처리하는 컨트롤러로 인식합니다.
@Controller

// @RequiredArgsConstructor는 Lombok에서 제공하며, final로 선언된 필드에 대해 생성자를 자동으로 생성합니다.

// @RequestMapping("/bloodpressure")는 "/bloodpressure"로 시작하는 모든 URL 요청을 이 컨트롤러에서 처리하게 합니다.
@RequestMapping("/bloodpressure")
public class BloodPressureController {

    @Autowired
    public BloodPressureController(BloodPressureService bloodPressureService,
                                   MemberRepository memberRepository) {
        this.bloodPressureService = bloodPressureService;
        this.memberRepository = memberRepository;
    }


    // `BloodPressureService` 객체를 의존성 주입(DI) 방식으로 사용합니다.
    private final BloodPressureService bloodPressureService;
    private final MemberRepository memberRepository;

    /**
     * GET 요청 메서드입니다.
     * "/bloodpressure/add" URL을 요청하면, 혈압 데이터를 추가하는 페이지를 반환합니다.
     */
    @GetMapping("/add")
    public String showAddBloodPressurePage() {
        // "bloodpressure/addbloodpressure" HTML 페이지를 표시합니다.
        return "bloodpressure/addbloodpressure";
    }

    /**
     * POST 요청 메서드입니다.
     * "/bloodpressure/add" URL로 사용자가 입력한 혈압 데이터를 전송할 때 호출됩니다.
     *
     * @param bloodPressureRecordDTO 사용자가 입력한 혈압 데이터를 담은 객체입니다.
     * @param redirectAttributes     리다이렉트할 때 전달할 메시지를 저장하는 객체입니다.
     * @return 데이터를 추가한 후 리다이렉트할 URL입니다.
     */
    @PostMapping("/add")
    public String addBloodPressure(@ModelAttribute BloodPressureDTO bloodPressureRecordDTO,
                                   RedirectAttributes redirectAttributes) {
        try {
            // BloodPressureService를 통해 혈압 데이터를 데이터베이스에 저장합니다.
            bloodPressureService.addBloodPressureRecord(bloodPressureRecordDTO);

            // 성공 메시지를 추가하고, "/bloodpressure/data"로 리다이렉트합니다.
            redirectAttributes.addFlashAttribute("message", "혈압 데이터가 추가되었습니다.");
            return "redirect:/bloodpressure/data";
        } catch (Exception e) {
            // 에러가 발생하면 에러 메시지를 추가하고, 다시 입력 페이지로 리다이렉트합니다.
            redirectAttributes.addFlashAttribute("error", "혈압 데이터 추가에 실패했습니다.");
            return "redirect:/bloodpressure/add";
        }
    }

    /**
     * GET 요청 메서드입니다.
     * "/bloodpressure/data" URL로 요청하면, 저장된 모든 혈압 데이터를 표시합니다.
     *
     * @param model 뷰로 데이터를 전달하기 위한 객체입니다.
     * @return "bloodpressure/bloodpressuredata" 페이지를 반환하거나, 에러가 발생하면 메인 페이지로 리다이렉트합니다.
     */
    @GetMapping("/data")
    public String showBloodPressureData(Model model) {
        try {
            // BloodPressureService에서 모든 혈압 데이터를 가져와서 모델에 추가합니다.
            model.addAttribute("bloodpressuredata", bloodPressureService.getBloodPressureRecords());
            // "bloodpressure/bloodpressuredata" HTML 페이지를 반환합니다.
            return "bloodpressure/bloodpressuredata";
        } catch (Exception e) {
            // 데이터 로드 실패 시, 에러 메시지를 추가하고 메인 페이지로 리다이렉트합니다.
            model.addAttribute("error", "혈압 데이터를 불러오는 데 실패했습니다.");
            return "redirect:/";
        }
    }

    /**
     * GET 요청 메서드입니다.
     * "/bloodpressure/chart" URL로 요청하면, 혈압 데이터 차트를 표시합니다.
     *
     * @param model 뷰로 데이터를 전달하기 위한 객체입니다.
     * @return "bloodpressure/bloodpressurechart" 페이지를 반환하거나, 에러 시 데이터 페이지로 리다이렉트합니다.
     */
    @GetMapping("/chart")
    public String showChart(Model model) {
        try {
            // 현재 날짜와 시간을 가져옵니다.
            LocalDateTime now = LocalDateTime.now();

            // 혈압 데이터를 다양한 기간(24시간, 7일, 3개월, 6개월, 1년) 동안 조회합니다.
            List<CombinedBloodPressureData> last24Hours =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusHours(24), now);
            List<CombinedBloodPressureData> last7Days =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusDays(7), now);
            List<CombinedBloodPressureData> last3Months =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusMonths(3), now);
            List<CombinedBloodPressureData> last6Months =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusMonths(6), now);
            List<CombinedBloodPressureData> last1Years =
                    bloodPressureService.getBloodPressureRecordsByPeriod(now.minusYears(1), now);

            // 데이터의 크기를 로그로 출력합니다.
            System.out.println("24시간 데이터 수: " + last24Hours.size());
            System.out.println("7일 데이터 수: " + last7Days.size());
            System.out.println("3개월 데이터 수: " + last3Months.size());
            System.out.println("6개월 데이터 수: " + last6Months.size());
            System.out.println("1년 데이터 수: " + last1Years.size());

            // 첫 번째 데이터를 샘플로 출력합니다.
            if (!last24Hours.isEmpty()) {
                System.out.println("24시간 첫 번째 데이터: " +
                        last24Hours.get(0).getMeasureDatetime() + ", " +
                        last24Hours.get(0).getSystolic() + ", " +
                        last24Hours.get(0).getDiastolic());
            }

            // 모델에 각 기간별 혈압 데이터를 추가합니다.
            model.addAttribute("last24Hours", last24Hours);
            model.addAttribute("last7Days", last7Days);
            model.addAttribute("last3Months", last3Months);
            model.addAttribute("last6Months", last6Months);
            model.addAttribute("last1Years", last1Years);

            // "bloodpressure/bloodpressurechart" HTML 페이지를 반환합니다.
            return "bloodpressure/bloodpressurechart";
        } catch (Exception e) {
            // 에러 로그를 출력하고, 에러 메시지를 추가한 후 데이터 페이지로 리다이렉트합니다.
            e.printStackTrace();
            model.addAttribute("error", "차트 데이터를 불러오는 데 실패했습니다.");
            return "redirect:/bloodpressure/data";
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateBloodPressure(
            @RequestBody BloodPressureDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Member member = memberRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            bloodPressureService.updateBloodPressure(dto, member);

            Map<String, String> response = Map.of("message", "성공적으로 수정되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "데이터 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/delete/{id}")  // 경로가 /bloodpressure/delete/{id}가 됨
    @ResponseBody
    public ResponseEntity<String> deleteBloodPressure(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Member member = memberRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            bloodPressureService.deleteBloodPressure(id, member);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}