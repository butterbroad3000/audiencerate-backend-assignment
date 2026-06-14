package com.audiencerate.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.LinkedHashMap;
import java.util.Map;

@Schema(description = "Health check")
public record HealthStatus(
        @Schema(description = "Status") String status,
        @Schema(description = "Databases") Map<String, String> databases) {

    public static Builder check() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, String> dbs = new LinkedHashMap<>();
        private String status = "ok";

        public Builder up(String name) {
            dbs.put(name, "up");
            return this;
        }

        public Builder down(String name) {
            dbs.put(name, "down");
            status = "error";
            return this;
        }

        public HealthStatus build() {
            return new HealthStatus(status, Map.copyOf(dbs));
        }
    }
}
