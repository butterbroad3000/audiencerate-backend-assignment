package com.audiencerate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Segment activation")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Activation {
    @Schema(description = "Activation ID")
    private String id;

    @Schema(description = "Segment ID")
    private String segmentId;

    @Schema(description = "Destination ID")
    private String destinationId;

    @Schema(description = "Status", allowableValues = {"live", "syncing", "paused", "error"})
    private String status;

    @Schema(description = "Synced profiles")
    private long syncedProfiles;

    @Schema(description = "Created at")
    private OffsetDateTime createdAt;

    @Schema(description = "Last sync at")
    private OffsetDateTime lastSyncAt;

    @Schema(description = "Destination")
    private Destination destination;

    public Activation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }
    public String getDestinationId() { return destinationId; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getSyncedProfiles() { return syncedProfiles; }
    public void setSyncedProfiles(long syncedProfiles) { this.syncedProfiles = syncedProfiles; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(OffsetDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }
}
