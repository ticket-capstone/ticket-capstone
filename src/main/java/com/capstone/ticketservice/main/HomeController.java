package com.capstone.ticketservice.main;

import com.capstone.ticketservice.event.service.EventService;
import com.capstone.ticketservice.user.model.User;
import com.capstone.ticketservice.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserService userService;
    private final EventService eventService;

    @Autowired
    public HomeController(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        //todo: 현재 로그인한 사용자 정보 저장

        //todo: 공연 목록 가져와서 모델에 저장

        return "home";
    }
}
