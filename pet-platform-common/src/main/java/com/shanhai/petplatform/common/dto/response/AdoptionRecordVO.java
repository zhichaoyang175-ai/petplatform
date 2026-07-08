package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 领养记录 VO
 *
 * @author PetPlatform Team
 */
@Data
public class AdoptionRecordVO {

    /** 记录ID */
    private Long id;

    /** 申请ID */
    private Long applicationId;

    /** 宠物名称 */
    private String petName;

    /** 宠物品种 */
    private String petBreed;

    /** 送养人姓名 */
    private String adopterName;

    /** 领养人姓名 */
    private String applicantName;

    /** 领养日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime adoptedAt;

    /** 状态: 0-领养中 1-回访中 2-已完成 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 回访总月数 */
    private Integer followUpMonths;

    /** 构建 VO */
    public static AdoptionRecordVO of(Long id, Long applicationId, String petName,
                                       String petBreed, String adopterName, String applicantName,
                                       LocalDateTime adoptedAt, Integer status,
                                       Integer followUpMonths) {
        AdoptionRecordVO vo = new AdoptionRecordVO();
        vo.setId(id);
        vo.setApplicationId(applicationId);
        vo.setPetName(petName);
        vo.setPetBreed(petBreed);
        vo.setAdopterName(adopterName);
        vo.setApplicantName(applicantName);
        vo.setAdoptedAt(adoptedAt);
        vo.setStatus(status);
        vo.setStatusName(statusName(status));
        vo.setFollowUpMonths(followUpMonths);
        return vo;
    }

    private static String statusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "领养中";
            case 1 -> "回访中";
            case 2 -> "已完成";
            default -> "未知";
        };
    }

}
