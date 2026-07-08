package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shanhai.petplatform.common.enums.ApplicationStatusEnum;
import com.shanhai.petplatform.common.enums.HousingTypeEnum;
import com.shanhai.petplatform.common.enums.PetExperienceEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领养申请 VO
 *
 * @author PetPlatform Team
 */
@Data
public class ApplicationVO {

    /** 申请ID */
    private Long id;

    /** 宠物ID */
    private Long petId;

    /** 宠物名称 */
    private String petName;

    /** 宠物品种 */
    private String petBreed;

    /** 宠物表情（前端展示用） */
    private String petEmoji;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 住房类型 code */
    private Integer housingType;

    /** 住房类型名称 */
    private String housingTypeName;

    /** 养宠经验 code */
    private Integer petExperience;

    /** 养宠经验名称 */
    private String petExperienceName;

    /** 月收入 */
    private BigDecimal monthlyIncome;

    /** 家人态度 */
    private String familyAttitude;

    /** 现有宠物 */
    private String currentPets;

    /** 申请理由 */
    private String reason;

    /** 状态 code */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 驳回原因 */
    private String rejectReason;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 构建 VO */
    public static ApplicationVO of(Long id, Long petId, String petName, String petBreed,
                                    String petEmoji, Long applicantId, String applicantName,
                                    Integer housingType, Integer petExperience,
                                    BigDecimal monthlyIncome, String familyAttitude,
                                    String currentPets, String reason,
                                    Integer status, String rejectReason, LocalDateTime createdAt) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(id);
        vo.setPetId(petId);
        vo.setPetName(petName);
        vo.setPetBreed(petBreed);
        vo.setPetEmoji(petEmoji);
        vo.setApplicantId(applicantId);
        vo.setApplicantName(applicantName);
        vo.setHousingType(housingType);
        vo.setHousingTypeName(HousingTypeEnum.fromCode(housingType).getDesc());
        vo.setPetExperience(petExperience);
        vo.setPetExperienceName(PetExperienceEnum.fromCode(petExperience).getDesc());
        vo.setMonthlyIncome(monthlyIncome);
        vo.setFamilyAttitude(familyAttitude);
        vo.setCurrentPets(currentPets);
        vo.setReason(reason);
        vo.setStatus(status);
        vo.setStatusName(ApplicationStatusEnum.fromCode(status).getDesc());
        vo.setRejectReason(rejectReason);
        vo.setCreatedAt(createdAt);
        return vo;
    }

}
