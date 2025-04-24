package com.capstone.ticketservice.main;

import com.capstone.ticketservice.event.service.EventService;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

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
        // 세션에서 username 가져오기
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            String username = (String) session.getAttribute("username");
            // username으로 사용자 정보 조회
            Optional<Users> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                // 사용자 정보를 세션에 저장
                session.setAttribute("user", userOpt.get());
                // 모델에도 추가할 수 있음
                model.addAttribute("user", userOpt.get());
            }
        }

        // 이벤트 목록 추가 (필요하다면)
        model.addAttribute("events", eventService.getEvents());

        return "home";
    }
}
