package com.must.secondhand.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Page wrapper so the frontend does not depend on Spring's Page serialisation format. */
public record PageResult<T>(List<T> items, int page, int size, long total, int totalPages) {

    public static <E, T> PageResult<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    public static <T> PageResult<T> of(List<T> items) {
        return new PageResult<>(items, 0, items.size(), items.size(), 1);
    }
}
