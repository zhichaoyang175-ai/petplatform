package com.shanhai.petplatform.service.state.event;

import java.time.LocalDate;

/**
 * 领养申请已通过领域事件。
 *
 * <p>由领养状态机在「审核通过」成功、事务提交后发布。
 * 监听方据此：① 通知领养人；② 异步生成 12 期回访计划（走 MQ，可靠投递）。
 * 状态机不再直接依赖通知子系统与回访计划生成。</p>
 */
public record ApplicationApprovedEvent(
        Long applicationId,
        Long petId,
        Long applicantId,
        String petName,
        Long adoptionRecordId,
        Integer followUpMonths,
        LocalDate adoptedDate
) {
}
