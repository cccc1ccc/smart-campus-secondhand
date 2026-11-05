package com.must.secondhand.service;

import com.must.secondhand.common.PageResult;
import com.must.secondhand.dto.NotificationView;
import com.must.secondhand.model.entity.Notification;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.model.enums.NotificationType;
import com.must.secondhand.repository.NotificationRepository;
import com.must.secondhand.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Creates and reads in-system notifications (REQ-8.1).
 * A failing notification never rolls back the business operation, it is logged and retried once.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final Duration RETENTION = Duration.ofDays(90);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /** Runs in its own transaction so a notification failure cannot break the caller. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyUser(Long userId, NotificationType type, String title, String body,
                           Long listingId, Long requestId) {
        try {
            createOnce(userId, type, title, body, listingId, requestId);
        } catch (RuntimeException first) {
            log.warn("Notification failed for user {}, retrying once: {}", userId, first.getMessage());
            try {
                createOnce(userId, type, title, body, listingId, requestId);
            } catch (RuntimeException second) {
                log.error("Notification abandoned for user {} ({}): {}", userId, type, second.getMessage());
            }
        }
    }

    private void createOnce(Long userId, NotificationType type, String title, String body,
                            Long listingId, Long requestId) {
        User user = userRepository.findById(userId).orElseThrow();
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setListingId(listingId);
        notification.setRequestId(requestId);
        notification.setCreatedAt(Instant.now());
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PageResult<NotificationView> list(Long userId, int page, int size) {
        var result = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50)));
        return PageResult.of(result, NotificationView::of);
    }

    @Transactional(readOnly = true)
    public List<NotificationView> latest(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5))
                .stream().map(NotificationView::of).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .ifPresent(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    /** Called by the daily cleanup job: notifications are kept for 90 days. */
    @Transactional
    public int purgeExpired() {
        int removed = notificationRepository.deleteOlderThan(Instant.now().minus(RETENTION));
        if (removed > 0) {
            log.info("Purged {} notifications older than {} days", removed, RETENTION.toDays());
        }
        return removed;
    }
}
