package com.capstone.ticketservice.user.dto;

import com.capstone.ticketservice.user.model.Users;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    private String username;
    private String password;
    private String role;

    public static LoginRequestDto fromEntity(Users user) {
        return LoginRequestDto.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .role(user.getRole().name())
                .build();
    }

}
