package com.must.secondhand.service;

import com.must.secondhand.common.BizException;
import com.must.secondhand.dto.ListingView;
import com.must.secondhand.model.entity.Favorite;
import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.model.enums.ListingStatus;
import com.must.secondhand.repository.FavoriteRepository;
import com.must.secondhand.repository.ListingRepository;
import com.must.secondhand.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Favorites list (REQ-6.1): add, remove, list; at most 200 per user. */
@Service
public class FavoriteService {

    private static final int MAX_FAVORITES = 200;

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ListingRepository listingRepository,
                           UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    /** Idempotent: saving the same listing twice is ignored. */
    @Transactional
    public void add(Long userId, Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> BizException.notFound("Listing not found"));
        if (listing.getStatus() == ListingStatus.SOLD) {
            throw new BizException("A sold listing cannot be saved");
        }
        if (favoriteRepository.existsByUserIdAndListingId(userId, listingId)) {
            return;
        }
        if (favoriteRepository.countByUserId(userId) >= MAX_FAVORITES) {
            throw new BizException("You can save at most " + MAX_FAVORITES + " listings");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.unauthorized("Session expired"));
        Favorite favorite = new Favorite(user, listing);
        favorite.setCreatedAt(Instant.now());
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void remove(Long userId, Long listingId) {
        favoriteRepository.findByUserIdAndListingId(userId, listingId)
                .ifPresent(favoriteRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<ListingView> list(Long userId) {
        List<ListingView> result = new ArrayList<>();
        for (Favorite favorite : favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            result.add(ListingView.of(favorite.getListing(),
                    favoriteRepository.countByListingId(favorite.getListing().getId())));
        }
        return result;
    }
}
