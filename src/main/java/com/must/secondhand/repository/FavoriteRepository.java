package com.must.secondhand.repository;

import com.must.secondhand.model.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Favorite> findByUserIdAndListingId(Long userId, Long listingId);

    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    long countByUserId(Long userId);

    long countByListingId(Long listingId);

    /** Users who favorited a listing, used to notify them about status/price changes. */
    @Query("select f.user.id from Favorite f where f.listing.id = :listingId")
    List<Long> findUserIdsByListingId(@Param("listingId") Long listingId);

    @Modifying
    @Query("delete from Favorite f where f.listing.id = :listingId")
    int deleteByListingId(@Param("listingId") Long listingId);

    @Modifying
    @Query("delete from Favorite f where f.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
