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

    public Users registerUser(UserRegistrationDto userRegistrationDto) {
        Users user = Users.builder()
                .username(userRegistrationDto.getUsername())
                .email(userRegistrationDto.getEmail())
                .password(userRegistrationDto.getPassword()) // 학교 프로젝트이므로 암호화 생략
                .name(userRegistrationDto.getName())
                .phone(userRegistrationDto.getPhone())
                .status("ACTIVE")
                .build();

        return userRepository.save(user);
    }


    public boolean login(String username, String password) {

        return userRepository.findByUsername(username)
                .map(user -> { return user.getPassword().equals(password);})
                .orElse(false);
    }

    public Optional<Users> findByUsername(String userName) {
        return userRepository.findByUsername(userName);
    }
}
