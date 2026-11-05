package com.must.secondhand.repository;

import com.must.secondhand.model.entity.BuyRequest;
import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BuyRequestRepository extends JpaRepository<BuyRequest, Long> {

    List<BuyRequest> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    Optional<BuyRequest> findByIdAndBuyerId(Long id, Long buyerId);

    long countByBuyerIdAndStatus(Long buyerId, RequestStatus status);

    List<BuyRequest> findByStatus(RequestStatus status);

    List<BuyRequest> findByStatusAndCategory(RequestStatus status, Category category);

    @Query("select r from BuyRequest r join fetch r.buyer where r.status = :status")
    List<BuyRequest> findActiveWithBuyer(@Param("status") RequestStatus status);

    /** Used by the scheduled matching retry: only recent requests are re-checked. */
    @Query("select r from BuyRequest r join fetch r.buyer where r.status = :status and r.createdAt >= :after")
    List<BuyRequest> findRecentActive(@Param("status") RequestStatus status, @Param("after") Instant after);
}
