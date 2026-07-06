package com.indiestation.service;

import com.indiestation.entity.vo.PresignedUrlVo;

/**
 * R2 文件存储服务
 *
 * @author IndieStation
 */
public interface R2Service {

    /**
     * 生成预签名上传 URL
     *
     * @param fileName    原始文件名
     * @param contentType MIME 类型 (如 image/jpeg)
     * @return 预签名信息
     */
    PresignedUrlVo generateUploadUrl(String fileName, String contentType);

    /**
     * 根据公开访问 URL 删除 R2 对象。
     *
     * @param fileUrl 文件公开访问地址
     */
    void deleteFileByUrl(String fileUrl);
}
