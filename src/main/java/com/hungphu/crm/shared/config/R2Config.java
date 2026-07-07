package com.hungphu.crm.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "r2")
@RequiredArgsConstructor
public class R2Config {

    private final StorageProperties storageProperties;

    @Bean
    public S3Client s3Client() {
        StorageProperties.R2Properties r2 = storageProperties.getR2();

        // ★ R2 endpoint KHÔNG có bucket name trong URL
        String endpoint = String.format(
                "https://%s.r2.cloudflarestorage.com", r2.getAccountId());

        log.info("Configuring Cloudflare R2: endpoint={}, bucket={}",
                endpoint, r2.getBucketName());

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        r2.getAccessKey(),
                                        r2.getSecretKey()
                                )
                        )
                )
                .region(Region.of("auto"))
                .forcePathStyle(true)
                .build();
    }
}