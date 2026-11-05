package com.must.secondhand.common;

import jakarta.servlet.http.HttpSession;

/** Small helper to read the logged-in user from the HTTP session. */
public final class SessionSupport {

    private SessionSupport() {
    }

    public static Long userId(HttpSession session) {
        Object value = session.getAttribute(SessionKeys.USER_ID);
        return value == null ? null : (Long) value;
    }

    public static Long requireUserId(HttpSession session) {
        Long userId = userId(session);
        if (userId == null) {
            throw BizException.unauthorized("Please log in to continue");
        }
        return userId;
    }
}
