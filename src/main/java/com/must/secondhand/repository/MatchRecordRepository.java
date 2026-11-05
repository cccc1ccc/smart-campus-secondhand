package com.must.secondhand.repository;

import com.must.secondhand.model.entity.MatchRecord;
import com.must.secondhand.model.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long> {

    Optional<MatchRecord> findByListingIdAndRequestId(Long listingId, Long requestId);

    List<MatchRecord> findByListingId(Long listingId);

    List<MatchRecord> findByRequestId(Long requestId);

    List<MatchRecord> findByListingIdAndStatus(Long listingId, MatchStatus status);

    List<MatchRecord> findByRequestIdAndStatus(Long requestId, MatchStatus status);

    /** All open matches that involve the user, either as seller or as buyer (REQ-5.1). */
    @Query("""
            select m from MatchRecord m
            join fetch m.listing l
            join fetch m.request r
            where m.status = com.must.secondhand.model.enums.MatchStatus.OPEN
              and (l.seller.id = :userId or r.buyer.id = :userId)
            order by m.score desc, m.createdAt desc
            """)
    List<MatchRecord> findOpenMatchesForUser(@Param("userId") Long userId);

    @Query("select m from MatchRecord m where m.status = :status order by m.score desc")
    List<MatchRecord> findByStatus(@Param("status") MatchStatus status);

    @Modifying
    @Query("delete from MatchRecord m where m.listing.id = :listingId")
    int deleteByListingId(@Param("listingId") Long listingId);

    @Modifying
    @Query("delete from MatchRecord m where m.request.id = :requestId")
    int deleteByRequestId(@Param("requestId") Long requestId);

    @Modifying
    @Query("delete from MatchRecord m where m.listing.seller.id = :userId or m.request.buyer.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
