package com.capstone.ticketservice.user.dto;

import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class checkUserInfoResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String password;
    private String name;
    private String phone;

    public static checkUserInfoResponseDto fromEntity(Users user) {
        return checkUserInfoResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .phone(user.getPhone())
                .build();

    }




}
