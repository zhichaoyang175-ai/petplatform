package com.shanhai.petplatform.common.constant;

/**
 * RocketMQ Topic 常量 — 统一管理消息主题，避免硬编码。
 *
 * @author PetPlatform Team
 */
public final class MqTopicConstant {

    private MqTopicConstant() {}

    /** 审核通过 → 异步生成回访计划（落点 A） */
    public static final String TOPIC_ADOPTION_FOLLOW_UP = "adoption-followup-topic";

    /** 站内通知可靠投递（落点 B） */
    public static final String TOPIC_NOTIFICATION = "notification-topic";

}
