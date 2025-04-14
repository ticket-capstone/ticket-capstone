package com.capstone.ticketservice.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
    private String username;
    private String email;
    private String password;
    private String name;
    private String phone;
}
