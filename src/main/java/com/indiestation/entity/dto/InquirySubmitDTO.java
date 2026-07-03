package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 询盘提交参数
 *
 * @author IndieStation
 */
@Data
public class InquirySubmitDTO {

    /** 用户备注 */
    private String remark;

    /** 配送时间 */
    @NotBlank(message = "Delivery date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Delivery date must be in YYYY-MM-DD format")
    private String deliveryDate;
}
