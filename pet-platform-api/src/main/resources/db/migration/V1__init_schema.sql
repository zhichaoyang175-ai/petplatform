-- =====================================================
-- 宠物领养信息平台 — 数据库初始化脚本
-- 数据库: pet_platform
-- 字符集: utf8mb4 | 排序规则: utf8mb4_unicode_ci
-- 引擎:   InnoDB
-- =====================================================

-- =====================================================
-- 1. t_user — 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    phone       VARCHAR(20)  NOT NULL                 COMMENT '手机号',
    password    VARCHAR(128) NOT NULL                 COMMENT '密码(BCrypt)',
    nickname    VARCHAR(50)  NOT NULL                 COMMENT '昵称',
    avatar_url  VARCHAR(255)     NULL                 COMMENT '头像URL',
    real_name   VARCHAR(50)      NULL                 COMMENT '真实姓名',
    id_card     VARCHAR(18)      NULL                 COMMENT '身份证号(加密存储)',
    email       VARCHAR(100)     NULL                 COMMENT '邮箱',
    role        TINYINT      NOT NULL DEFAULT 1       COMMENT '角色: 1-领养人 2-送养人 3-管理员',
    status      TINYINT      NOT NULL DEFAULT 1       COMMENT '状态: 1-正常 0-禁用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY  uk_user_phone (phone),
    INDEX       idx_user_status (status),
    INDEX       idx_user_role (role),
    INDEX       idx_user_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- =====================================================
-- 2. t_pet — 宠物信息表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_pet (
    id                    BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '宠物ID',
    owner_id              BIGINT       NOT NULL                 COMMENT '送养人ID',
    name                  VARCHAR(50)  NOT NULL                 COMMENT '宠物名称',
    breed                 VARCHAR(50)  NOT NULL                 COMMENT '品种',
    gender                TINYINT      NOT NULL                 COMMENT '性别: 0-未知 1-公 2-母',
    age_months            INT              NULL                 COMMENT '月龄',
    weight_kg             DECIMAL(5,2)     NULL                 COMMENT '体重(kg)',
    neutered              TINYINT      NOT NULL DEFAULT 0       COMMENT '是否绝育: 0-否 1-是',
    health_status         TINYINT      NOT NULL DEFAULT 1       COMMENT '健康状态: 1-健康 2-轻微疾病 3-治疗中 4-残疾',
    location_province     VARCHAR(20)      NULL                 COMMENT '所在省',
    location_city         VARCHAR(20)      NULL                 COMMENT '所在市',
    description           TEXT             NULL                 COMMENT '详细描述',
    adoption_requirements TEXT             NULL                 COMMENT '领养要求',
    status                TINYINT      NOT NULL DEFAULT 0       COMMENT '状态: 0-待领养 1-申请中 2-审核中 3-已领养 4-回访中 5-已完成 6-已下架',
    view_count            INT          NOT NULL DEFAULT 0       COMMENT '浏览次数',
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
    updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX       idx_pet_owner (owner_id),
    INDEX       idx_pet_breed (breed),
    INDEX       idx_pet_gender (gender),
    INDEX       idx_pet_age_months (age_months),
    INDEX       idx_pet_neutered (neutered),
    INDEX       idx_pet_status (status),
    INDEX       idx_pet_province (location_province),
    INDEX       idx_pet_city (location_city),
    INDEX       idx_pet_view_count (view_count),
    INDEX       idx_pet_breed_age (breed, age_months),
    INDEX       idx_pet_location (location_province, location_city),
    INDEX       idx_pet_status_neutered (status, neutered),
    CONSTRAINT  fk_pet_owner FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物信息表';


-- =====================================================
-- 3. t_pet_image — 宠物图片表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_pet_image (
    id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '图片ID',
    pet_id      BIGINT        NOT NULL                 COMMENT '宠物ID',
    image_url   VARCHAR(500)  NOT NULL                 COMMENT '图片URL/路径',
    image_type  TINYINT       NOT NULL DEFAULT 1       COMMENT '类型: 1-宠物照片 2-回访照片',
    sort_order  INT           NOT NULL DEFAULT 0       COMMENT '排序',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX       idx_pet_image_pet (pet_id),
    INDEX       idx_pet_image_type (image_type),
    CONSTRAINT  fk_pet_image_pet FOREIGN KEY (pet_id) REFERENCES t_pet(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物图片表';


-- =====================================================
-- 4. t_adoption_application — 领养申请表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_adoption_application (
    id               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '申请ID',
    pet_id           BIGINT        NOT NULL                 COMMENT '宠物ID',
    applicant_id     BIGINT        NOT NULL                 COMMENT '申请人ID',
    housing_type     TINYINT       NOT NULL                 COMMENT '住房类型: 1-自有房 2-租房 3-与家人同住',
    monthly_income   DECIMAL(10,2)     NULL                 COMMENT '月收入',
    pet_experience   TINYINT       NOT NULL                 COMMENT '养宠经验: 0-无 1-有过 2-正在养',
    family_attitude  VARCHAR(100)  NOT NULL                 COMMENT '家人态度',
    current_pets     VARCHAR(200)      NULL                 COMMENT '现有宠物情况',
    reason           TEXT              NULL                 COMMENT '申请理由',
    status           TINYINT       NOT NULL DEFAULT 0       COMMENT '状态: 0-待审核 1-审核中 2-已通过 3-已驳回 4-已取消',
    reject_reason    TEXT              NULL                 COMMENT '驳回原因',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_application_pet_applicant (pet_id, applicant_id),
    INDEX      idx_application_pet (pet_id),
    INDEX      idx_application_applicant (applicant_id),
    INDEX      idx_application_status (status),
    INDEX      idx_application_created_at (created_at),
    CONSTRAINT fk_application_pet       FOREIGN KEY (pet_id)       REFERENCES t_pet(id)  ON DELETE CASCADE,
    CONSTRAINT fk_application_applicant FOREIGN KEY (applicant_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养申请表';


-- =====================================================
-- 5. t_adoption_record — 领养记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_adoption_record (
    id               BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    application_id   BIGINT   NOT NULL                 COMMENT '申请ID',
    pet_id           BIGINT   NOT NULL                 COMMENT '宠物ID',
    adopter_id       BIGINT   NOT NULL                 COMMENT '送养人ID',
    applicant_id     BIGINT   NOT NULL                 COMMENT '领养人ID',
    adopted_at       DATETIME NOT NULL                 COMMENT '领养日期',
    status           TINYINT  NOT NULL DEFAULT 0       COMMENT '状态: 0-领养中 1-回访中 2-已完成',
    follow_up_months INT      NOT NULL DEFAULT 12      COMMENT '回访总月数',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_adoption_record_application (application_id),
    INDEX      idx_adoption_record_pet (pet_id),
    INDEX      idx_adoption_record_adopter (adopter_id),
    INDEX      idx_adoption_record_applicant (applicant_id),
    INDEX      idx_adoption_record_status (status),
    CONSTRAINT fk_adoption_record_application FOREIGN KEY (application_id) REFERENCES t_adoption_application(id) ON DELETE CASCADE,
    CONSTRAINT fk_adoption_record_pet         FOREIGN KEY (pet_id)         REFERENCES t_pet(id)                  ON DELETE CASCADE,
    CONSTRAINT fk_adoption_record_adopter     FOREIGN KEY (adopter_id)     REFERENCES t_user(id)                 ON DELETE CASCADE,
    CONSTRAINT fk_adoption_record_applicant   FOREIGN KEY (applicant_id)   REFERENCES t_user(id)                 ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养记录表';


-- =====================================================
-- 6. t_follow_up_task — 回访任务表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_follow_up_task (
    id                  BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '任务ID',
    adoption_record_id  BIGINT   NOT NULL                 COMMENT '领养记录ID',
    period_number       INT      NOT NULL                 COMMENT '第几次回访(1/2/3...)',
    scheduled_date      DATE     NOT NULL                 COMMENT '计划回访日期',
    due_date            DATE     NOT NULL                 COMMENT '截止日期',
    status              TINYINT  NOT NULL DEFAULT 0       COMMENT '状态: 0-待执行 1-已提醒 2-已完成 3-已逾期',
    notified_at         DATETIME     NULL                 COMMENT '提醒通知时间',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX      idx_follow_up_task_record (adoption_record_id),
    INDEX      idx_follow_up_task_status (status),
    INDEX      idx_follow_up_task_due_date (due_date),
    INDEX      idx_follow_up_task_scheduled (scheduled_date),
    CONSTRAINT fk_follow_up_task_record FOREIGN KEY (adoption_record_id) REFERENCES t_adoption_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回访任务表';


-- =====================================================
-- 7. t_follow_up_record — 回访记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_follow_up_record (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    task_id      BIGINT        NOT NULL                 COMMENT '任务ID(一对一)',
    user_id      BIGINT        NOT NULL                 COMMENT '上传用户ID',
    content      TEXT              NULL                 COMMENT '文字描述',
    image_url    VARCHAR(500)      NULL                 COMMENT '回访照片URL',
    submitted_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_follow_up_record_task (task_id),
    INDEX      idx_follow_up_record_user (user_id),
    CONSTRAINT fk_follow_up_record_task FOREIGN KEY (task_id) REFERENCES t_follow_up_task(id) ON DELETE CASCADE,
    CONSTRAINT fk_follow_up_record_user FOREIGN KEY (user_id) REFERENCES t_user(id)          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回访记录表';


-- =====================================================
-- 8. t_notification — 通知表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_notification (
    id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    user_id     BIGINT        NOT NULL                 COMMENT '接收用户ID',
    title       VARCHAR(100)  NOT NULL                 COMMENT '通知标题',
    content     TEXT              NULL                 COMMENT '通知内容',
    type        TINYINT       NOT NULL                 COMMENT '类型: 1-申请状态 2-审核结果 3-回访提醒 4-系统通知',
    read_status TINYINT       NOT NULL DEFAULT 0       COMMENT '阅读状态: 0-未读 1-已读',
    ref_type    VARCHAR(30)       NULL                 COMMENT '关联类型: application/pet/follow_up',
    ref_id      BIGINT            NULL                 COMMENT '关联ID',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX      idx_notification_user (user_id),
    INDEX      idx_notification_type (type),
    INDEX      idx_notification_read (read_status),
    INDEX      idx_notification_user_read (user_id, read_status),
    INDEX      idx_notification_created_at (created_at),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';


-- =====================================================
-- 9. t_favorite — 收藏表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_favorite (
    id         BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '收藏ID',
    user_id    BIGINT   NOT NULL                 COMMENT '用户ID',
    pet_id     BIGINT   NOT NULL                 COMMENT '宠物ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_favorite_user_pet (user_id, pet_id),
    INDEX      idx_favorite_user (user_id),
    INDEX      idx_favorite_pet (pet_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_pet  FOREIGN KEY (pet_id)  REFERENCES t_pet(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';


-- =====================================================
-- ShedLock 分布式定时任务锁表
-- =====================================================
CREATE TABLE IF NOT EXISTS shedlock (
    name       VARCHAR(64)  NOT NULL                COMMENT '锁名称',
    lock_until TIMESTAMP(3) NOT NULL                COMMENT '锁定截止时间',
    locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '锁定时间',
    locked_by  VARCHAR(255) NOT NULL                COMMENT '锁定者标识',
    PRIMARY KEY (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ShedLock分布式调度锁';
