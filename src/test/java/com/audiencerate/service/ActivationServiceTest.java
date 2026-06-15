package com.audiencerate.service;

import com.audiencerate.dao.ActivationDao;
import com.audiencerate.dao.ActivationDao.ActivationListResult;
import com.audiencerate.dao.DestinationDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.error.NotFoundException;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.Activation;
import com.audiencerate.model.Destination;
import com.audiencerate.model.Segment;
import com.audiencerate.model.request.CreateActivationRequest;
import com.audiencerate.validation.ActivationValidator;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationServiceTest {

    private static final String NON_EXISTENT_ID = "seg_0999";

    @Mock ActivationDao activationDao;
    @Mock DestinationDao destinationDao;
    @Mock SegmentDao segmentDao;
    @Mock DataSource activationsDs;
    @Mock Connection connection;
    @Mock ActivationValidator validator;

    private ActivationService service;

    @BeforeEach
    void setUp() {
        service = new ActivationService(activationDao, destinationDao, segmentDao, validator, activationsDs);
    }

    // ── list ──

    @Test
    void shouldListActivations() {
        String expectedName = "Google Ads";
        var dests = List.of(
                new Destination("dest_001", expectedName, "#34A853"),
                new Destination("dest_002", "Meta", "#1877F2"));
        when(destinationDao.findAll()).thenReturn(dests);

        var acts = List.of(
                Instancio.of(Activation.class).set(Select.field(Activation::getDestinationId), "dest_001").create(),
                Instancio.of(Activation.class).set(Select.field(Activation::getDestinationId), "dest_002").create());
        when(activationDao.list(null, null, 1, 12)).thenReturn(new ActivationListResult(acts, 2));

        var result = service.list(null, null, 1, 12);

        assertEquals(2, result.data().size(), "two activations in result");
        assertNotNull(result.data().get(0).getDestination(), "first activation must have destination enriched");
        assertEquals(expectedName, result.data().get(0).getDestination().name(), "first destination name must match");

        verify(destinationDao).findAll();
        verify(activationDao).list(null, null, 1, 12);
        verifyNoMoreInteractions(activationDao, destinationDao);
        verifyNoInteractions(segmentDao, validator, activationsDs, connection);
    }

    // ── getActivationsForSegment ──

    @Test
    void shouldReturnActivationsForSegment() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(Instancio.create(Segment.class)));
        when(destinationDao.findAll()).thenReturn(List.of());
        when(activationDao.list(eq("seg_0001"), isNull(), eq(1), eq(12)))
                .thenReturn(new ActivationListResult(List.of(), 0));

        var result = service.getActivationsForSegment("seg_0001", 1, 12);

        assertTrue(result.data().isEmpty(), "no activations when list is empty");
        verify(segmentDao).findById("seg_0001");
        verify(activationDao).list("seg_0001", null, 1, 12);
        verifyNoMoreInteractions(segmentDao, activationDao);
    }

    @Test
    void shouldThrowNotFoundWhenSegmentMissingForActivations() {
        when(segmentDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getActivationsForSegment(NON_EXISTENT_ID, 1, 12),
                "missing segment must throw NotFoundException");
        verify(segmentDao).findById(NON_EXISTENT_ID);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(activationDao, destinationDao);
    }

    // ── create ──

    @Test
    void shouldCreateActivation() throws SQLException {
        when(destinationDao.findById("dest_001"))
                .thenReturn(Optional.of(Instancio.of(Destination.class)
                        .set(Select.field(Destination::id), "dest_001")
                        .create()));
        when(activationsDs.getConnection()).thenReturn(connection);
        when(activationDao.create(any(), eq("seg_0001"), eq("dest_001")))
                .thenReturn(Instancio.of(Activation.class)
                        .set(Select.field(Activation::getDestinationId), "dest_001")
                        .create());

        var req = new CreateActivationRequest("seg_0001", "dest_001");
        var result = service.create(req);

        assertNotNull(result, "created activation must not be null");
        assertNotNull(result.getDestination(), "activation must be enriched with destination");
        verify(validator).validateCreate(req);
        verify(destinationDao).findById("dest_001");
        verify(activationsDs).getConnection();
        verify(activationDao).create(any(), eq("seg_0001"), eq("dest_001"));
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).close();
        verifyNoMoreInteractions(validator, destinationDao, activationDao, activationsDs, connection);
        verifyNoInteractions(segmentDao);
    }

    @Test
    void shouldFailCreateWhenSegmentIdMissing() {
        doThrow(new ValidationException("Validation failed",
                Map.of("segmentId", "Segment ID is required")))
                .when(validator).validateCreate(any());

        var req = new CreateActivationRequest(null, "dest_001");
        var ex = assertThrows(ValidationException.class, () -> service.create(req),
                "missing segmentId must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("segmentId"),
                "error details must contain 'segmentId' field");
        verify(validator).validateCreate(req);
        verifyNoMoreInteractions(validator);
        verifyNoInteractions(activationDao, destinationDao, segmentDao, activationsDs);
    }

    @Test
    void shouldFailCreateWhenDestinationIdMissing() {
        doThrow(new ValidationException("Validation failed",
                Map.of("destinationId", "Destination ID is required")))
                .when(validator).validateCreate(any());

        var req = new CreateActivationRequest("seg_0001", null);
        var ex = assertThrows(ValidationException.class, () -> service.create(req),
                "missing destinationId must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("destinationId"),
                "error details must contain 'destinationId' field");
        verify(validator).validateCreate(req);
        verifyNoMoreInteractions(validator);
        verifyNoInteractions(activationDao, destinationDao, segmentDao, activationsDs);
    }

    @Test
    void shouldFailCreateWhenSegmentNotFound() {
        doThrow(new ValidationException("Validation failed",
                Map.of("segmentId", "Segment not found: %s".formatted(NON_EXISTENT_ID))))
                .when(validator).validateCreate(any());

        var req = new CreateActivationRequest(NON_EXISTENT_ID, "dest_001");
        var ex = assertThrows(ValidationException.class, () -> service.create(req),
                "non-existing segment must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("segmentId"),
                "error details must contain 'segmentId' field");
        verify(validator).validateCreate(req);
        verifyNoMoreInteractions(validator);
        verifyNoInteractions(activationDao, destinationDao, segmentDao, activationsDs);
    }

    @Test
    void shouldFailCreateWhenDestinationNotFound() {
        doThrow(new ValidationException("Validation failed",
                Map.of("destinationId", "Destination not found: dest_0999")))
                .when(validator).validateCreate(any());

        var req = new CreateActivationRequest("seg_0001", "dest_0999");
        var ex = assertThrows(ValidationException.class, () -> service.create(req),
                "non-existing destination must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("destinationId"),
                "error details must contain 'destinationId' field");
        verify(validator).validateCreate(req);
        verifyNoMoreInteractions(validator);
        verifyNoInteractions(activationDao, destinationDao, segmentDao, activationsDs);
    }

    @Test
    void shouldRollbackOnCreateError() throws SQLException {
        when(activationsDs.getConnection()).thenReturn(connection);
        doThrow(new RuntimeException("DB error")).when(activationDao).create(any(), any(), any());

        var req = new CreateActivationRequest("seg_0001", "dest_001");
        assertThrows(RuntimeException.class, () -> service.create(req),
                "DB error must propagate as RuntimeException");
        verify(validator).validateCreate(req);
        verify(activationsDs).getConnection();
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).close();
        verifyNoMoreInteractions(validator, activationsDs, connection);
        verifyNoInteractions(segmentDao, destinationDao);
    }
}
