package com.must.secondhand.controller;

import com.must.secondhand.common.ApiResponse;
import com.must.secondhand.model.enums.Category;
import com.must.secondhand.model.enums.ItemCondition;
import com.must.secondhand.service.MatchingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Fixed option lists and the published matching rule, used to build the forms. */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private record Option(String value, String label) {
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> meta() {
        List<Option> categories = Arrays.stream(Category.values())
                .map(category -> new Option(category.name(), category.getLabel()))
                .toList();
        List<Option> conditions = Arrays.stream(ItemCondition.values())
                .map(condition -> new Option(condition.name(), condition.getLabel()))
                .toList();
        return ApiResponse.ok(Map.of(
                "categories", categories,
                "conditions", conditions,
                "sorts", List.of(
                        new Option("newest", "Newest first"),
                        new Option("price_asc", "Price: low to high"),
                        new Option("price_desc", "Price: high to low")),
                "matching", Map.of(
                        "weights", Map.of(
                                "category", MatchingService.WEIGHT_CATEGORY,
                                "price", MatchingService.WEIGHT_PRICE,
                                "condition", MatchingService.WEIGHT_CONDITION),
                        "keywordThreshold", MatchingService.KEYWORD_THRESHOLD,
                        "scoreThreshold", MatchingService.SCORE_THRESHOLD)));
    }
}
