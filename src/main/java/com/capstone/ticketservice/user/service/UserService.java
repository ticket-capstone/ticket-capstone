package com.capstone.ticketservice.user.service;
import com.capstone.ticketservice.user.dto.UserRegistrationDto;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(UserRegistrationDto userRegistrationDto) {
        Users user = Users.builder()
                .username(userRegistrationDto.getUsername())
                .email(userRegistrationDto.getEmail())
                .password(userRegistrationDto.getPassword()) // 학교 프로젝트이므로 암호화 생략
                .name(userRegistrationDto.getName())
                .phone(userRegistrationDto.getPhone())
                .status("ACTIVE")
                .role(Users.Role.USER)
                .build();

        userRepository.save(user);
    }


    public boolean login(String username, String password) {

        return userRepository.findByUsername(username)
                .map(user -> { return user.getPassword().equals(password);})
                .orElse(false);
    }

    public Optional<Users> findByUsername(String userName) {
        return userRepository.findByUsername(userName);
    }

    /**
     * 로그인 처리 - 인증 성공 시 사용자 객체 반환
     * N+1 문제 해결: 한 번의 조회로 인증과 사용자 정보 모두 처리
     */
    public Optional<Users> authenticateUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }
}
