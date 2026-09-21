package com.shanhai.petplatform.service.state.event;

/**
 * 领养申请已驳回领域事件。
 *
 * <p>由领养状态机在「审核驳回」成功、事务提交后发布。
 * 监听方据此通知领养人，使状态机不再直接依赖通知子系统。
 */
public record ApplicationRejectedEvent(
        Long applicationId,
        Long petId,
        Long applicantId,
        String petName,
        String rejectReason
) {
}
