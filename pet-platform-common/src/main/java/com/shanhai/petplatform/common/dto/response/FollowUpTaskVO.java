package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shanhai.petplatform.common.enums.FollowUpStatusEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 回访任务 VO
 *
 * @author PetPlatform Team
 */
@Data
public class FollowUpTaskVO {

    /** 任务ID */
    private Long id;

    /** 领养记录ID */
    private Long adoptionRecordId;

    /** 宠物名称 */
    private String petName;

    /** 第几次回访 */
    private Integer periodNumber;

    /** 计划日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    /** 截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    /** 状态 code */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 提醒时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notifiedAt;

    /** 构建 VO */
    public static FollowUpTaskVO of(Long id, Long adoptionRecordId, String petName,
                                     Integer periodNumber, LocalDate scheduledDate,
                                     LocalDate dueDate, Integer status, LocalDateTime notifiedAt) {
        FollowUpTaskVO vo = new FollowUpTaskVO();
        vo.setId(id);
        vo.setAdoptionRecordId(adoptionRecordId);
        vo.setPetName(petName);
        vo.setPeriodNumber(periodNumber);
        vo.setScheduledDate(scheduledDate);
        vo.setDueDate(dueDate);
        vo.setStatus(status);
        vo.setStatusName(FollowUpStatusEnum.fromCode(status).getDesc());
        vo.setNotifiedAt(notifiedAt);
        return vo;
    }

}
