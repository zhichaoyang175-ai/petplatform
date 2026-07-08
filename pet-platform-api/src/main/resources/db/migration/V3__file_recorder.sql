-- ============================================================
-- V3: 文件上传记录表 file_info（dromara x-file-storage FileRecorder）
-- ============================================================
-- 注意：当前项目 pom 中 Flyway 依赖处于注释状态，未启用自动迁移。
-- 因此本脚本需由 DBA / 运维在对应环境手动执行，或待 Flyway 启用后自动纳入。
-- 字段严格对齐 dromara 2.3.0 org.dromara.x.file.storage.core.FileInfo 的真实结构。

CREATE TABLE IF NOT EXISTS `file_info` (
    `id`                  VARCHAR(64)    NOT NULL COMMENT '文件ID（dromara 生成）',
    `url`                 VARCHAR(1024)  DEFAULT NULL COMMENT '文件访问地址',
    `size`                BIGINT         DEFAULT NULL COMMENT '文件大小（字节）',
    `filename`            VARCHAR(255)   DEFAULT NULL COMMENT '文件名',
    `original_filename`   VARCHAR(255)   DEFAULT NULL COMMENT '原始文件名',
    `base_path`           VARCHAR(512)   DEFAULT NULL COMMENT '基础路径',
    `path`                VARCHAR(1024)  DEFAULT NULL COMMENT '路径',
    `ext`                 VARCHAR(32)    DEFAULT NULL COMMENT '文件后缀',
    `content_type`        VARCHAR(128)   DEFAULT NULL COMMENT 'Content-Type',
    `platform`            VARCHAR(64)    DEFAULT NULL COMMENT '存储平台',
    `th_url`              VARCHAR(1024)  DEFAULT NULL COMMENT '缩略图访问地址',
    `th_filename`         VARCHAR(255)   DEFAULT NULL COMMENT '缩略图文件名',
    `th_size`             BIGINT         DEFAULT NULL COMMENT '缩略图大小（字节）',
    `th_content_type`     VARCHAR(128)   DEFAULT NULL COMMENT '缩略图 Content-Type',
    `object_id`           VARCHAR(255)   DEFAULT NULL COMMENT '对象ID',
    `object_type`         VARCHAR(64)    DEFAULT NULL COMMENT '对象类型',
    `metadata`            LONGTEXT       DEFAULT NULL COMMENT '元数据（JSON）',
    `user_metadata`       LONGTEXT       DEFAULT NULL COMMENT '用户元数据（JSON）',
    `th_metadata`         LONGTEXT       DEFAULT NULL COMMENT '缩略图元数据（JSON）',
    `th_user_metadata`    LONGTEXT       DEFAULT NULL COMMENT '缩略图用户元数据（JSON）',
    `attr`                LONGTEXT       DEFAULT NULL COMMENT '附加属性（JSON, hutool Dict）',
    `file_acl`            LONGTEXT       DEFAULT NULL COMMENT '文件 ACL（JSON）',
    `th_file_acl`         LONGTEXT       DEFAULT NULL COMMENT '缩略图 ACL（JSON）',
    `hash_info`           LONGTEXT       DEFAULT NULL COMMENT '哈希信息（JSON）',
    `upload_id`           VARCHAR(255)   DEFAULT NULL COMMENT '分片上传ID',
    `upload_status`       INT            DEFAULT NULL COMMENT '上传状态',
    `create_time`         DATETIME       DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_info_url` (`url`(255))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文件上传记录表';
