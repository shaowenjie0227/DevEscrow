package com.programmer.escrow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {
    CLIENT(1, "用户端"),
    DEVELOPER(2, "开发者"),
    BOTH(3, "双角色"),
    ADMIN(9, "管理员");

    private final Integer code;
    private final String desc;
}
