package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.PageResult;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.NotificationView;
import com.must.secondhand.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** In-system notifications (REQ-8.1). */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResult<NotificationView>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        return ApiResponse.ok(notificationService.list(SessionSupport.requireUserId(session), page, size));
    }

    /** Unread counter for the bell plus the five latest entries. */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(HttpSession session) {
        Long userId = SessionSupport.requireUserId(session);
        List<NotificationView> latest = notificationService.latest(userId);
        return ApiResponse.ok(Map.of(
                "unread", notificationService.unreadCount(userId),
                "latest", latest));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id, HttpSession session) {
        notificationService.markRead(SessionSupport.requireUserId(session), id);
        return ApiResponse.ok("Marked as read", null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(HttpSession session) {
        notificationService.markAllRead(SessionSupport.requireUserId(session));
        return ApiResponse.ok("All notifications marked as read", null);
    }
}
