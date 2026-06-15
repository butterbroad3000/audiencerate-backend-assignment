package com.audiencerate.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Audience segment")
public record Segment(
        @Schema(description = "Segment ID")
        String id,

        @Schema(description = "Name")
        String name,

        @Schema(description = "Description")
        String description,

        @Schema(description = "Status", allowableValues = {"active", "draft", "archived"})
        String status,

        @Schema(description = "Audience size")
        long audienceSize,

        @Schema(description = "Match rate")
        BigDecimal matchRate,

        @Schema(description = "Tags")
        List<String> tags,

        @Schema(description = "Data source IDs")
        List<String> dataSourceIds,

        @Schema(description = "Created by")
        String createdBy,

        @Schema(description = "Created at")
        OffsetDateTime createdAt,

        @Schema(description = "Updated at")
        OffsetDateTime updatedAt) {
}
