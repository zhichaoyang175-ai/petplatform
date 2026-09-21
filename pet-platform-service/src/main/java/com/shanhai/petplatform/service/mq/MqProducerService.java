package com.shanhai.petplatform.service.mq;

import com.shanhai.petplatform.common.constant.MqTopicConstant;
import com.shanhai.petplatform.common.mq.AdoptionApprovedMessage;
import com.shanhai.petplatform.common.mq.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * RocketMQ 生产者封装 — 统一消息发送入口。
 *
 * <p>仅在 {@code app.rocketmq.enabled=true} 时装配；默认关闭，
 * 配合 {@code AdoptionNotificationListener} 的降级逻辑，保证无 MQ 环境应用可正常启动。</p>
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rocketmq.enabled", havingValue = "true")
public class MqProducerService {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 发送「回访计划生成」消息（落点 A）。
     */
    public void sendFollowUpPlan(Long applicationId, Long adoptionRecordId,
                                 Integer followUpMonths, LocalDate adoptedDate) {
        AdoptionApprovedMessage message = new AdoptionApprovedMessage(
                applicationId, adoptionRecordId, followUpMonths, adoptedDate.toString());
        rocketMQTemplate.convertAndSend(MqTopicConstant.TOPIC_ADOPTION_FOLLOW_UP, message);
        log.info("发送回访计划生成消息: recordId={}, months={}", adoptionRecordId, followUpMonths);
    }

    /**
     * 发送「站内通知」消息（落点 B）。
     */
    public void sendNotification(NotificationMessage message) {
        rocketMQTemplate.convertAndSend(MqTopicConstant.TOPIC_NOTIFICATION, message);
        log.info("发送通知消息: userId={}, type={}, refId={}",
                message.getUserId(), message.getType(), message.getRefId());
    }

}
