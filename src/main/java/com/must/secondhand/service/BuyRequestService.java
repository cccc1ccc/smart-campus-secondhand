package com.must.secondhand.service;

import com.must.secondhand.common.BizException;
import com.must.secondhand.dto.BuyRequestForm;
import com.must.secondhand.dto.RequestView;
import com.must.secondhand.model.entity.BuyRequest;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.model.enums.RequestStatus;
import com.must.secondhand.repository.BuyRequestRepository;
import com.must.secondhand.repository.MatchRecordRepository;
import com.must.secondhand.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Want-to-buy requests (REQ-4.1): publish, edit, close. */
@Service
public class BuyRequestService {

    private static final Logger log = LoggerFactory.getLogger(BuyRequestService.class);
    private static final int MAX_ACTIVE_REQUESTS = 20;

    private final BuyRequestRepository buyRequestRepository;
    private final UserRepository userRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final MatchingService matchingService;

    public BuyRequestService(BuyRequestRepository buyRequestRepository,
                             UserRepository userRepository,
                             MatchRecordRepository matchRecordRepository,
                             MatchingService matchingService) {
        this.buyRequestRepository = buyRequestRepository;
        this.userRepository = userRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.matchingService = matchingService;
    }

    @Transactional
    public RequestView create(Long userId, BuyRequestForm form) {
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> BizException.unauthorized("Session expired"));
        validateBudget(form);
        if (buyRequestRepository.countByBuyerIdAndStatus(userId, RequestStatus.ACTIVE) >= MAX_ACTIVE_REQUESTS) {
            throw new BizException("You already have " + MAX_ACTIVE_REQUESTS
                    + " active requests. Close one before publishing another.");
        }
        BuyRequest request = new BuyRequest();
        request.setBuyer(buyer);
        applyForm(request, form);
        request.setStatus(RequestStatus.ACTIVE);
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());
        request = buyRequestRepository.save(request);

        triggerMatching(request);
        return RequestView.of(request);
    }

    @Transactional
    public RequestView update(Long userId, Long requestId, BuyRequestForm form) {
        BuyRequest request = requireOwned(userId, requestId);
        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new BizException("A closed request cannot be edited");
        }
        validateBudget(form);
        applyForm(request, form);
        request.setUpdatedAt(Instant.now());
        request = buyRequestRepository.save(request);
        triggerMatching(request);
        return RequestView.of(request);
    }

    @Transactional
    public RequestView close(Long userId, Long requestId) {
        BuyRequest request = requireOwned(userId, requestId);
        if (request.getStatus() == RequestStatus.CLOSED) {
            throw new BizException("This request is already closed");
        }
        request.setStatus(RequestStatus.CLOSED);
        request.setUpdatedAt(Instant.now());
        request = buyRequestRepository.save(request);
        matchingService.closeMatchesOfRequest(requestId);
        return RequestView.of(request);
    }

    @Transactional(readOnly = true)
    public List<RequestView> myRequests(Long userId) {
        return buyRequestRepository.findByBuyerIdOrderByCreatedAtDesc(userId).stream()
                .map(RequestView::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequestView detail(Long requestId) {
        return RequestView.of(buyRequestRepository.findById(requestId)
                .orElseThrow(() -> BizException.notFound("Request not found")));
    }

    private BuyRequest requireOwned(Long userId, Long requestId) {
        BuyRequest request = buyRequestRepository.findById(requestId)
                .orElseThrow(() -> BizException.notFound("Request not found"));
        if (!request.getBuyer().getId().equals(userId)) {
            throw BizException.forbidden("You can only manage your own requests");
        }
        return request;
    }

    private void validateBudget(BuyRequestForm form) {
        if (form.minBudget().compareTo(form.maxBudget()) > 0) {
            throw new BizException("Minimum budget cannot be greater than maximum budget");
        }
    }

    private void applyForm(BuyRequest request, BuyRequestForm form) {
        request.setKeywords(form.keywords().trim());
        request.setCategory(form.category());
        request.setMinBudget(form.minBudget());
        request.setMaxBudget(form.maxBudget());
        request.setAcceptableCondition(form.acceptableCondition());
        request.setRequirements(form.requirements() == null || form.requirements().isBlank()
                ? null : form.requirements().trim());
    }

    private void triggerMatching(BuyRequest request) {
        try {
            matchingService.runForRequest(request);
        } catch (RuntimeException ex) {
            log.error("Matching after request {} failed, the scheduled run will retry", request.getId(), ex);
        }
    }
}
