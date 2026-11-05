package com.must.secondhand.dto;

import com.must.secondhand.model.enums.ListingStatus;
import jakarta.validation.constraints.NotNull;

/** Target status for a listing transition (REQ-7.1). */
public record StatusForm(@NotNull(message = "Target status is required") ListingStatus status) {
}
