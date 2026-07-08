-- =====================================================
-- 宠物领养信息平台 — 开发测试种子数据
-- 密码 "123456" 的 BCrypt 哈希值（统一使用）
-- =====================================================

-- 1. 创建3个用户
INSERT IGNORE INTO t_user (id, phone, password, nickname, avatar_url, role, status, created_at, updated_at) VALUES
(1, '13800000001', '$2a$10$b/6wtY7oa1oV4ztgc2KB0.z3WK1jl81reu3FgPNB454rgPsz9kQmK', '爱心送养站', NULL, 2, 1, NOW(), NOW()),
(2, '13800000002', '$2a$10$b/6wtY7oa1oV4ztgc2KB0.z3WK1jl81reu3FgPNB454rgPsz9kQmK', '小王爱宠',   NULL, 1, 1, NOW(), NOW()),
(3, '13800000003', '$2a$10$b/6wtY7oa1oV4ztgc2KB0.z3WK1jl81reu3FgPNB454rgPsz9kQmK', '平台管理员',   NULL, 3, 1, NOW(), NOW());

-- 2. 插入6条宠物记录（owner_id=1 爱心送养站发布）
INSERT IGNORE INTO t_pet (id, owner_id, name, breed, gender, age_months, neutered, health_status, location_province, location_city, description, adoption_requirements, status, view_count, created_at, updated_at) VALUES
(1, 1, '布丁', '金毛寻回犬',   1, 24, 0, 1, '上海', '浦东新区',  '布丁是一只性格温顺的金毛寻回犬，非常亲人，喜欢和小朋友玩耍。已完成基础训练，会坐下、握手等指令。', '有固定住所和稳定收入；接受定期回访；家庭成员一致同意领养。', 0, 128, NOW(), NOW()),
(2, 1, '雪球', '布偶猫',       2, 12, 1, 1, '北京', '朝阳区',  '雪球是一只纯种布偶猫，性格温柔安静，喜欢被抱着。毛发柔软顺滑，眼睛湛蓝如宝石。', '有养猫经验优先；家中需封窗；接受定期回访。', 0, 256, NOW(), NOW()),
(3, 1, '豆豆', '柯基犬',       1, 36, 0, 1, '杭州', '西湖区',  '豆豆是一只活泼可爱的柯基犬，短腿大耳朵，非常会撒娇。已经做完绝育手术，身体健康。', '每天能保证遛狗时间；有养犬经验优先；接受定期回访。', 0, 64, NOW(), NOW()),
(4, 1, '米糕', '英国短毛猫',   2, 8,  0, 1, '深圳', '南山区',  '米糕是一只可爱的英短银渐层，圆脸大眼，性格粘人。已做驱虫和疫苗，非常健康。', '有养猫经验优先；家中需封窗；不笼养。', 0, 192, NOW(), NOW()),
(5, 1, '阿福', '柴犬',         1, 18, 0, 1, '成都', '武侯区',  '阿福是一只帅气的柴犬，性格独立但不冷漠，对主人非常忠诚。喜欢户外活动，精力充沛。', '每天能带出门活动；有养犬经验；不接受笼养。', 0, 80, NOW(), NOW()),
(6, 1, '咪咪', '橘猫',         2, 24, 1, 1, '广州', '天河区',  '咪咪是一只可爱的橘猫，性格温顺粘人，喜欢晒太阳和撒娇。已做驱虫疫苗和绝育。', '不笼养；家中需封窗；科学喂养不喂剩饭。', 0, 148, NOW(), NOW());

-- 3. 插入领养申请记录（applicant_id=2 小王申请）
INSERT IGNORE INTO t_adoption_application (id, pet_id, applicant_id, housing_type, pet_experience, family_attitude, current_pets, reason, status, created_at, updated_at) VALUES
(1, 1, 2, 1, 1, '全家支持', '无', '一直想养一只金毛，已经做了充分准备', 1, NOW(), NOW()),
(2, 2, 2, 1, 1, '全家支持', '无', '很喜欢布偶猫温顺的性格', 2, NOW(), NOW());

-- 4. 插入领养记录（申请2已通过 → 雪球领养记录）
INSERT IGNORE INTO t_adoption_record (id, application_id, pet_id, adopter_id, applicant_id, adopted_at, status, follow_up_months, created_at) VALUES
(1, 2, 2, 1, 2, NOW(), 0, 12, NOW());
