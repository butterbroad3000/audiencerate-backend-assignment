package com.audiencerate.validation;

import com.audiencerate.error.ValidationException;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SegmentValidator {

    private static final Set<String> VALID_STATUSES = Set.of("active", "draft", "archived");

    public void validateCreate(CreateSegmentRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateName(req.name(), errors);
        validateStatus(req.status(), errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
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
