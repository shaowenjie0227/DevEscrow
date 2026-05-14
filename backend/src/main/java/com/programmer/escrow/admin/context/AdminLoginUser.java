package com.programmer.escrow.admin.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginUser {

    private Long adminId;
    private String roleCode;
}
