package com.audiencerate.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaginationMeta(
        int page,
        int pageSize,
        long total,
        int totalPages) {
}
