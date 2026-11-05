package com.must.secondhand.repository;

import com.must.secondhand.model.entity.ListingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingStatusHistoryRepository extends JpaRepository<ListingStatusHistory, Long> {

    List<ListingStatusHistory> findByListingIdOrderByChangedAtAsc(Long listingId);

    void deleteByListingId(Long listingId);
}
