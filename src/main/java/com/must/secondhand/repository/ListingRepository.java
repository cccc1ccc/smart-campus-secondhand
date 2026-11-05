package com.must.secondhand.repository;

import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    List<Listing> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    Optional<Listing> findByIdAndSellerId(Long id, Long sellerId);

    long countBySellerIdAndStatusIn(Long sellerId, List<ListingStatus> statuses);

    long countByStatus(ListingStatus status);

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByStatusAndCategory(ListingStatus status, Category category);

    /** Light projection used by the matching engine to avoid N+1 lazy loading. */
    @Query("select l from Listing l join fetch l.seller where l.status = :status")
    List<Listing> findActiveWithSeller(@Param("status") ListingStatus status);

    /** Used by the scheduled matching retry: only recent items are re-checked. */
    @Query("select l from Listing l join fetch l.seller where l.status = :status and l.createdAt >= :after")
    List<Listing> findRecentActive(@Param("status") ListingStatus status, @Param("after") Instant after);

    @Query("select l from Listing l join fetch l.seller where l.id = :id")
    Optional<Listing> findWithSellerById(@Param("id") Long id);
}
