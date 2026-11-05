package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.BuyRequestForm;
import com.must.secondhand.dto.RequestView;
import com.must.secondhand.service.BuyRequestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Want-to-buy requests (REQ-4.1). */
@RestController
@RequestMapping("/api/requests")
public class BuyRequestController {

    private final BuyRequestService buyRequestService;

    public BuyRequestController(BuyRequestService buyRequestService) {
        this.buyRequestService = buyRequestService;
    }

    @GetMapping("/{id}")
    public ApiResponse<RequestView> detail(@PathVariable Long id) {
        return ApiResponse.ok(buyRequestService.detail(id));
    }

    @PostMapping
    public ApiResponse<RequestView> create(@Valid @RequestBody BuyRequestForm form, HttpSession session) {
        return ApiResponse.ok("Request published",
                buyRequestService.create(SessionSupport.requireUserId(session), form));
    }

    @PutMapping("/{id}")
    public ApiResponse<RequestView> update(@PathVariable Long id, @Valid @RequestBody BuyRequestForm form,
                                           HttpSession session) {
        return ApiResponse.ok("Request updated",
                buyRequestService.update(SessionSupport.requireUserId(session), id, form));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<RequestView> close(@PathVariable Long id, HttpSession session) {
        return ApiResponse.ok("Request closed",
                buyRequestService.close(SessionSupport.requireUserId(session), id));
    }
}
