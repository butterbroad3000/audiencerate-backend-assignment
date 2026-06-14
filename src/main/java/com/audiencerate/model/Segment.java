package com.audiencerate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Audience segment")
public class Segment {
    @Schema(description = "Segment ID")
    private String id;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Status", allowableValues = {"active", "draft", "archived"})
    private String status;

    @JsonProperty("audienceSize")
    @Schema(description = "Audience size")
    private long audienceSize;

    @JsonProperty("matchRate")
    @Schema(description = "Match rate")
    private BigDecimal matchRate;

    @Schema(description = "Tags")
    private List<String> tags;

    @JsonProperty("dataSourceIds")
    @Schema(description = "Data source IDs")
    private List<String> dataSourceIds;

    @JsonProperty("createdBy")
    @Schema(description = "Created by")
    private String createdBy;

    @JsonProperty("createdAt")
    @Schema(description = "Created at")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    @Schema(description = "Updated at")
    private OffsetDateTime updatedAt;

    public Segment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getAudienceSize() { return audienceSize; }
    public void setAudienceSize(long audienceSize) { this.audienceSize = audienceSize; }
    public BigDecimal getMatchRate() { return matchRate; }
    public void setMatchRate(BigDecimal matchRate) { this.matchRate = matchRate; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getDataSourceIds() { return dataSourceIds; }
    public void setDataSourceIds(List<String> dataSourceIds) { this.dataSourceIds = dataSourceIds; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
