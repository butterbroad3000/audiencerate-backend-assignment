package com.audiencerate.model.response;

public record PaginationMeta(
        int page,
        int pageSize,
        long total,
        int totalPages) {
}
