package com.example.bpmanagement.Controller;

import com.example.bpmanagement.DTO.MemberDTO;
import com.example.bpmanagement.Service.RegisterService;
import com.example.bpmanagement.Service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final LoginService loginService;
    private final RegisterService RegisterService;

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 로그인 페이지
    @GetMapping("/members/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        // 로그인 시 오류가 발생한 경우
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        // 회원가입 후 로그인 페이지로 이동 시 성공 메시지 전달
        if (registered != null) {
            model.addAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
        }
        return "login";
    }

    // 회원가입 페이지 GET 요청
    @GetMapping("/members/register")
    public String registerForm(Model model) {
        log.info("회원가입 페이지 접속");
        model.addAttribute("memberDTO", new MemberDTO());
        return "register";  // templates/register.html을 찾음
    }

    // 회원가입 처리 POST 요청
    @PostMapping("/members/register")
    public String register(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        log.info("회원가입 요청 수신: {}", memberDTO.getUsername());

        try {
            if (!memberDTO.getPassword().equals(memberDTO.getPasswordConfirm())) {
                log.error("비밀번호 불일치");
                redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "redirect:/members/register";
            }

            RegisterService.register(memberDTO);
            log.info("회원가입 성공: {}", memberDTO.getUsername());
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/members/login?registered";  // registered 파라미터 추가
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/members/register";
        }
    }

    // 마이페이지
    @GetMapping("/members/mypage")
    public String mypage(Authentication authentication, Model model) {
        try {
            String username = authentication.getName();
            MemberDTO memberDTO = loginService.getMemberInfo(username);
            model.addAttribute("member", memberDTO);
            return "mypage";
        } catch (Exception e) {
            log.error("마이페이지 조회 실패: {}", e.getMessage());  // 로그 추가
            model.addAttribute("error", "회원 정보를 불러오는데 실패했습니다.");
            return "redirect:/";
        }
    }


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

    // 회원정보 수정 처리
    @PostMapping("/members/update")
    public String updateMember(@ModelAttribute("member") MemberDTO memberDTO,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            if (memberDTO.getPassword() != null && !memberDTO.getPassword().isEmpty()) {
                if (!memberDTO.getPassword().equals(memberDTO.getPasswordConfirm())) {
                    log.error("비밀번호 불일치");
                    redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                    return "redirect:/members/update";
                }
            }

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
