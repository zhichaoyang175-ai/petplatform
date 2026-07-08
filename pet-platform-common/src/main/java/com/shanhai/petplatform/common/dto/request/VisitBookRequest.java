package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约看宠请求
 *
 * @author PetPlatform Team
 */
@Data
public class VisitBookRequest {

    /** 宠物ID */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    /** 预约探视时间 */
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;

    /** 备注/留言（可选） */
    private String note;

}
