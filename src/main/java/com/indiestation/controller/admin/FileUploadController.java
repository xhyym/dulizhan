package com.indiestation.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.indiestation.common.Result;
import com.indiestation.entity.vo.PresignedUrlVo;
import com.indiestation.service.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件上传控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final R2Service r2Service;

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:8080/uploads/}")
    private String urlPrefix;

    /**
     * 获取 R2 预签名上传 URL (前端直传)
     * GET /api/admin/upload/presigned?fileName=products/123/main.jpg&contentType=image/jpeg
     */
    @GetMapping("/presigned")
    public Result<PresignedUrlVo> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam String contentType) {
        PresignedUrlVo vo = r2Service.generateUploadUrl(fileName, contentType);
        return Result.success(vo);
    }

    /**
     * 上传图片 (本地存储备用)
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalName);
        if (!isImageFile(suffix)) {
            return Result.error("仅支持 jpg, jpeg, png, gif, webp 格式");
        }

        // 生成文件名
        String fileName = IdUtil.fastSimpleUUID() + "." + suffix;

        // 创建目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(new File(uploadPath + fileName));
        } catch (IOException e) {
            return Result.error("文件上传失败");
        }

        // 返回访问URL
        String url = urlPrefix + fileName;
        return Result.success(url);
    }

    private boolean isImageFile(String suffix) {
        return "jpg".equalsIgnoreCase(suffix)
                || "jpeg".equalsIgnoreCase(suffix)
                || "png".equalsIgnoreCase(suffix)
                || "gif".equalsIgnoreCase(suffix)
                || "webp".equalsIgnoreCase(suffix);
    }
}
