package com.audiencerate.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Destination")
public record Destination(
        @Schema(description = "ID") String id,
        @Schema(description = "Name") String name,
        @Schema(description = "Color") String color) {
}
