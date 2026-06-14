package com.audiencerate.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Create activation request")
public record CreateActivationRequest(
        @JsonProperty("segmentId")
        @Schema(description = "Segment ID", required = true)
        String segmentId,

        @JsonProperty("destinationId")
        @Schema(description = "Destination ID", required = true)
        String destinationId) {
}
