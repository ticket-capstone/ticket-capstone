package com.capstone.ticketservice.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.capstone.ticketservice.user.model.UserSession;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // 특정 토큰으로 세션 조회
    Optional<UserSession> findByToken(String token);

    // 특정 사용자 ID로 세션 조회
    Optional<UserSession> findByUser_Id(Long userId);

    // 사용자 ID와 IP 주소로 세션 조회
    Optional<UserSession> findByUser_IdAndIpAddress(Long userId, String ipAddress);

    // 만료되지 않은 세션 조회
    Optional<UserSession> findByTokenAndExpiresAtAfter(String token, java.time.LocalDateTime now);
}
