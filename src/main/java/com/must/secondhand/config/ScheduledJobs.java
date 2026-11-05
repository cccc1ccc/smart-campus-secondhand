package com.must.secondhand.config;

import com.must.secondhand.service.MatchingService;
import com.must.secondhand.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Background jobs: matching retry (REQ-5.1) and notification retention (REQ-8.1). */
@Component
public class ScheduledJobs {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobs.class);

    private final MatchingService matchingService;
    private final NotificationService notificationService;

    public ScheduledJobs(MatchingService matchingService, NotificationService notificationService) {
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    /** Safety net if an event-driven matching run failed. */
    @Scheduled(initialDelay = 120_000, fixedDelay = 600_000)
    public void retryMatching() {
        try {
            matchingService.runAll();
        } catch (RuntimeException ex) {
            log.error("Scheduled matching retry failed", ex);
        }
    }

    /** Notifications are kept for 90 days. */
    @Scheduled(cron = "0 30 3 * * *")
    public void purgeNotifications() {
        try {
            notificationService.purgeExpired();
        } catch (RuntimeException ex) {
            log.error("Notification purge failed", ex);
        }
    }
}
