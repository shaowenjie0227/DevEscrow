package com.programmer.escrow.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.home")
public class HomePageProperties {

    private AdminContact adminContact = new AdminContact();
    private List<Insight> marketSignals = new ArrayList<>();
    private List<String> tradingRules = new ArrayList<>();

    @Data
    public static class AdminContact {
        private String title;
        private String message;
        private String qrImageUrl;
    }

    @Data
    public static class Insight {
        private String label;
        private String value;
        private String note;
    }
}
