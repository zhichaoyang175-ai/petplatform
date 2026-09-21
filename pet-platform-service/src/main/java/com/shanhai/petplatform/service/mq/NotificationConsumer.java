package com.shanhai.petplatform.service.mq;

import com.shanhai.petplatform.common.constant.MqTopicConstant;
import com.shanhai.petplatform.common.constant.RedisKeyConstant;
import com.shanhai.petplatform.common.mq.NotificationMessage;
import com.shanhai.petplatform.infrastructure.cache.CacheService;
import com.shanhai.petplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 站内通知消费者（落点 B）。
 *
 * <p>消费通知消息，可靠投递到 {@code t_notification} 表。关键设计：</p>
 * <ul>
 *   <li><b>幂等去重</b>：以「关联业务 + 类型」为业务唯一键，通过 Redis SETNX 去重，
 *       规避 MQ 重复投递导致的重复通知。</li>
 *   <li><b>fail-open</b>：Redis 不可用时 {@code setIfAbsent} 返回 true（视为首次），
 *       通知照常写入，宁可重复也不丢消息。</li>
 * </ul>
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rocketmq.enabled", havingValue = "true")
@RocketMQMessageListener(topic = MqTopicConstant.TOPIC_NOTIFICATION,
        consumerGroup = "pet-platform-notification-consumer")
public class NotificationConsumer implements RocketMQListener<NotificationMessage> {

    private final NotificationService notificationService;
    private final CacheService cacheService;

    @Override
    public void onMessage(NotificationMessage message) {
        log.info("收到通知消息: userId={}, type={}, refId={}",
                message.getUserId(), message.getType(), message.getRefId());

        // 幂等去重：重复消息直接跳过
        String dedupKey = RedisKeyConstant.mqNotificationDedupKey(
                message.getRefType(), message.getRefId(), message.getType());
        if (!cacheService.setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS)) {
            log.info("重复通知消息，跳过: refType={}, refId={}, type={}",
                    message.getRefType(), message.getRefId(), message.getType());
            return;
        }

        notificationService.notify(message.getUserId(), message.getTitle(), message.getContent(),
                message.getType(), message.getRefType(), message.getRefId());
    }
}
