package com.capstone.ticketservice.user.controller;

import com.capstone.ticketservice.user.dto.checkUserInfoResponseDto;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.service.UserInfoService;
import com.capstone.ticketservice.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        System.out.println(updatedUser.getName());
        // 세션 사용자 정보도 업데이트
        session.setAttribute("user", updatedUser);

        model.addAttribute("info", checkUserInfoResponseDto.fromEntity(updatedUser));

        return "/user/info";
    }



    //사용자 탈퇴
    @DeleteMapping("/api/users/{userid}")
    public ResponseEntity<String> deleteUserInfo(@PathVariable Long userid, Model model , HttpSession session) {

        Users loggedInUser = (Users) session.getAttribute("user");

        // 로그인 유무 확인 후 아닐 경우 로그인 화면으로
        if (isNotAuthorized(session,userid)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 다른 사용자 정보 접근 방지
        if (!loggedInUser.getUserId().equals(userid)) {
            return ResponseEntity.ok("접근이 불가 합니다"); // 홈 화면으로
        }

        userInfoService.deleteUserInfo(userid);
        session.invalidate(); //세션 만료 처리
        return ResponseEntity.ok("탈퇴 완료");
    }


    //로그인 유무 확인
    private boolean isNotAuthorized(HttpSession session, Long userId) {
        Users loggedInUser = (Users) session.getAttribute("user");
        return loggedInUser == null || !loggedInUser.getUserId().equals(userId);
    }


}
