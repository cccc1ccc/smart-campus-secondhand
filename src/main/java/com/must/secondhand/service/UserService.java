package com.must.secondhand.service;

import com.must.secondhand.common.BizException;
import com.must.secondhand.dto.PasswordChangeForm;
import com.must.secondhand.dto.ProfileForm;
import com.must.secondhand.dto.UserView;
import com.must.secondhand.model.entity.BuyRequest;
import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.repository.BuyRequestRepository;
import com.must.secondhand.repository.FavoriteRepository;
import com.must.secondhand.repository.ListingRepository;
import com.must.secondhand.repository.ListingStatusHistoryRepository;
import com.must.secondhand.repository.MatchRecordRepository;
import com.must.secondhand.repository.NotificationRepository;
import com.must.secondhand.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Profile maintenance and account deletion (REQ-1.2). */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BuyRequestRepository buyRequestRepository;
    private final FavoriteRepository favoriteRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final NotificationRepository notificationRepository;
    private final ListingStatusHistoryRepository statusHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ListingRepository listingRepository,
                       BuyRequestRepository buyRequestRepository,
                       FavoriteRepository favoriteRepository,
                       MatchRecordRepository matchRecordRepository,
                       NotificationRepository notificationRepository,
                       ListingStatusHistoryRepository statusHistoryRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.buyRequestRepository = buyRequestRepository;
        this.favoriteRepository = favoriteRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.notificationRepository = notificationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User requireUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> BizException.unauthorized("Session expired"));
    }

    @Transactional(readOnly = true)
    public UserView profile(Long userId) {
        return UserView.of(requireUser(userId));
    }

    @Transactional
    public UserView updateProfile(Long userId, ProfileForm form) {
        User user = requireUser(userId);
        user.setName(form.name().trim());
        user.setContact(form.contact() == null ? null : form.contact().trim());
        return UserView.of(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeForm form) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(form.oldPassword(), user.getPasswordHash())) {
            throw new BizException("Current password is incorrect");
        }
        if (passwordEncoder.matches(form.newPassword(), user.getPasswordHash())) {
            throw new BizException("The new password must be different from the current one");
        }
        user.setPasswordHash(passwordEncoder.encode(form.newPassword()));
        userRepository.save(user);
    }

    /**
     * Deletes the profile together with listings, requests, favorites, match records and
     * notifications in one transaction (REQ-1.2 data handling).
     */
    @Transactional
    public void deleteAccount(Long userId) {
        List<Listing> listings = listingRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        for (Listing listing : listings) {
            favoriteRepository.deleteByListingId(listing.getId());
            matchRecordRepository.deleteByListingId(listing.getId());
            statusHistoryRepository.deleteByListingId(listing.getId());
            listingRepository.delete(listing);
        }
        for (BuyRequest request : buyRequestRepository.findByBuyerIdOrderByCreatedAtDesc(userId)) {
            matchRecordRepository.deleteByRequestId(request.getId());
            buyRequestRepository.delete(request);
        }
        favoriteRepository.deleteByUserId(userId);
        matchRecordRepository.deleteByUserId(userId);
        notificationRepository.deleteByUserId(userId);
        userRepository.delete(requireUser(userId));
    }
}
