-- 评价/信用表
CREATE TABLE IF NOT EXISTS t_evaluation (
    id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '评价ID',
    reviewer_id  BIGINT       NOT NULL                 COMMENT '评价人ID',
    target_type  TINYINT      NOT NULL                 COMMENT '评价目标类型: 1-用户 2-救助站',
    target_id    BIGINT       NOT NULL                 COMMENT '评价目标ID',
    score        TINYINT      NOT NULL                 COMMENT '评分: 1-5',
    comment      VARCHAR(500)                      COMMENT '评价内容',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                     COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_evaluation_target (target_type, target_id),
    INDEX idx_evaluation_reviewer (reviewer_id),
    CONSTRAINT fk_evaluation_reviewer FOREIGN KEY (reviewer_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价/信用表';
