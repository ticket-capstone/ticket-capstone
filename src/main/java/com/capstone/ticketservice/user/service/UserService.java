package com.capstone.ticketservice.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capstone.ticketservice.user.dto.UserRegistrationDto;
import com.capstone.ticketservice.user.model.User;
import com.capstone.ticketservice.user.model.UserSession;
import com.capstone.ticketservice.user.repository.UserSessionRepository;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserSessionRepository userSessionRepository;

    /**
     * 회원가입
     */

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword()) // 실제론 비밀번호 암호화 필요!
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }


    /**
     * 로그인 시 세션 생성
     */
    @Transactional
    public UserSession createSession(User user, String ipAddress) {
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .ipAddress(ipAddress)
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusHours(2)) // 2시간짜리 세션
                .build();

        return userSessionRepository.save(session);
    }

    /**
     * 토큰으로 세션 조회
     */
    public Optional<UserSession> getSessionByToken(String token) {
        return userSessionRepository.findByToken(token);
    }

    /**
     * 유저 ID로 세션 조회
     */
    public Optional<UserSession> getSessionByUserId(Long userId) {
        return userSessionRepository.findByUser_Id(userId);
    }

    /**
     * 유저 ID + IP 주소로 세션 조회
     */
    public Optional<UserSession> getSessionByUserIdAndIp(String userId, String ipAddress) {
        return userSessionRepository.findByUser_IdAndIpAddress(Long.valueOf(userId), ipAddress);
    }

    public UserSession findValidSessionByToken(String token) {
        return userSessionRepository
                .findByTokenAndExpiresAtAfter(token, LocalDateTime.now())
                .orElse(null);
    }

    public void registerUser(UserRegistrationDto dto) {
    }
}