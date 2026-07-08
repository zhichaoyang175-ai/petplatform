-- 通用审核任务表（送养人认证 / 救助站认证等审核流程统一入口）
CREATE TABLE IF NOT EXISTS t_review_task (
    id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '审核任务ID',
    applicant_id BIGINT       NOT NULL                 COMMENT '申请人ID',
    type         TINYINT      NOT NULL DEFAULT 1       COMMENT '类型: 1-送养人认证 2-救助站认证',
    status       TINYINT      NOT NULL DEFAULT 0       COMMENT '状态: 0-待审核 1-已通过 2-已驳回 3-已取消',
    title        VARCHAR(100) NOT NULL                 COMMENT '审核标题',
    submit_data  TEXT                              COMMENT '提交资料(JSON)',
    reviewer_id  BIGINT                            COMMENT '审核人ID',
    review_comment VARCHAR(500)                    COMMENT '审核意见',
    reviewed_at  DATETIME                           COMMENT '审核时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_review_status (status),
    INDEX idx_review_applicant (applicant_id),
    CONSTRAINT fk_review_applicant FOREIGN KEY (applicant_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通用审核任务表';
