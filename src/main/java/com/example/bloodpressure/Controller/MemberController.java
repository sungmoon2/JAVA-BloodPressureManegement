package com.example.bloodpressure.Controller;

// DTO, 서비스, Lombok, 스프링 관련 애너테이션을 임포트
import com.example.bloodpressure.DTO.MemberDTO;  // 회원 정보를 담는 Data Transfer Object (DTO) 클래스
import com.example.bloodpressure.Service.RegisterService;  // 회원가입 처리를 위한 서비스 클래스
import com.example.bloodpressure.Service.LoginService;  // 로그인 및 회원 정보 관리를 위한 서비스 클래스
import lombok.RequiredArgsConstructor;  // final 필드에 대해 생성자를 자동으로 생성해주는 Lombok 애너테이션
import lombok.extern.slf4j.Slf4j;  // 로깅을 위한 Lombok 애너테이션
import org.springframework.security.core.Authentication;  // 인증 객체를 통한 로그인 사용자 정보 접근을 위한 클래스
import org.springframework.stereotype.Controller;  // 이 클래스가 Spring MVC의 컨트롤러임을 나타내는 애너테이션
import org.springframework.ui.Model;  // 뷰에 데이터를 전달하기 위한 Spring MVC 클래스
import org.springframework.web.bind.annotation.*;  // 스프링 웹 요청 매핑에 필요한 애너테이션들
import org.springframework.web.servlet.mvc.support.RedirectAttributes;  // 리다이렉트 시 메시지 전달을 위한 클래스

// Lombok을 통해 로그 기능을 활성화하여 로그를 출력
@Slf4j  // 로깅 기능 활성화
// Spring MVC의 Controller 역할을 수행하는 클래스
@Controller  // 이 클래스가 HTTP 요청을 처리하는 컨트롤러임을 나타냄
@RequiredArgsConstructor  // final 필드인 loginService와 RegisterService의 생성자를 자동으로 생성해줌
public class MemberController {

    private final LoginService loginService;  // 로그인 서비스 객체 (final로 선언되어 DI(의존성 주입) 방식으로 주입됨)
    private final RegisterService RegisterService;  // 회원가입 서비스 객체 (final로 선언되어 DI 방식으로 주입됨)

    // 메인 페이지
    @GetMapping("/")  // 메인 페이지의 URL 경로에 대한 GET 요청을 처리
    public String index() {
        return "index";  // index 뷰를 반환하여 메인 페이지를 표시
    }

    // 로그인 페이지
    @GetMapping("/members/login")  // 로그인 페이지의 URL 경로에 대한 GET 요청을 처리
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        // 로그인 시 오류가 발생한 경우
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");  // 로그인 에러 메시지 전달
        }
        // 회원가입 후 로그인 페이지로 이동 시 성공 메시지 전달
        if (registered != null) {
            model.addAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");  // 회원가입 완료 메시지 전달
        }
        return "login";  // login 뷰를 반환하여 로그인 페이지를 표시
    }

    // 회원가입 페이지 GET 요청
    @GetMapping("/members/register")  // 회원가입 페이지 URL에 대한 GET 요청 처리
    public String registerForm(Model model) {
        log.info("회원가입 페이지 접속");  // 회원가입 페이지에 접속 시 로그 출력
        model.addAttribute("memberDTO", new MemberDTO());  // 새로운 빈 MemberDTO 객체를 모델에 추가 (뷰에서 사용)
        return "register";  // register 뷰를 반환하여 회원가입 페이지를 표시
    }

    // 회원가입 처리 POST 요청
    @PostMapping("/members/register")  // 회원가입 요청에 대한 POST 요청을 처리
    public String register(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        log.info("회원가입 요청 수신: {}", memberDTO.getUsername());  // 회원가입 요청의 사용자 이름을 로그로 기록

        try {
            // 비밀번호와 비밀번호 확인 필드가 일치하는지 확인
            if (!memberDTO.getPassword().equals(memberDTO.getPasswordConfirm())) {
                log.error("비밀번호 불일치");  // 비밀번호 불일치 시 에러 로그 출력
                redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");  // 에러 메시지를 리다이렉트에 추가
                return "redirect:/members/register";  // 회원가입 페이지로 리다이렉트
            }

            // 회원가입 처리 서비스 호출
            RegisterService.register(memberDTO);  // RegisterService의 register 메서드를 호출하여 회원가입 처리
            log.info("회원가입 성공: {}", memberDTO.getUsername());  // 회원가입 성공 로그 출력
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");  // 회원가입 완료 메시지 리다이렉트에 추가
            return "redirect:/members/login";  // 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());  // 회원가입 실패 로그 출력
            redirectAttributes.addFlashAttribute("error", e.getMessage());  // 실패 메시지를 리다이렉트에 추가
            return "redirect:/members/register";  // 회원가입 페이지로 리다이렉트
        }
    }

    // 마이페이지
    @GetMapping("/members/mypage")  // 마이페이지의 URL 경로에 대한 GET 요청 처리
    public String mypage(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();  // 인증 객체에서 로그인한 사용자 이름을 가져옴
            MemberDTO memberDTO = loginService.getMemberInfo(username);  // 로그인한 사용자 정보를 조회
            model.addAttribute("member", memberDTO);  // 조회된 사용자 정보를 모델에 추가하여 뷰에서 사용
            return "mypage";  // mypage 뷰를 반환하여 마이페이지 표시
        } catch (Exception e) {
            model.addAttribute("error", "회원 정보를 불러오는데 실패했습니다.");  // 에러 발생 시 에러 메시지 모델에 추가
            return "redirect:/";  // 메인 페이지로 리다이렉트
        }
    }

    // 마이페이지 정보 수정
    @PostMapping("/members/mypage/update")  // 마이페이지 정보 수정에 대한 POST 요청 처리
    public String updateMemberInfo(@ModelAttribute MemberDTO memberDTO,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {
        try {
            memberDTO.setUsername(authentication.getName());  // 현재 로그인한 사용자 이름을 DTO에 설정
            loginService.updateMember(memberDTO);  // 회원 정보 수정 서비스 호출
            redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");  // 수정 성공 메시지를 리다이렉트에 추가
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());  // 수정 실패 메시지를 리다이렉트에 추가
        }
        return "redirect:/members/mypage";  // 마이페이지로 리다이렉트
    }
}