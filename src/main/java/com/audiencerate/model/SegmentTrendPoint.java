package com.audiencerate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Trend data point")
public record SegmentTrendPoint(
        @Schema(description = "Date") LocalDate date,
        @JsonProperty("audienceSize") @Schema(description = "Audience size") long audienceSize,
        @JsonProperty("matchedProfiles") @Schema(description = "Matched profiles") long matchedProfiles) {
}
