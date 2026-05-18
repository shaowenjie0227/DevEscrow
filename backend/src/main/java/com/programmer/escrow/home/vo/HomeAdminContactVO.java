package com.programmer.escrow.home.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeAdminContactVO {

    private String title;
    private String message;
    private String qrImageUrl;
}
