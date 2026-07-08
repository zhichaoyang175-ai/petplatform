package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.response.NotificationVO;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.Notification;
import com.shanhai.petplatform.repository.mapper.NotificationMapper;
import com.shanhai.petplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public void notify(Long userId, String title, String content, Integer type, String refType, Long refId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setReadStatus(0);
        n.setRefType(refType);
        n.setRefId(refId);
        notificationMapper.insert(n);
        log.info("通知发送: userId={}, type={}, title={}", userId, type, title);
    }

    @Override
    public PageResult<NotificationVO> getNotifications(Long userId, Integer readStatus, int page, int size) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId);
        if (readStatus != null) w.eq(Notification::getReadStatus, readStatus);
        w.orderByDesc(Notification::getCreatedAt);

        Page<Notification> p = notificationMapper.selectPage(new Page<>(page, size), w);
        List<NotificationVO> vos = p.getRecords().stream()
                .map(n -> NotificationVO.of(n.getId(), n.getTitle(), n.getContent(), n.getType(),
                        n.getReadStatus(), n.getRefType(), n.getRefId(), n.getCreatedAt()))
                .toList();
        return PageResult.of(vos, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null || !n.getUserId().equals(userId)) throw new NotFoundException("通知不存在");
        n.setReadStatus(1);
        notificationMapper.updateById(n);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.updateReadStatus(userId, null);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }
}
