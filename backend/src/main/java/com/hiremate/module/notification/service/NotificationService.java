package com.hiremate.module.notification.service;

import com.hiremate.domain.entity.Notification;
import com.hiremate.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Notification createNotification(User user, String title, String message, String type);

    Page<Notification> getUserNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
