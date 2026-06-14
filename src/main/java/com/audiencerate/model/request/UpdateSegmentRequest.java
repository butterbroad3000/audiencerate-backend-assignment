package com.audiencerate.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Update segment request")
public record UpdateSegmentRequest(
        @Schema(description = "Name")
        String name,

        @Schema(description = "Description")
        String description,

        @Schema(description = "Status", allowableValues = {"active", "draft", "archived"})
        String status,

        @JsonProperty("dataSourceIds")
        @Schema(description = "Data source IDs")
        List<String> dataSourceIds,

        @Schema(description = "Tags")
        List<String> tags) {
}
