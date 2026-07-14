package com.indiestation.service.impl;

import com.indiestation.config.R2Config;
import com.indiestation.entity.vo.PresignedUrlVo;
import com.indiestation.exception.BusinessException;
import com.indiestation.service.R2Service;
import com.indiestation.support.upload.AdminUploadPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

    private final ObjectProvider<S3Presigner> s3PresignerProvider;
    private final ObjectProvider<S3Client> s3ClientProvider;
    private final R2Config r2Config;

    /** 预签名有效期 (分钟) */
    private static final int PRESIGN_DURATION_MINUTES = 10;

    /** 允许的图片类型 */
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    @Override
    public PresignedUrlVo generateUploadUrl(String fileName, String contentType) {
        return generateUploadUrl(fileName, contentType, null, null);
    }

    @Override
    public PresignedUrlVo generateUploadUrl(String fileName, String contentType, Long fileSize, AdminUploadPurpose purpose) {
        return generateUploadUrl(fileName, contentType, fileSize, purpose, null, null);
    }

    @Override
    public PresignedUrlVo generateUploadUrl(
            String fileName,
            String contentType,
            Long fileSize,
            AdminUploadPurpose purpose,
            Integer imageWidth,
            Integer imageHeight
    ) {
        S3Presigner s3Presigner = s3PresignerProvider.getIfAvailable();
        if (s3Presigner == null || !r2Config.isConfigured()) {
            log.warn("R2 文件存储未配置，无法生成预签名上传地址: fileName={}, contentType={}", fileName, contentType);
            throw new BusinessException("R2 文件存储未配置，请先配置 R2_ENDPOINT、R2_ACCESS_KEY、R2_SECRET_KEY、R2_PUBLIC_URL");
        }

        // 校验文件类型
        if (!isAllowedContentType(contentType)) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }

        validateUploadRequest(fileName, contentType, fileSize, purpose, imageWidth, imageHeight);

        // 生成对象 Key: images/product-gallery/2026/06/22/{uuid}.jpg
        String key = generateKey(contentType, purpose);

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

        log.info("生成预签名URL: key={}, fileUrl={}, purpose={}, fileSize={}, imageWidth={}, imageHeight={}",
                key, fileUrl, purpose != null ? purpose.getCode() : "general", fileSize, imageWidth, imageHeight);
        return vo;
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (s3Client == null || !r2Config.isConfigured()) {
            log.warn("R2 文件存储未配置，跳过删除文件: fileUrl={}", fileUrl);
            return;
        }

        String objectKey = extractKeyFromFileUrl(fileUrl);
        if (!StringUtils.hasText(objectKey)) {
            log.warn("无法从文件地址中解析 R2 对象 Key，跳过删除: fileUrl={}", fileUrl);
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucket())
                    .key(objectKey)
                    .build());
            log.info("R2 文件删除成功: key={}, fileUrl={}", objectKey, fileUrl);
        } catch (Exception exception) {
            log.error("删除 R2 文件失败: key={}, fileUrl={}", objectKey, fileUrl, exception);
        }
    }

    /**
     * 生成 R2 对象 Key
     * 格式: images/product-gallery/2026/06/22/{uuid}.jpg
     */
    private String generateKey(String contentType, AdminUploadPurpose purpose) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = getExtensionFromContentType(contentType);
        String directory = purpose != null ? purpose.getDirectory() : "general";
        return String.format("images/%s/%s/%s.%s", directory, datePath, uuid, extension);
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

    /**
     * 后端兜底校验上传请求，避免有人绕过前端直接申请超大图片预签名地址。
     */
    private void validateUploadRequest(
            String fileName,
            String contentType,
            Long fileSize,
            AdminUploadPurpose purpose,
            Integer imageWidth,
            Integer imageHeight
    ) {
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException("文件名不能为空");
        }

        if (fileName.length() > 200) {
            throw new BusinessException("文件名过长，请缩短后重试");
        }

        if (purpose == null) {
            return;
        }

        if (!contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(purpose.getLabel() + "仅支持图片文件");
        }

        if (fileSize != null && fileSize > purpose.getMaxFileSize()) {
            throw new BusinessException(String.format("%s大小不能超过 %dMB",
                    purpose.getLabel(),
                    purpose.getMaxFileSize() / 1024 / 1024));
        }

        if (imageWidth == null || imageHeight == null || imageWidth <= 0 || imageHeight <= 0) {
            throw new BusinessException(purpose.getLabel() + "缺少有效的图片宽高信息");
        }

        double ratio = (double) imageWidth / imageHeight;
        if (ratio < purpose.getMinRatio() || ratio > purpose.getMaxRatio()) {
            throw new BusinessException(String.format(
                    "%s比例不符合要求，允许范围为 %.2f 到 %.2f",
                    purpose.getLabel(),
                    purpose.getMinRatio(),
                    purpose.getMaxRatio()
            ));
        }
    }

    /**
     * 从公开访问 URL 中提取对象 Key。
     */
    private String extractKeyFromFileUrl(String fileUrl) {
        String normalizedPublicUrl = r2Config.getPublicUrl();
        if (!StringUtils.hasText(normalizedPublicUrl)) {
            return null;
        }

        String publicUrlPrefix = normalizedPublicUrl.endsWith("/")
                ? normalizedPublicUrl
                : normalizedPublicUrl + "/";
        if (!fileUrl.startsWith(publicUrlPrefix)) {
            return null;
        }

        String objectKey = fileUrl.substring(publicUrlPrefix.length()).trim();
        return StringUtils.hasText(objectKey) ? objectKey : null;
    }
}
