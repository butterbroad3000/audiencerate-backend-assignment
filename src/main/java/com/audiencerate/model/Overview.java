package com.audiencerate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(description = "Aggregated KPIs")
public record Overview(
        @Schema(description = "KPIs") Kpis kpis,
        @JsonProperty("segmentsByStatus") @Schema(description = "Segments by status") Map<String, Integer> segmentsByStatus,
        @JsonProperty("topSegments") @Schema(description = "Top segments") List<TopSegment> topSegments) {

    @Schema(description = "KPIs")
    public record Kpis(
            @JsonProperty("totalProfiles") @Schema(description = "Total profiles") long totalProfiles,
            @JsonProperty("totalSegments") @Schema(description = "Total segments") int totalSegments,
            @JsonProperty("activeSegments") @Schema(description = "Active segments") int activeSegments,
            @JsonProperty("avgMatchRate") @Schema(description = "Avg match rate") BigDecimal avgMatchRate,
            @JsonProperty("identitiesResolved") @Schema(description = "Identities resolved") long identitiesResolved,
            @JsonProperty("totalActivations") @Schema(description = "Total activations") int totalActivations) {
    }

    @Schema(description = "Top segment")
    public record TopSegment(
            @Schema(description = "ID") String id,
            @Schema(description = "Name") String name,
            @JsonProperty("audienceSize") @Schema(description = "Audience size") long audienceSize) {
    }
}
