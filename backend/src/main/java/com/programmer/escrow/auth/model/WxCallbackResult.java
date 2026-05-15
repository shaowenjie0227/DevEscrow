package com.programmer.escrow.auth.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WxCallbackResult {

    private boolean followRequired;
    private String followUrl;
    private String title;
    private String message;
}
