package com.capstone.ticketservice.user.controller;

import com.capstone.ticketservice.user.dto.LoginRequestDto;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@Controller
@RequestMapping("/api/sessions")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequestDto", new LoginRequestDto());
        return "user/login"; // user/login html에 연결
    }


    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequestDto requestDto,
                        HttpServletRequest request,
                        Model model) {
        // 기존: 2번의 DB 조회
        // boolean success = userService.login(requestDto.getUsername(), requestDto.getPassword());
        // Optional<Users> userOpt = userService.findByUsername(requestDto.getUsername());

        // 개선: 1번의 DB 조회로 통합
        Optional<Users> authenticatedUser = userService.authenticateUser(
                requestDto.getUsername(),
                requestDto.getPassword()
        );

        if (authenticatedUser.isPresent()) {
            Users user = authenticatedUser.get();
            request.getSession().setAttribute("user", user);
            return "redirect:/";
        } else {
            model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다.");
            return "user/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 세션 무효화
            session.invalidate();
        }

        // 선택적: 로그아웃 성공 메시지 추가
        redirectAttributes.addFlashAttribute("successMessage", "로그아웃되었습니다.");

        return "redirect:/"; // 홈으로 리다이렉트
    }
}
