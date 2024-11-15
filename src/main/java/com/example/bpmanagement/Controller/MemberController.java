// 패키지 선언: 이 클래스가 속해 있는 패키지
package com.example.bpmanagement.Controller;

// 필요한 클래스 및 패키지 import
import com.example.bpmanagement.DTO.MemberDTO; // 회원 정보를 담는 DTO 클래스
import com.example.bpmanagement.Service.RegisterService; // 회원가입 관련 서비스 클래스
import com.example.bpmanagement.Service.LoginService; // 로그인 관련 서비스 클래스
import lombok.RequiredArgsConstructor; // Lombok에서 final 필드에 대해 자동으로 생성자를 생성해주는 어노테이션
import lombok.extern.slf4j.Slf4j; // 로그 기록을 위한 Lombok 어노테이션
import org.springframework.security.core.Authentication; // Spring Security에서 인증 객체를 가져오기 위한 클래스
import org.springframework.stereotype.Controller; // Spring MVC에서 이 클래스가 컨트롤러임을 나타냄
import org.springframework.ui.Model; // 뷰에 데이터를 전달하기 위한 객체
import org.springframework.web.bind.annotation.*; // 요청 처리 관련 어노테이션 (GET, POST 등)
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // 리다이렉트 시 메시지 전달을 위한 객체

// @Slf4j: 로그를 기록하기 위한 Lombok 어노테이션
@Slf4j

// @Controller: 이 클래스가 Spring MVC에서 요청을 처리하는 컨트롤러임을 나타냄
@Controller

// @RequiredArgsConstructor: final 필드에 대해 생성자를 자동으로 생성함
@RequiredArgsConstructor
public class MemberController {

    // LoginService 인스턴스 주입 (DI)
    private final LoginService loginService;

    // RegisterService 인스턴스 주입 (DI)
    private final RegisterService RegisterService;

    /**
     * 메인 페이지 요청 처리
     * @return 메인 페이지 뷰 이름 ("index.html" 파일)
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 로그인 페이지 GET 요청 처리
     * @param error 로그인 오류 메시지
     * @param registered 회원가입 완료 메시지
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return "login" 뷰 (login.html 파일)
     */
    @GetMapping("/members/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        // 로그인 오류 메시지 설정
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        // 회원가입 완료 메시지 설정
        if (registered != null) {
            model.addAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
        }
        return "login";
    }

    /**
     * 회원가입 페이지 GET 요청 처리
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return "register" 뷰 (register.html 파일)
     */
    @GetMapping("/members/register")
    public String registerForm(Model model) {
        log.info("회원가입 페이지 접속");
        // 빈 MemberDTO 객체를 모델에 추가
        model.addAttribute("memberDTO", new MemberDTO());
        return "register";
    }

    /**
     * 회원가입 처리 POST 요청
     * @param memberDTO 회원가입 폼에서 입력된 데이터
     * @param redirectAttributes 리다이렉트 시 전달할 메시지
     * @return 회원가입 성공 시 로그인 페이지로 리다이렉트
     */
    @PostMapping("/members/register")
    public String register(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        log.info("회원가입 요청 수신: {}", memberDTO.getUsername());

        try {
            // 비밀번호 확인
            if (!memberDTO.getPassword().equals(memberDTO.getPasswordConfirm())) {
                log.error("비밀번호 불일치");
                redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "redirect:/members/register";
            }

            // 회원가입 서비스 호출
            RegisterService.register(memberDTO);
            log.info("회원가입 성공: {}", memberDTO.getUsername());
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/members/login?registered";
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/members/register";
        }
    }

    /**
     * 마이페이지 GET 요청 처리
     * @param authentication 인증된 사용자 정보
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return "mypage" 뷰 (mypage.html 파일)
     */
    @GetMapping("/members/mypage")
    public String mypage(Authentication authentication, Model model) {
        try {
            // 현재 로그인한 사용자 이름 가져오기
            String username = authentication.getName();
            // 사용자 정보 조회
            MemberDTO memberDTO = loginService.getMemberInfo(username);
            model.addAttribute("member", memberDTO);
            return "mypage";
        } catch (Exception e) {
            log.error("마이페이지 조회 실패: {}", e.getMessage());
            model.addAttribute("error", "회원 정보를 불러오는데 실패했습니다.");
            return "redirect:/";
        }
    }

    /**
     * 회원정보 수정 페이지 GET 요청 처리
     * @param authentication 인증된 사용자 정보
     * @param model 뷰에 데이터를 전달하기 위한 객체
     * @return "update" 뷰 (update.html 파일)
     */
    @GetMapping("/members/update")
    public String updateForm(Authentication authentication, Model model) {
        try {
            log.info("회원정보 수정 페이지 접속: {}", authentication.getName());
            String username = authentication.getName();
            MemberDTO memberDTO = loginService.getMemberInfo(username);
            model.addAttribute("member", memberDTO);
            return "update";
        } catch (Exception e) {
            log.error("회원정보 조회 실패: {}", e.getMessage());
            model.addAttribute("error", "회원 정보를 불러오는데 실패했습니다.");
            return "redirect:/members/mypage";
        }
    }

    /**
     * 회원정보 수정 처리 POST 요청
     * @param memberDTO 수정된 사용자 정보
     * @param authentication 인증된 사용자 정보
     * @param redirectAttributes 리다이렉트 시 전달할 메시지
     * @return 수정 성공 시 마이페이지로 리다이렉트
     */
    @PostMapping("/members/update")
    public String updateMember(@ModelAttribute("member") MemberDTO memberDTO,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            // 비밀번호 확인
            if (memberDTO.getPassword() != null && !memberDTO.getPassword().isEmpty()) {
                if (!memberDTO.getPassword().equals(memberDTO.getPasswordConfirm())) {
                    log.error("비밀번호 불일치");
                    redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                    return "redirect:/members/update";
                }
            }

            // 사용자 이름 설정
            memberDTO.setUsername(authentication.getName());
            loginService.updateMember(memberDTO);

            log.info("회원정보 수정 성공: {}", memberDTO.getUsername());
            redirectAttributes.addFlashAttribute("message", "회원정보가 성공적으로 수정되었습니다.");
            return "redirect:/members/mypage";

        } catch (Exception e) {
            log.error("회원정보 수정 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "회원정보 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/members/update";
        }
    }
}
