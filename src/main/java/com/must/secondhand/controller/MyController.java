package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.ListingView;
import com.must.secondhand.dto.RequestView;
import com.must.secondhand.service.BuyRequestService;
import com.must.secondhand.service.ListingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** "My listings" and "My requests" pages; always scoped to the logged-in owner. */
@RestController
@RequestMapping("/api/my")
public class MyController {

    private final ListingService listingService;
    private final BuyRequestService buyRequestService;

    public MyController(ListingService listingService, BuyRequestService buyRequestService) {
        this.listingService = listingService;
        this.buyRequestService = buyRequestService;
    }

    @GetMapping("/listings")
    public ApiResponse<List<ListingView>> myListings(HttpSession session) {
        return ApiResponse.ok(listingService.myListings(SessionSupport.requireUserId(session)));
    }

    @GetMapping("/requests")
    public ApiResponse<List<RequestView>> myRequests(HttpSession session) {
        return ApiResponse.ok(buyRequestService.myRequests(SessionSupport.requireUserId(session)));
    }
}
