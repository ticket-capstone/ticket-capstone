package com.capstone.ticketservice.user.controller;

import com.capstone.ticketservice.user.dto.UserRegistrationDto;
import com.capstone.ticketservice.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/users")
public class UserRegisterController {
    private final UserService userService;

    @Autowired
    public UserRegisterController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "user/register"; //user.register.html 반환
    }

    @PostMapping
    public String registerUser(@ModelAttribute("userRegistrationDto") UserRegistrationDto userRegistrationDto,
                               RedirectAttributes redirectAttributes) {
        // 사용자명 중복 체크
        if (userService.findByUsername(userRegistrationDto.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 아이디입니다");
            return "redirect:/api/users/register";
        }

        try {
            userService.registerUser(userRegistrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/api/sessions/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/users/register";
        }
    }
}
