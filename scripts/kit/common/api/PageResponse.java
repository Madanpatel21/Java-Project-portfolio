package com.java700.kit.common.api;

import java.util.List;
import org.springframework.data.domain.Page;

/** Uniform pagination envelope. */
public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {

    public PageResponse {
        items = List.copyOf(items);
    }

    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}
