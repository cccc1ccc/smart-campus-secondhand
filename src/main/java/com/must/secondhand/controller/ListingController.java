package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.common.SessionSupport;
import com.must.secondhand.dto.ListingDetailView;
import com.must.secondhand.dto.ListingForm;
import com.must.secondhand.dto.ListingView;
import com.must.secondhand.dto.StatusForm;
import com.must.secondhand.service.ListingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Listing publishing, editing, deletion and status transitions (REQ-2.1, REQ-2.2, REQ-7.1). */
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/{id}")
    public ApiResponse<ListingDetailView> detail(@PathVariable Long id, HttpSession session) {
        return ApiResponse.ok(listingService.detail(id, SessionSupport.userId(session)));
    }

    @PostMapping
    public ApiResponse<ListingView> create(@Valid @RequestBody ListingForm form, HttpSession session) {
        return ApiResponse.ok("Listing published",
                listingService.create(SessionSupport.requireUserId(session), form));
    }

    @PutMapping("/{id}")
    public ApiResponse<ListingView> update(@PathVariable Long id, @Valid @RequestBody ListingForm form,
                                           HttpSession session) {
        return ApiResponse.ok("Listing updated",
                listingService.update(SessionSupport.requireUserId(session), id, form));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<ListingView> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusForm form,
                                                 HttpSession session) {
        return ApiResponse.ok("Status updated",
                listingService.changeStatus(SessionSupport.requireUserId(session), id, form.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        listingService.delete(SessionSupport.requireUserId(session), id);
        return ApiResponse.ok("Listing deleted", null);
    }
}
