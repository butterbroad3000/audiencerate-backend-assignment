package com.audiencerate.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Create activation request")
public record CreateActivationRequest(
        @Schema(description = "Segment ID", required = true)
        String segmentId,

        @Schema(description = "Destination ID", required = true)
        String destinationId) {
}
