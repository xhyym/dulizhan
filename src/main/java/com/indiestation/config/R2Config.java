package com.indiestation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Cloudflare R2 配置 (S3 兼容)
 *
 * @author IndieStation
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "r2")
public class R2Config {

    /** R2 S3 Endpoint */
    private String endpoint;

    /** Access Key ID */
    private String accessKeyId;

    /** Secret Access Key */
    private String secretAccessKey;

    /** Bucket 名称 */
    private String bucket;

    /** 公开访问 URL */
    private String publicUrl;

    /**
     * 创建 S3Presigner Bean (用于生成预签名 URL)
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                        )
                )
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(
                        software.amazon.awssdk.services.s3.S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .build();
    }
}
