package com.shanhai.petplatform.service.mq;

import com.shanhai.petplatform.common.constant.MqTopicConstant;
import com.shanhai.petplatform.common.mq.AdoptionApprovedMessage;
import com.shanhai.petplatform.service.FollowUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 回访计划生成消费者（落点 A）。
 *
 * <p>消费「审核通过」消息，异步生成 12 期回访计划。关键设计：</p>
 * <ul>
 *   <li><b>幂等</b>：消费前先 {@code hasFollowUpPlan} 判断是否已生成，避免 MQ 重复投递导致重复建计划。</li>
 *   <li><b>原子</b>：生成逻辑在 {@code FollowUpService} 内以 {@code @Transactional} 包裹，失败回滚，
 *       由 MQ 重试机制重新消费，保证 12 期要么全生成、要么全不生成。</li>
 * </ul>
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rocketmq.enabled", havingValue = "true")
@RocketMQMessageListener(topic = MqTopicConstant.TOPIC_ADOPTION_FOLLOW_UP,
        consumerGroup = "pet-platform-followup-consumer")
public class AdoptionFollowUpConsumer implements RocketMQListener<AdoptionApprovedMessage> {

    private final FollowUpService followUpService;

    @Override
    public void onMessage(AdoptionApprovedMessage message) {
        log.info("收到回访计划生成消息: recordId={}, months={}",
                message.getAdoptionRecordId(), message.getFollowUpMonths());

        // 幂等：已生成则跳过（MQ at-least-once 语义下的重复投递）
        if (followUpService.hasFollowUpPlan(message.getAdoptionRecordId())) {
            log.info("回访计划已生成，跳过重复消息: recordId={}", message.getAdoptionRecordId());
            return;
        }

        followUpService.generateFollowUpPlan(message.getAdoptionRecordId(), message.getFollowUpMonths());
        log.info("回访计划生成完成: recordId={}", message.getAdoptionRecordId());
    }
}
