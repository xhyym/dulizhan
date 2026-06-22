package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 询盘订单实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_inquiry")
public class Inquiry {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 询盘编号 */
    private String inquiryNo;

    /** 用户ID */
    private Long userId;

    /** 用户名 (冗余) */
    private String userName;

    /** 用户邮箱 (冗余) */
    private String userEmail;

    /** WhatsApp (冗余) */
    private String userWhatsapp;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 用户备注 */
    private String remark;

    /** 状态: 0-待处理, 1-已联系, 2-已完成, 3-已取消 */
    private Integer status;

    /** 管理员备注 */
    private String adminRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
