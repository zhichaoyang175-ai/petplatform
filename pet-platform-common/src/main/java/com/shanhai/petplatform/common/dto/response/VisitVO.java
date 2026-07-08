package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约看宠 VO
 *
 * @author PetPlatform Team
 */
@Data
public class VisitVO {

    /** 预约ID */
    private Long id;

    /** 宠物ID */
    private Long petId;

    /** 申请人ID */
    private Long applicantId;

    /** 送养人ID */
    private Long ownerId;

    /** 宠物名称 */
    private String petName;

    /** 申请人名称 */
    private String applicantName;

    /** 送养人名称 */
    private String ownerName;

    /** 预约探视时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;

    /** 备注/留言 */
    private String note;

    /** 状态: 0-待确认 1-已确认 2-已完成 3-已取消 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /** 构建 VO */
    public static VisitVO of(Long id, Long petId, Long applicantId, Long ownerId,
                             String petName, String applicantName, String ownerName,
                             LocalDateTime appointmentTime, String note, Integer status,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        VisitVO vo = new VisitVO();
        vo.setId(id);
        vo.setPetId(petId);
        vo.setApplicantId(applicantId);
        vo.setOwnerId(ownerId);
        vo.setPetName(petName);
        vo.setApplicantName(applicantName);
        vo.setOwnerName(ownerName);
        vo.setAppointmentTime(appointmentTime);
        vo.setNote(note);
        vo.setStatus(status);
        vo.setStatusName(statusName(status));
        vo.setCreatedAt(createdAt);
        vo.setUpdatedAt(updatedAt);
        return vo;
    }

    /** status → name 翻译 */
    public static String statusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "已确认";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

}
