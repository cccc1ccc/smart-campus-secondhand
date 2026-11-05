package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.PasswordChangeForm;
import com.must.secondhand.dto.ProfileForm;
import com.must.secondhand.dto.UserView;
import com.must.secondhand.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Profile maintenance and account deletion (REQ-1.2). */
@RestController
@RequestMapping("/api/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping
    public ApiResponse<UserView> updateProfile(@Valid @RequestBody ProfileForm form, HttpSession session) {
        return ApiResponse.ok("Profile updated", userService.updateProfile(SessionSupport.requireUserId(session), form));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeForm form, HttpSession session) {
        userService.changePassword(SessionSupport.requireUserId(session), form);
        return ApiResponse.ok("Password changed", null);
    }

    @DeleteMapping
    public ApiResponse<Void> deleteAccount(HttpSession session, HttpServletRequest request) {
        userService.deleteAccount(SessionSupport.requireUserId(session));
        HttpSession current = request.getSession(false);
        if (current != null) {
            current.invalidate();
        }
        return ApiResponse.ok("Account deleted", null);
    }
}
