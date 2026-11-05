package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.ListingView;
import com.must.secondhand.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Favorites (REQ-6.1). */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<List<ListingView>> list(HttpSession session) {
        return ApiResponse.ok(favoriteService.list(SessionSupport.requireUserId(session)));
    }

    @PostMapping("/{listingId}")
    public ApiResponse<Void> add(@PathVariable Long listingId, HttpSession session) {
        favoriteService.add(SessionSupport.requireUserId(session), listingId);
        return ApiResponse.ok("Saved to favorites", null);
    }

    @DeleteMapping("/{listingId}")
    public ApiResponse<Void> remove(@PathVariable Long listingId, HttpSession session) {
        favoriteService.remove(SessionSupport.requireUserId(session), listingId);
        return ApiResponse.ok("Removed from favorites", null);
    }
}
