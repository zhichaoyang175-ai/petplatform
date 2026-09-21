package com.shanhai.petplatform.service.listener;

import com.shanhai.petplatform.common.mq.NotificationMessage;
import com.shanhai.petplatform.service.FollowUpService;
import com.shanhai.petplatform.service.NotificationService;
import com.shanhai.petplatform.service.mq.MqProducerService;
import com.shanhai.petplatform.service.state.event.ApplicationApprovedEvent;
import com.shanhai.petplatform.service.state.event.ApplicationRejectedEvent;
import com.shanhai.petplatform.service.state.event.ApplicationSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 领养领域事件监听器 — 事件 → RocketMQ 的桥接层（含优雅降级）。
 *
 * <p>职责：把「状态流转」与「通知 / 回访计划」彻底解耦。状态机只负责发布领域事件，
 * 本监听器在事务提交后（AFTER_COMMIT）把事件翻译成 MQ 消息，交由消费者异步处理：</p>
 * <ul>
 *   <li><b>落点 A</b>：审核通过 → 发「回访计划生成」消息，消费者异步生成 12 期回访计划。</li>
 *   <li><b>落点 B</b>：申请提交 / 通过 / 驳回 → 发「站内通知」消息，消费者可靠投递。</li>
 * </ul>
 *
 * <p><b>优雅降级</b>：通过 {@link ObjectProvider} 注入 {@link MqProducerService}，
 * 当 {@code app.rocketmq.enabled=false}（默认，未接入 MQ）时该 Bean 不存在，
 * 回退为原来的同步执行（通知直接写库、回访计划同步生成），保证应用在无 MQ 环境仍可正常启动与运行。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdoptionNotificationListener {

    private final NotificationService notificationService;
    private final FollowUpService followUpService;
    private final ObjectProvider<MqProducerService> mqProducerProvider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        sendNotification(event.ownerId(), "新的领养申请",
                "有人申请领养您的宠物「" + event.petName() + "」", 1, "application", event.applicationId());
        log.info("事件驱动通知-申请提交: appId={}, ownerId={}", event.applicationId(), event.ownerId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationApproved(ApplicationApprovedEvent event) {
        // 落点 A：异步生成回访计划（MQ 可用发消息，不可用同步降级）
        MqProducerService mq = mqProducerProvider.getIfAvailable();
        if (mq != null) {
            mq.sendFollowUpPlan(event.applicationId(), event.adoptionRecordId(),
                    event.followUpMonths(), event.adoptedDate());
        } else {
            followUpService.generateFollowUpPlan(event.adoptionRecordId(), event.followUpMonths());
        }

        // 落点 B：通知可靠投递
        sendNotification(event.applicantId(), "申请已通过",
                "恭喜！您的领养申请（" + event.petName() + "）已通过审核", 2, "application", event.applicationId());
        log.info("事件驱动通知-申请通过: appId={}, applicantId={}", event.applicationId(), event.applicantId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationRejected(ApplicationRejectedEvent event) {
        sendNotification(event.applicantId(), "申请被驳回",
                "您的领养申请（" + event.petName() + "）已被驳回" +
                        (event.rejectReason() != null ? "，原因：" + event.rejectReason() : ""),
                2, "application", event.applicationId());
        log.info("事件驱动通知-申请驳回: appId={}, applicantId={}", event.applicationId(), event.applicantId());
    }

    /**
     * 统一发送通知：MQ 可用则走可靠投递，不可用则同步写库。
     */
    private void sendNotification(Long userId, String title, String content,
                                  Integer type, String refType, Long refId) {
        MqProducerService mq = mqProducerProvider.getIfAvailable();
        if (mq != null) {
            mq.sendNotification(new NotificationMessage(userId, title, content, type, refType, refId));
        } else {
            notificationService.notify(userId, title, content, type, refType, refId);
        }
    }
}
