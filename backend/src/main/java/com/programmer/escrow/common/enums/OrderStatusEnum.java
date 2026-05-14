package com.programmer.escrow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    WAIT_QUOTE(0, "待报价"),
    QUOTED(1, "已报价"),
    PAID(2, "已托管"),
    IN_PROGRESS(3, "开发中"),
    WAIT_ACCEPT(4, "待验收"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    DISPUTE(7, "纠纷中");

    private final Integer code;
    private final String desc;
}
