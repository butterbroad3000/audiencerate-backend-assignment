package com.audiencerate.validation;

import com.audiencerate.dao.DestinationDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.Destination;
import com.audiencerate.model.Segment;
import com.audiencerate.model.request.CreateActivationRequest;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationValidatorTest {

    private static final String NON_EXISTENT_ID = "seg_0999";

    @Mock SegmentDao segmentDao;
    @Mock DestinationDao destinationDao;

    private ActivationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ActivationValidator(segmentDao, destinationDao);
    }

    // ── valid request ──

    @Test
    void shouldPassWithValidRequest() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(Instancio.create(Segment.class)));
        when(destinationDao.findById("dest_001")).thenReturn(Optional.of(Instancio.create(Destination.class)));

        var req = new CreateActivationRequest("seg_0001", "dest_001");
        assertDoesNotThrow(() -> validator.validateCreate(req),
                "valid request should pass without exception");
        verify(segmentDao).findById("seg_0001");
        verify(destinationDao).findById("dest_001");
        verifyNoMoreInteractions(segmentDao, destinationDao);
    }

    // ── segmentId ──

    @Test
    void shouldFailWhenSegmentIdIsNull() {
        var req = new CreateActivationRequest(null, "dest_001");
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "null segmentId must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("segmentId"),
                "error details must contain 'segmentId' field");
        verifyNoInteractions(segmentDao, destinationDao);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldFailWhenSegmentIdIsBlank(String segmentId) {
        var req = new CreateActivationRequest(segmentId, "dest_001");
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "blank segmentId must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("segmentId"),
                "error details must contain 'segmentId' field");
        verifyNoInteractions(segmentDao, destinationDao);
    }

    // ── destinationId ──

    @Test
    void shouldFailWhenDestinationIdIsNull() {
        var req = new CreateActivationRequest("seg_0001", null);
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "null destinationId must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("destinationId"),
                "error details must contain 'destinationId' field");
        verifyNoInteractions(segmentDao, destinationDao);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldFailWhenDestinationIdIsBlank(String destinationId) {
        var req = new CreateActivationRequest("seg_0001", destinationId);
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "blank destinationId must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("destinationId"),
                "error details must contain 'destinationId' field");
        verifyNoInteractions(segmentDao, destinationDao);
    }

    // ── both blank ──

    @Test
    void shouldCollectMultipleErrors() {
        var req = new CreateActivationRequest(null, null);
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "both fields null must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("segmentId"),
                "error details must contain 'segmentId' field");
        assertTrue(ex.getDetails().containsKey("destinationId"),
                "error details must contain 'destinationId' field");
        verifyNoInteractions(segmentDao, destinationDao);
    }

    // ── segment not found ──

    @Test
    void shouldFailWhenSegmentNotFound() {
        when(segmentDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        var req = new CreateActivationRequest(NON_EXISTENT_ID, "dest_001");
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "non-existing segment must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("segmentId"),
                "error details must contain 'segmentId' field");
        assertTrue(ex.getDetails().get("segmentId").contains(NON_EXISTENT_ID),
                "error message must include the segment ID");
        verify(segmentDao).findById(NON_EXISTENT_ID);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(destinationDao);
    }

    // ── destination not found ──

    @Test
    void shouldFailWhenDestinationNotFound() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(Instancio.create(Segment.class)));
        when(destinationDao.findById("dest_0999")).thenReturn(Optional.empty());

        var req = new CreateActivationRequest("seg_0001", "dest_0999");
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "non-existing destination must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("destinationId"),
                "error details must contain 'destinationId' field");
        assertTrue(ex.getDetails().get("destinationId").contains("dest_0999"),
                "error message must include the destination ID");
        verify(segmentDao).findById("seg_0001");
        verify(destinationDao).findById("dest_0999");
        verifyNoMoreInteractions(segmentDao, destinationDao);
    }
}
