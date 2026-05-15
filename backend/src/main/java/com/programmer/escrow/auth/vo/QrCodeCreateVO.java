package com.programmer.escrow.auth.vo;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class QrCodeCreateVO {

    private String token;
    private String loginCode;
    private String officialAccountQrImageUrl;
    private String loginEntryUrl;
    private Instant expireAt;
}
