package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回访记录 VO
 *
 * @author PetPlatform Team
 */
@Data
public class FollowUpRecordVO {

    /** 记录ID */
    private Long id;

    /** 任务ID */
    private Long taskId;

    /** 文字描述 */
    private String content;

    /** 照片URL */
    private String imageUrl;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    /** 构建 VO */
    public static FollowUpRecordVO of(Long id, Long taskId, String content,
                                       String imageUrl, LocalDateTime submittedAt) {
        FollowUpRecordVO vo = new FollowUpRecordVO();
        vo.setId(id);
        vo.setTaskId(taskId);
        vo.setContent(content);
        vo.setImageUrl(imageUrl);
        vo.setSubmittedAt(submittedAt);
        return vo;
    }

}
