package com.programmer.escrow.auth.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String nickname;
    private String phone;
    private String email;
    private String intro;
}
