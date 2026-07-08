package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 救助站实体 — t_shelter
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_shelter")
public class Shelter implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 救助站ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者用户ID（关联 t_user，救助站也由送养人 role=2 创建） */
    private Long ownerUserId;

    /** 救助站名称 */
    private String name;

    /** 简介/描述 */
    private String description;

    /** 详细地址 */
    private String address;

    /** 所在省 */
    private String province;

    /** 所在市 */
    private String city;

    /** 联系手机号 */
    private String contactPhone;

    /** LOGO/头像 URL */
    private String logoUrl;

    /** 状态: 0-待审核 1-已通过 2-已驳回 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
