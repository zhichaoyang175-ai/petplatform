package com.shanhai.petplatform.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领养动态/领养故事实体 — t_feed
 *
 * @author PetPlatform Team
 */
@Data
@TableName("t_feed")
public class Feed implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 动态ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者用户ID */
    private Long authorId;

    /** 作者昵称（冗余存储，便于列表展示，避免每次 JOIN 用户表） */
    private String authorName;

    /** 关联宠物ID（可选，可为空） */
    private Long petId;

    /** 动态内容 */
    private String content;

    /** 图片 URL 列表 — 以 JSON 数组字符串形式存于 TEXT */
    private String images;

    /** 点赞数 */
    private Integer likeCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
