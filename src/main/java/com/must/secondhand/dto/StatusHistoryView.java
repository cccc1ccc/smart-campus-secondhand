package com.must.secondhand.dto;

import java.time.Instant;

/** One recorded status transition (REQ-7.1). */
public record StatusHistoryView(String fromStatus, String toStatus, Instant changedAt) {
}
