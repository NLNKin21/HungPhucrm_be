package com.hungphu.crm.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class StorageProperties {

    private String type = "local";  // r2 | local
    private String maxFileSize = "50MB";
    private String allowedImageTypes = "jpg,jpeg,png,gif,webp";
    private String allowedVideoTypes = "mp4,mov,avi,webm";
    private String allowedDocumentTypes = "pdf,doc,docx,xls,xlsx";

    private R2Properties r2 = new R2Properties();
    private LocalProperties local = new LocalProperties();

    @Getter
    @Setter
    public static class R2Properties {
        private String accountId;
        private String accessKey;
        private String secretKey;
        private String bucketName = "hungphu-crm";
        private String publicUrl;
    }

    @Getter
    @Setter
    public static class LocalProperties {
        private String uploadDir = "./uploads";
        private String publicUrl = "http://localhost:8080/uploads";
    }
}