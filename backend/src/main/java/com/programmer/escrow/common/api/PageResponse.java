package com.programmer.escrow.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> list;
    private Long total;
    private Integer pageNo;
    private Integer pageSize;

    public static <T> PageResponse<T> empty(Integer pageNo, Integer pageSize) {
        return PageResponse.<T>builder()
                .list(Collections.emptyList())
                .total(0L)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
    }
}
