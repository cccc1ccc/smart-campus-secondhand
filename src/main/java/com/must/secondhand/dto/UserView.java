package com.must.secondhand.dto;

import com.must.secondhand.model.entity.User;

import java.time.Instant;

/** Owner-only view of the account (SEC-1.2). */
public record UserView(
        Long id,
        String name,
        String studentNo,
        String email,
        String contact,
        Instant createdAt
) {

    public static UserView of(User user) {
        return new UserView(user.getId(), user.getName(), user.getStudentNo(), user.getEmail(),
                user.getContact(), user.getCreatedAt());
    }
}
