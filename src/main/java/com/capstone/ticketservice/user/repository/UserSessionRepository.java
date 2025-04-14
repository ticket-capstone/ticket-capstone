package com.capstone.ticketservice.user.repository;

import com.capstone.ticketservice.user.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
}
