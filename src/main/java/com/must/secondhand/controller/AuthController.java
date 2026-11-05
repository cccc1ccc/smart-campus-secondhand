package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.SessionKeys;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.LoginForm;
import com.must.secondhand.dto.RegisterForm;
import com.must.secondhand.dto.UserView;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.service.AuthService;
import com.must.secondhand.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registration, login, logout and session state (REQ-1.1 / REQ-1.2). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<UserView> register(@Valid @RequestBody RegisterForm form, HttpServletRequest request) {
        User user = authService.register(form);
        startSession(request, user);
        return ApiResponse.ok("Welcome to the campus marketplace", UserView.of(user));
    }

    @PostMapping("/login")
    public ApiResponse<UserView> login(@Valid @RequestBody LoginForm form, HttpServletRequest request) {
        User user = authService.login(form);
        startSession(request, user);
        return ApiResponse.ok("Logged in", UserView.of(user));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.ok("Logged out", null);
    }

    /** Returns the current user, or null data when nobody is logged in. */
    @GetMapping("/me")
    public ApiResponse<UserView> me(HttpSession session) {
        Long userId = SessionSupport.userId(session);
        return ApiResponse.ok(userId == null ? null : userService.profile(userId));
    }

    /** Prevents session fixation: a fresh session is created on every login. */
    private void startSession(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.USER_ID, user.getId());
        session.setAttribute(SessionKeys.USER_NAME, user.getName());
    }
}
