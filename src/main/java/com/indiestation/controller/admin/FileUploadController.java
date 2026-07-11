package com.indiestation.controller.admin;

import com.indiestation.common.Result;
import com.indiestation.entity.vo.PresignedUrlVo;
import com.indiestation.service.R2Service;
import com.indiestation.support.upload.AdminUploadPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 获取 R2 预签名上传 URL (前端直传)
     * GET /api/admin/upload/presigned?fileName=products/123/main.jpg&contentType=image/jpeg
     */
    @GetMapping("/presigned")
    public Result<PresignedUrlVo> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam(required = false) Long fileSize,
            @RequestParam(required = false) String purpose) {
        AdminUploadPurpose uploadPurpose = AdminUploadPurpose.fromCode(purpose).orElse(null);
        PresignedUrlVo vo = r2Service.generateUploadUrl(fileName, contentType, fileSize, uploadPurpose);
        return Result.success(vo);
    }

    /**
     * 删除 R2 文件
     */
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam String fileUrl) {
        r2Service.deleteFileByUrl(fileUrl);
        return Result.success();
    }
}
