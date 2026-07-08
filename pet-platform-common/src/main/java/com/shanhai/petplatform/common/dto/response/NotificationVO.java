package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shanhai.petplatform.common.enums.NotificationTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知 VO
 *
 * @author PetPlatform Team
 */
@Data
public class NotificationVO {

    /** 通知ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型 code */
    private Integer type;

    /** 类型名称 */
    private String typeName;

    /** 是否已读 */
    private Boolean readStatus;

    /** 关联类型 */
    private String refType;

    /** 关联ID */
    private Long refId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 构建 VO */
    public static NotificationVO of(Long id, String title, String content, Integer type,
                                     Integer readStatus, String refType, Long refId,
                                     LocalDateTime createdAt) {
        NotificationVO vo = new NotificationVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setContent(content);
        vo.setType(type);
        vo.setTypeName(NotificationTypeEnum.fromCode(type).getDesc());
        vo.setReadStatus(readStatus != null && readStatus == 1);
        vo.setRefType(refType);
        vo.setRefId(refId);
        vo.setCreatedAt(createdAt);
        return vo;
    }

}
