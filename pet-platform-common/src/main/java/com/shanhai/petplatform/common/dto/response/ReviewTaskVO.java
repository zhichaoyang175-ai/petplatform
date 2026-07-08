package com.shanhai.petplatform.common.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核任务视图对象
 *
 * @author PetPlatform Team
 */
@Data
public class ReviewTaskVO {

    private Long id;
    private Long applicantId;
    private String applicantName;
    private Integer type;
    private String typeName;
    private Integer status;
    private String statusName;
    private String title;
    private String submitData;
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
