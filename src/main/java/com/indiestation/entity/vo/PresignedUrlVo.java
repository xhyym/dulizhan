package com.indiestation.entity.vo;

import lombok.Data;

/**
 * 预签名上传 URL 响应 VO
 *
 * @author IndieStation
 */
@Data
public class PresignedUrlVo {

    /** 预签名 PUT URL (前端直传用) */
    private String uploadUrl;

    /** 上传后的文件访问 URL */
    private String fileUrl;

    /** R2 对象 Key */
    private String key;
}
