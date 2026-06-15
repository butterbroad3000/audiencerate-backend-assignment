package com.audiencerate.validation;

import com.audiencerate.dao.DestinationDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.request.CreateActivationRequest;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActivationValidator {

    private final SegmentDao segmentDao;
    private final DestinationDao destinationDao;

    public ActivationValidator(SegmentDao segmentDao, DestinationDao destinationDao) {
        this.segmentDao = segmentDao;
        this.destinationDao = destinationDao;
    }

    public void validateCreate(CreateActivationRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (req.segmentId() == null || req.segmentId().isBlank()) {
            errors.put("segmentId", "Segment ID is required");
        }
        if (req.destinationId() == null || req.destinationId().isBlank()) {
            errors.put("destinationId", "Destination ID is required");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        // Existence checks
        if (segmentDao.findById(req.segmentId()).isEmpty()) {
            throw new ValidationException("Validation failed",
                    Map.of("segmentId", "Segment not found: %s".formatted(req.segmentId())));
        }
        if (!req.destinationId().isBlank()
                && destinationDao.findById(req.destinationId()).isEmpty()) {
            throw new ValidationException("Validation failed",
                    Map.of("destinationId", "Destination not found: %s".formatted(req.destinationId())));
        }
    }
}
