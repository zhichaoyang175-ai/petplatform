-- =====================================================
-- 5. t_shelter — 救助站表
-- 依赖: t_user（创建者）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_shelter (
    id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '救助站ID',
    owner_user_id BIGINT       NOT NULL                 COMMENT '创建者用户ID（关联 t_user，救助站由送养人创建）',
    name          VARCHAR(100) NOT NULL                 COMMENT '救助站名称',
    description   TEXT             NULL                 COMMENT '简介/描述',
    address       VARCHAR(255)     NULL                 COMMENT '详细地址',
    province      VARCHAR(20)      NULL                 COMMENT '所在省',
    city          VARCHAR(20)      NULL                 COMMENT '所在市',
    contact_phone VARCHAR(20)      NULL                 COMMENT '联系手机号',
    logo_url      VARCHAR(500)     NULL                 COMMENT 'LOGO/头像URL',
    status        TINYINT      NOT NULL DEFAULT 0       COMMENT '状态: 0-待审核 1-已通过 2-已驳回',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX       idx_shelter_owner (owner_user_id),
    INDEX       idx_shelter_province (province),
    INDEX       idx_shelter_city (city),
    INDEX       idx_shelter_status (status),
    INDEX       idx_shelter_created_at (created_at),
    CONSTRAINT  fk_shelter_owner FOREIGN KEY (owner_user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='救助站表';
