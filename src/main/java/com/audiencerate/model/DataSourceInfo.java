package com.audiencerate.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Data source")
public record DataSourceInfo(
        @Schema(description = "ID") String id,
        @Schema(description = "Name") String name,
        @Schema(description = "Type") String type,
        @Schema(description = "Status") String status,
        @Schema(description = "Profiles count") long profilesCount,
        @Schema(description = "Match rate") BigDecimal matchRate,
        @Schema(description = "Last sync at") OffsetDateTime lastSyncAt) {
}
