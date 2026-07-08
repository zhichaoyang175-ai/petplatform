package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shanhai.petplatform.repository.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知 Mapper — 包含批量操作
 *
 * @author PetPlatform Team
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    int countUnread(@Param("userId") Long userId);

    /**
     * 批量标记通知为已读
     *
     * @param userId 用户ID
     * @param ids    通知ID列表
     * @return 影响行数
     */
    int updateReadStatus(@Param("userId") Long userId, @Param("ids") List<Long> ids);

}
