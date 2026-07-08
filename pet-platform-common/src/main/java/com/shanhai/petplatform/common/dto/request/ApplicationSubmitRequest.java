package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 领养申请提交请求
 *
 * @author PetPlatform Team
 */
@Data
public class ApplicationSubmitRequest {

    /** 宠物ID */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    /** 住房类型: 1-自有房 2-租房 3-与家人同住 */
    @NotNull(message = "住房类型不能为空")
    private Integer housingType;

    /** 月收入 */
    private BigDecimal monthlyIncome;

    /** 养宠经验: 0-无 1-有过 2-正在养 */
    @NotNull(message = "养宠经验不能为空")
    private Integer petExperience;

    /** 家人态度 */
    @NotBlank(message = "家人态度不能为空")
    private String familyAttitude;

    /** 现有宠物情况 */
    private String currentPets;

    /** 申请理由 */
    private String reason;

}
