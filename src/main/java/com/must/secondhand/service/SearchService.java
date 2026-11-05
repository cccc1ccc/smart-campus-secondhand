package com.must.secondhand.service;

import com.must.secondhand.common.BizException;
import com.must.secondhand.common.PageResult;
import com.must.secondhand.dto.ListingView;
import com.must.secondhand.model.entity.Listing;
import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.ItemCondition;
import com.must.secondhand.model.enums.ListingStatus;
import com.must.secondhand.repository.ListingRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Search, filter and sort of available listings (REQ-3.1). */
@Service
public class SearchService {

    public static final int PAGE_SIZE = 20;

    private final ListingRepository listingRepository;

    public SearchService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<ListingView> search(String keyword, Category category, BigDecimal minPrice,
                                          BigDecimal maxPrice, List<ItemCondition> conditions,
                                          String sort, int page, int size) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BizException("Minimum price cannot be greater than maximum price");
        }
        int pageNumber = Math.max(page, 0);
        int pageSize = size <= 0 ? PAGE_SIZE : Math.min(size, 60);

        Specification<Listing> specification = buildSpecification(keyword, category, minPrice, maxPrice, conditions);
        Page<Listing> result = listingRepository.findAll(specification,
                PageRequest.of(pageNumber, pageSize, resolveSort(sort)));
        return PageResult.of(result, ListingView::of);
    }

    private Specification<Listing> buildSpecification(String keyword, Category category, BigDecimal minPrice,
                                                      BigDecimal maxPrice, List<ItemCondition> conditions) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Search results never include sold or reserved listings.
            predicates.add(cb.equal(root.get("status"), ListingStatus.AVAILABLE));

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (conditions != null && !conditions.isEmpty()) {
                predicates.add(root.get("condition").in(conditions));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
