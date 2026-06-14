package com.audiencerate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Data source")
public record DataSource(
        @Schema(description = "ID") String id,
        @Schema(description = "Name") String name,
        @Schema(description = "Type") String type,
        @Schema(description = "Status") String status,
        @JsonProperty("profilesCount") @Schema(description = "Profiles count") long profilesCount,
        @JsonProperty("matchRate") @Schema(description = "Match rate") BigDecimal matchRate,
        @JsonProperty("lastSyncAt") @Schema(description = "Last sync at") OffsetDateTime lastSyncAt) {
}
