package com.programmer.escrow.demand.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemandFileItem {

    @NotBlank(message = "文件名不能为空")
    private String name;

    @NotBlank(message = "文件内容不能为空")
    private String url;

    private String contentType;

    private Long size;
}
