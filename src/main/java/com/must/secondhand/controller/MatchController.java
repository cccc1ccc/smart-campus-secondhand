package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.MatchView;
import com.must.secondhand.service.MatchingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Matches page: every open matched pair of the current user, sorted by score (REQ-5.1). */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping
    public ApiResponse<List<MatchView>> matches(HttpSession session) {
        return ApiResponse.ok(matchingService.matchesForUser(SessionSupport.requireUserId(session)));
    }
}
