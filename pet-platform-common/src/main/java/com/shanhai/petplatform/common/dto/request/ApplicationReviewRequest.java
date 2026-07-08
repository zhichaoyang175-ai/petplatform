package com.shanhai.petplatform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核操作请求 — 通过或驳回
 *
 * @author PetPlatform Team
 */
@Data
public class ApplicationReviewRequest {

    /** 操作: "approve" 或 "reject" */
    @NotBlank(message = "操作类型不能为空")
    private String action;

    /** 驳回原因（驳回时必填） */
    private String rejectReason;

}
