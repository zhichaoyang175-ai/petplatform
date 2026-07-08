package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.response.NotificationVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public R<PageResult<NotificationVO>> list(@CurrentUser Long userId,
                                               @RequestParam(required = false) Integer readStatus,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return R.ok(notificationService.getNotifications(userId, readStatus, page, size));
    }

    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id, @CurrentUser Long userId) {
        notificationService.markAsRead(id, userId);
        return R.ok();
    }

    @PutMapping("/read-all")
    public R<Void> markAllRead(@CurrentUser Long userId) {
        notificationService.markAllAsRead(userId);
        return R.ok();
    }

    @GetMapping("/unread-count")
    public R<Map<String, Long>> unreadCount(@CurrentUser Long userId) {
        return R.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }
}
