-- 领养动态/领养故事社区模块表
CREATE TABLE IF NOT EXISTS t_feed (
    id          BIGINT       NOT NULL AUTO_INCREMENT        COMMENT '动态ID',
    author_id   BIGINT       NOT NULL                       COMMENT '作者用户ID',
    author_name VARCHAR(100)  NOT NULL                      COMMENT '作者昵称（冗余存储）',
    pet_id      BIGINT                                    COMMENT '关联宠物ID（可选）',
    content     VARCHAR(2000) NOT NULL                     COMMENT '动态内容',
    images      TEXT                                       COMMENT '图片URL列表（JSON数组字符串）',
    like_count  INT          NOT NULL DEFAULT 0             COMMENT '点赞数',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP           COMMENT '创建时间',
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_feed_author (author_id),
    INDEX idx_feed_created (created_at),
    CONSTRAINT fk_feed_author FOREIGN KEY (author_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_feed_pet FOREIGN KEY (pet_id) REFERENCES t_pet (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '领养动态/领养故事表';
