package com.audiencerate.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(description = "Aggregated KPIs")
public record Overview(
        @Schema(description = "KPIs") Kpis kpis,
        @Schema(description = "Segments by status") Map<String, Integer> segmentsByStatus,
        @Schema(description = "Top segments") List<TopSegment> topSegments) {

    @Schema(description = "KPIs")
    public record Kpis(
            @Schema(description = "Total profiles") long totalProfiles,
            @Schema(description = "Total segments") int totalSegments,
            @Schema(description = "Active segments") int activeSegments,
            @Schema(description = "Avg match rate") BigDecimal avgMatchRate,
            @Schema(description = "Identities resolved") long identitiesResolved,
            @Schema(description = "Total activations") int totalActivations) {
    }

    @Schema(description = "Top segment")
    public record TopSegment(
            @Schema(description = "ID") String id,
            @Schema(description = "Name") String name,
            @Schema(description = "Audience size") long audienceSize) {
    }
}
