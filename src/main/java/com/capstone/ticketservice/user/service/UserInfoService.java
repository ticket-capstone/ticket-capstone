package com.capstone.ticketservice.user.service;

import com.capstone.ticketservice.user.dto.checkUserInfoResponseDto;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserInfoService {

    private UserRepository userRepository;

    @Autowired
    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //사용자 정보 조회
    public checkUserInfoResponseDto checkUserInfo(Long userId) {
        Optional<Users> user = userRepository.findById(userId);
        return user.map(checkUserInfoResponseDto::fromEntity)
                .orElseThrow(()-> new NoSuchElementException("해당 ID의 사용자를 찾을 수 없습니다."));
    }

    //사용자 정보 수정
    public Users updateUserInfo(Long userId, Users updatedUser) {
        Users user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자 없음"));

        //정보 수정
        user.setUsername(updatedUser.getUsername());
        user.setPassword(updatedUser.getPassword());
        user.setEmail(updatedUser.getEmail());
        user.setName(updatedUser.getName());
        user.setPhone(updatedUser.getPhone());

        return userRepository.save(user);
    }

    //사용자 정보 삭제
    public void deleteUserInfo(Long userId) {
        userRepository.deleteById(userId);
    }

}
