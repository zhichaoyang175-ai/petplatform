package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.response.NotificationVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 通知服务接口
 */
public interface NotificationService {

    void notify(Long userId, String title, String content, Integer type, String refType, Long refId);

    PageResult<NotificationVO> getNotifications(Long userId, Integer readStatus, int page, int size);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    long getUnreadCount(Long userId);

}
