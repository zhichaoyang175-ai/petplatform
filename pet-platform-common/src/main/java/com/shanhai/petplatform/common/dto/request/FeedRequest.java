package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 发布领养动态请求
 *
 * @author PetPlatform Team
 */
@Data
public class FeedRequest {

    /** 动态内容（必填） */
    @NotBlank(message = "动态内容不能为空")
    private String content;

    /** 图片 URL 列表（可选） */
    private List<String> images;

    /** 关联宠物ID（可选，可空） */
    private Long petId;

}
