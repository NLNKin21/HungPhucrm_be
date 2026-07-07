package com.hungphu.crm.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.email")
@Getter
@Setter
public class EmailProperties {
    private String fromName = "Hưng Phú CRM";
    private String fromAddress = "${MAIL_USERNAME:noreply@hungphu.vn}";
    private boolean enabled = true;
}