package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shanhai.petplatform.common.enums.EvaluationTargetTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价 VO
 *
 * @author PetPlatform Team
 */
@Data
public class EvaluationVO {

    /** 评价ID */
    private Long id;

    /** 评价人ID */
    private Long reviewerId;

    /** 评价人昵称 */
    private String reviewerName;

    /** 评价目标类型 code */
    private Integer targetType;

    /** 评价目标类型名称 */
    private String targetTypeName;

    /** 评价目标ID */
    private Long targetId;

    /** 评分 1-5 */
    private Integer score;

    /** 评价内容 */
    private String comment;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 构建 VO（翻译目标类型 code→name，并格式化时间） */
    public static EvaluationVO of(Long id, Long reviewerId, String reviewerName,
                                  Integer targetType, Long targetId, Integer score,
                                  String comment, LocalDateTime createdAt) {
        EvaluationVO vo = new EvaluationVO();
        vo.setId(id);
        vo.setReviewerId(reviewerId);
        vo.setReviewerName(reviewerName);
        vo.setTargetType(targetType);
        vo.setTargetTypeName(EvaluationTargetTypeEnum.fromCode(targetType == null ? 1 : targetType).getDesc());
        vo.setTargetId(targetId);
        vo.setScore(score);
        vo.setComment(comment);
        vo.setCreatedAt(createdAt);
        return vo;
    }
}
