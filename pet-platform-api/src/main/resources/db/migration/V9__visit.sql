-- 预约看宠（线下探视）表
CREATE TABLE IF NOT EXISTS t_visit_appointment (
    id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '预约ID',
    pet_id         BIGINT       NOT NULL                 COMMENT '宠物ID',
    applicant_id   BIGINT       NOT NULL                 COMMENT '申请人ID（领养人）',
    owner_id       BIGINT       NOT NULL                 COMMENT '宠物主人/送养人ID',
    appointment_time DATETIME    NOT NULL                 COMMENT '预约探视时间',
    status         TINYINT      NOT NULL DEFAULT 0       COMMENT '状态: 0-待确认 1-已确认 2-已完成 3-已取消',
    note           VARCHAR(500)                      COMMENT '备注/留言',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_visit_applicant (applicant_id),
    INDEX idx_visit_owner (owner_id),
    INDEX idx_visit_pet (pet_id),
    INDEX idx_visit_status (status),
    CONSTRAINT fk_visit_pet FOREIGN KEY (pet_id) REFERENCES t_pet (id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_applicant FOREIGN KEY (applicant_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_owner FOREIGN KEY (owner_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约看宠（线下探视）表';
