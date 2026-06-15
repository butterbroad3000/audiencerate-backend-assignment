package com.audiencerate.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Trend data point")
public record SegmentTrendPoint(
        @Schema(description = "Date") LocalDate date,
        @Schema(description = "Audience size") long audienceSize,
        @Schema(description = "Matched profiles") long matchedProfiles) {
}
