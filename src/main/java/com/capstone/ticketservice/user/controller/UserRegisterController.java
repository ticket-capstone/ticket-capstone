package com.capstone.ticketservice.user.controller;

import com.capstone.ticketservice.user.dto.UserRegistrationDto;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @PostMapping("/register")
    public ResponseEntity<Users> registerUser(@ModelAttribute("userRegistrationDto") UserRegistrationDto userRegistrationDto) {
        Users registeredUser = userService.registerUser(userRegistrationDto);
        return ResponseEntity.ok(registeredUser);
    }
}
