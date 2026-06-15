package com.audiencerate.validation;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SegmentValidator {

    private static final int MIN_TREND_RANGE_DAYS = 7;
    private static final int MAX_TREND_RANGE_DAYS = 180;
    private static final Set<String> VALID_STATUSES = Set.of("active", "draft", "archived");

    private final DataSourceDao dataSourceDao;

    public SegmentValidator(DataSourceDao dataSourceDao) {
        this.dataSourceDao = dataSourceDao;
    }

    public void validateCreate(CreateSegmentRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateName(req.name(), errors);
        validateStatus(req.status(), errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
        validateDataSourceIds(req.dataSourceIds());
    }

    public void validateUpdate(UpdateSegmentRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (req.name() != null) {
            validateName(req.name(), errors);
        }
        if (req.status() != null) {
            validateStatus(req.status(), errors);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
        validateDataSourceIds(req.dataSourceIds());
    }

    public void validateTrendRange(int rangeDays) {
        if (rangeDays < MIN_TREND_RANGE_DAYS || rangeDays > MAX_TREND_RANGE_DAYS) {
            throw new ValidationException("Validation failed",
                    Map.of("range", "Range must be between %d and %d days"
                            .formatted(MIN_TREND_RANGE_DAYS, MAX_TREND_RANGE_DAYS)));
        }
    }

    public void validateDataSourceIds(List<String> dataSourceIds) {
        if (dataSourceIds == null || dataSourceIds.isEmpty()) {
            return;
        }
        Set<String> existingIds = dataSourceDao.findExistingIds(dataSourceIds);
        for (String dsId : dataSourceIds) {
            if (!existingIds.contains(dsId)) {
                throw new ValidationException("Validation failed",
                        Map.of("dataSourceIds", "Data source not found: %s".formatted(dsId)));
            }
        }
    }

    private void validateName(String name, Map<String, String> errors) {
        if (name == null || name.isBlank()) {
            errors.put("name", "Name is required");
            return;
        }
        String trimmed = name.trim();
        if (trimmed.length() < 3) {
            errors.put("name", "Name must be at least 3 characters");
        }
        if (trimmed.length() > 80) {
            errors.put("name", "Name must be at most 80 characters");
        }
    }

    private void validateStatus(String status, Map<String, String> errors) {
        if (status != null && !VALID_STATUSES.contains(status)) {
            errors.put("status", "Status must be one of: active, draft, archived");
        }
    }
}
