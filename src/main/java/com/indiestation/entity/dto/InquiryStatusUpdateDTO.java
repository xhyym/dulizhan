package com.indiestation.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 询盘状态更新参数
 *
 * @author IndieStation
 */
@Data
public class InquiryStatusUpdateDTO {

    /**
     * 询盘状态: 0-待处理, 1-已联系, 2-已完成, 3-已取消
     */
    @NotNull(message = "询盘状态不能为空")
    @Min(value = 0, message = "询盘状态不合法")
    @Max(value = 3, message = "询盘状态不合法")
    private Integer status;

    /**
     * 管理员备注
     */
    private String adminRemark;
}
