package com.must.secondhand.config;

import com.must.secondhand.common.BizException;
import com.must.secondhand.common.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * Server-side authorisation for every mutating request (SEC-1.3).
 * Public: authentication endpoints, search, listing detail and metadata.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** GET endpoints that still require a session. */
    private static final List<String> PROTECTED_GET_PREFIXES = List.of(
            "/api/my/", "/api/matches", "/api/favorites", "/api/notifications");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            return true;
        }
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute(SessionKeys.USER_ID) != null;

        boolean publicGet = "GET".equals(request.getMethod())
                && !PROTECTED_GET_PREFIXES.stream().anyMatch(path::startsWith)
                && !path.startsWith("/api/auth/me");

        if (publicGet || loggedIn) {
            return true;
        }
        throw BizException.unauthorized("Please log in to continue");
    }
}
