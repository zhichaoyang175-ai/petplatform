package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

/**
 * 评价提交请求
 *
 * @author PetPlatform Team
 */
@Data
public class EvaluationRequest {

    /** 评价目标类型: 1-用户 2-救助站 */
    @NotNull(message = "评价目标类型不能为空")
    private Integer targetType;

    /** 评价目标ID */
    @NotNull(message = "评价目标ID不能为空")
    private Long targetId;

    /** 评分: 1-5 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为 1")
    @Max(value = 5, message = "评分最大为 5")
    private Integer score;

    /** 评价内容（可选） */
    private String comment;

}
