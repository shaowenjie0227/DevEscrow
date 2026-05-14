package com.programmer.escrow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YesNoEnum {
    NO(0, "否"),
    YES(1, "是");

    private final Integer code;
    private final String desc;
}
