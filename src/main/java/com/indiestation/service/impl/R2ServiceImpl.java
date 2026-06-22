package com.indiestation.service.impl;

import com.indiestation.config.R2Config;
import com.indiestation.entity.vo.PresignedUrlVo;
import com.indiestation.exception.BusinessException;
import com.indiestation.service.R2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * R2 文件存储服务实现
 *
 * @author IndieStation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class R2ServiceImpl implements R2Service {

    private final S3Presigner s3Presigner;
    private final R2Config r2Config;

    /** 预签名有效期 (分钟) */
    private static final int PRESIGN_DURATION_MINUTES = 10;

    /** 允许的图片类型 */
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    @Override
    public PresignedUrlVo generateUploadUrl(String fileName, String contentType) {
        // 校验文件类型
        if (!isAllowedContentType(contentType)) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }

        // 生成对象 Key: images/2026/06/22/{uuid}.jpg
        String key = generateKey(fileName, contentType);

        // 构建 PutObjectRequest
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Config.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        // 生成预签名 URL
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(PRESIGN_DURATION_MINUTES))
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        // 构建文件访问 URL (R2.dev 公开域名, 不含 bucket 名)
        String fileUrl = r2Config.getPublicUrl() + "/" + key;

        // 组装返回
        PresignedUrlVo vo = new PresignedUrlVo();
        vo.setUploadUrl(uploadUrl);
        vo.setFileUrl(fileUrl);
        vo.setKey(key);

        log.info("生成预签名URL: key={}, fileUrl={}", key, fileUrl);
        return vo;
    }

    /**
     * 生成 R2 对象 Key
     * 格式: images/2026/06/22/{uuid}.jpg
     */
    private String generateKey(String fileName, String contentType) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = getExtensionFromContentType(contentType);
        return String.format("images/%s/%s.%s", datePath, uuid, extension);
    }

    /**
     * 从 Content-Type 获取文件扩展名
     */
    private String getExtensionFromContentType(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "application/pdf" -> "pdf";
            default -> "bin";
        };
    }

    /**
     * 校验是否允许的文件类型
     */
    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) return false;
        String lower = contentType.toLowerCase();
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (type.equals(lower)) return true;
        }
        // 也允许 PDF
        return "application/pdf".equals(lower);
    }
}
