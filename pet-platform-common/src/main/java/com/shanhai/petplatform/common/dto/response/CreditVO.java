package com.shanhai.petplatform.common.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 信用分 VO — 某目标的评价平均分与数量
 *
 * @author PetPlatform Team
 */
@Data
public class CreditVO {

    /** 评价目标类型 code */
    private Integer targetType;

    /** 评价目标类型名称 */
    private String targetTypeName;

    /** 评价目标ID */
    private Long targetId;

    /** 平均分（1-5，保留两位小数） */
    private BigDecimal avgScore;

    /** 评价数 */
    private Long count;

    /** 构建信用分 VO */
    public static CreditVO of(Integer targetType, Long targetId, BigDecimal avgScore, long count) {
        CreditVO vo = new CreditVO();
        vo.setTargetType(targetType);
        vo.setTargetTypeName(
                com.shanhai.petplatform.common.enums.EvaluationTargetTypeEnum
                        .fromCode(targetType == null ? 1 : targetType).getDesc());
        vo.setTargetId(targetId);
        vo.setAvgScore(avgScore);
        vo.setCount(count);
        return vo;
    }
}
