package com.capstone.ticketservice.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.capstone.ticketservice.user.dto.UserRegistrationDto;
import com.capstone.ticketservice.user.model.User;
import com.capstone.ticketservice.user.service.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // ✅ 회원가입 폼 보여주기
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "user/register"; // templates/user/register.html
    }

    // ✅ 회원가입 처리
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto dto) {
        userService.registerUser(dto);
        return "redirect:/api/sessions/login"; // 회원가입 후 로그인 페이지로 이동
    }


}
