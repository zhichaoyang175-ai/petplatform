package com.shanhai.petplatform.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领养审核通过消息 — 用于异步生成回访计划（落点 A）。
 *
 * <p>说明：{@code adoptedDate} 使用字符串（yyyy-MM-dd）而非 {@code LocalDateTime}，
 * 以规避 MQ 默认序列化器未注册 JavaTimeModule 导致的时间序列化问题。</p>
 *
 * @author PetPlatform Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionApprovedMessage {

    /** 领养申请 ID */
    private Long applicationId;

    /** 领养记录 ID（已由审核事务强一致写入） */
    private Long adoptionRecordId;

    /** 回访期数（默认 12） */
    private Integer followUpMonths;

    /** 领养日期（yyyy-MM-dd），用于计算每期回访计划日期 */
    private String adoptedDate;

}
