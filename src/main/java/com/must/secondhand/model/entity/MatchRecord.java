package com.must.secondhand.model.entity;

import com.must.secondhand.model.enums.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * One automatic match between a listing and a want-to-buy request (REQ-5.1).
 * Every qualifying pair is stored separately, so a listing can match several buyers.
 */
@Entity
@Table(name = "match_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_match_pair", columnNames = {"listing_id", "request_id"}),
        indexes = {
                @Index(name = "idx_match_listing", columnList = "listing_id"),
                @Index(name = "idx_match_request", columnList = "request_id"),
                @Index(name = "idx_match_score", columnList = "score")
        })
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private BuyRequest request;

    @Column(nullable = false)
    private double score;

    @Column(name = "category_score", nullable = false)
    private double categoryScore;

    @Column(name = "price_score", nullable = false)
    private double priceScore;

    @Column(name = "condition_score", nullable = false)
    private double conditionScore;

    @Column(name = "keyword_score", nullable = false)
    private double keywordScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public BuyRequest getRequest() {
        return request;
    }

    public void setRequest(BuyRequest request) {
        this.request = request;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getCategoryScore() {
        return categoryScore;
    }

    public void setCategoryScore(double categoryScore) {
        this.categoryScore = categoryScore;
    }

    public double getPriceScore() {
        return priceScore;
    }

    public void setPriceScore(double priceScore) {
        this.priceScore = priceScore;
    }

    public double getConditionScore() {
        return conditionScore;
    }

    public void setConditionScore(double conditionScore) {
        this.conditionScore = conditionScore;
    }

    public double getKeywordScore() {
        return keywordScore;
    }

    public void setKeywordScore(double keywordScore) {
        this.keywordScore = keywordScore;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
