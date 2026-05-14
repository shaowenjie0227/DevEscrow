package com.programmer.escrow.auth.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private Integer userType;

    public boolean canAccessClientSide() {
        return userType != null && (userType == 1 || userType == 3);
    }

    public boolean canAccessDeveloperSide() {
        return userType != null && (userType == 2 || userType == 3);
    }
}
