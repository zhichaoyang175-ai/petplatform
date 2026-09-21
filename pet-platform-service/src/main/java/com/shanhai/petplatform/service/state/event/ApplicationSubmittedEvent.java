package com.shanhai.petplatform.service.state.event;

/**
 * 领养申请已提交领域事件。
 *
 * <p>由领养状态机在「提交申请」成功、事务提交后发布。
 * 监听方据此通知送养人，使状态机不再直接依赖通知子系统。
 */
public record ApplicationSubmittedEvent(
        Long applicationId,
        Long petId,
        Long ownerId,
        String petName
) {
}
