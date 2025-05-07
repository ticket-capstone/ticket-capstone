package com.capstone.ticketservice.user.controller;

import com.capstone.ticketservice.user.dto.checkUserInfoResponseDto;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.service.UserInfoService;
import com.capstone.ticketservice.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserInfoController {

    private UserInfoService userInfoService;

    @Autowired
    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    //사용자 정보 조회
    @GetMapping("/api/users/{userid}")
    public String checkUserInfo(@PathVariable Long userid, Model model , HttpSession session) {

        Users loggedInUser = (Users) session.getAttribute("user");

        // 로그인 유무 확인 후 아닐 경우 로그인 화면으로
        if (isNotAuthorized(session,userid)) {
            return "redirect:/api/sessions/login";
        }

        // 다른 사용자 정보 접근 방지
        if (!loggedInUser.getUserId().equals(userid)) {
            return "redirect:/"; // 홈 화면으로
        }

        checkUserInfoResponseDto info = userInfoService.checkUserInfo(userid);
        model.addAttribute("info", info);
        return "/user/info";
    }

    //사용자 정보 수정 페이지로 이동
    @GetMapping("/api/users/{userid}/update")
    public String showUpdateForm(@PathVariable Long userid, HttpSession session, Model model) {

        Users loggedInUser = (Users) session.getAttribute("user");

        // 로그인 유무 확인 후 아닐 경우 로그인 화면으로
        if (isNotAuthorized(session, userid)) {
            return "redirect:/api/sessions/login";
        }

        // 다른 사용자 정보 접근 방지
        if (!loggedInUser.getUserId().equals(userid)) {
            return "redirect:/"; // 홈 화면으로
        }

        checkUserInfoResponseDto info = userInfoService.checkUserInfo(userid);
        model.addAttribute("info", info);
        return "/user/update";
    }


    //사용자 정보 수정
    @PutMapping("/api/users/{userid}")
    public String updateUserInfo(@PathVariable Long userid ,@ModelAttribute Users updatedUser, HttpSession session, Model model) {

        Users loggedInUser = (Users) session.getAttribute("user");
        updatedUser = (Users) session.getAttribute("user"); //효율적이지 못하지만 각 역할에 맞도록 변수를 설정함

        // 로그인 유무 확인 후 아닐 경우 로그인 화면으로
        if (isNotAuthorized(session,userid)) {
            return "redirect:/api/sessions/login";
        }

        // 다른 사용자 정보 접근 방지
        if (!loggedInUser.getUserId().equals(userid)) {
            return "redirect:/"; // 홈 화면으로
        }

        // 실제 정보 업데이트
        userInfoService.updateUserInfo(userid, updatedUser);

        // 세션 사용자 정보도 업데이트
        session.setAttribute("user", updatedUser);

        model.addAttribute("info", checkUserInfoResponseDto.fromEntity(updatedUser));

        return "/user/info";
    }



    //사용자 탈퇴
    @DeleteMapping("/api/users/{userid}")
    public String deleteUserInfo(@PathVariable Long userid, Model model , HttpSession session) {

        Users loggedInUser = (Users) session.getAttribute("user");

        // 로그인 유무 확인 후 아닐 경우 로그인 화면으로
        if (isNotAuthorized(session,userid)) {
            return "redirect:/login";
        }

        // 다른 사용자 정보 접근 방지
        if (!loggedInUser.getUserId().equals(userid)) {
            return "redirect:/"; // 홈 화면으로
        }

        userInfoService.deleteUserInfo(userid);
        session.invalidate(); //세션 만료 처리
        return "redirect:/";
    }


    //로그인 유무 확인
    private boolean isNotAuthorized(HttpSession session, Long userId) {
        Users loggedInUser = (Users) session.getAttribute("user");
        return loggedInUser == null || !loggedInUser.getUserId().equals(userId);
    }


}
