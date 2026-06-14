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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationServiceTest {

    @Mock ActivationDao activationDao;
    @Mock DestinationDao destinationDao;
    @Mock SegmentDao segmentDao;
    @Mock DataSource activationsDs;
    @Mock Connection connection;

    private ActivationService service;

    @BeforeEach
    void setUp() {
        service = new ActivationService(activationDao, destinationDao, segmentDao);
    }

    // list

    @Test
    void shouldListActivations() {
        var dests = List.of(
                new Destination("dest_001", "Google Ads", "#34A853"),
                new Destination("dest_002", "Meta", "#1877F2"));
        when(destinationDao.findAll()).thenReturn(dests);

        var acts = List.of(
                Instancio.of(Activation.class).set(Select.field(Activation::getDestinationId), "dest_001").create(),
                Instancio.of(Activation.class).set(Select.field(Activation::getDestinationId), "dest_002").create());
        when(activationDao.list(null, null, 1, 12)).thenReturn(new ActivationListResult(acts, 2));

        var result = service.list(null, null, 1, 12);
        assertEquals(2, result.data().size());
        assertEquals("Google Ads", result.data().get(0).getDestination().name());
    }

    // getActivationsForSegment

    @Test
    void shouldReturnActivationsForSegment() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(new Segment()));
        when(destinationDao.findAll()).thenReturn(List.of());
        when(activationDao.list(eq("seg_0001"), isNull(), eq(1), eq(12)))
                .thenReturn(new ActivationListResult(List.of(), 0));

        var result = service.getActivationsForSegment("seg_0001", 1, 12);
        assertTrue(result.data().isEmpty());
    }

    @Test
    void shouldThrowNotFoundWhenSegmentMissingForActivations() {
        when(segmentDao.findById("seg_0999")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.getActivationsForSegment("seg_0999", 1, 12));
    }

    // create

    @Test
    void shouldCreateActivation() throws SQLException {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(new Segment()));
        var dests = List.of(new Destination("dest_001", "Google Ads", "#34A853"));
        when(destinationDao.findAll()).thenReturn(dests);
        when(activationsDs.getConnection()).thenReturn(connection);
        when(activationDao.create(any(), eq("seg_0001"), eq("dest_001")))
                .thenReturn(Instancio.of(Activation.class)
                        .set(Select.field(Activation::getDestinationId), "dest_001")
                        .create());

        var req = new CreateActivationRequest("seg_0001", "dest_001");
        var result = service.create(req, activationsDs);
        assertNotNull(result);
        assertEquals("Google Ads", result.getDestination().name());
        verify(connection).commit();
    }

    @Test
    void shouldFailCreateWhenSegmentIdMissing() {
        var req = new CreateActivationRequest(null, "dest_001");
        var ex = assertThrows(ValidationException.class, () -> service.create(req, activationsDs));
        assertTrue(ex.getDetails().containsKey("segmentId"));
    }

    @Test
    void shouldFailCreateWhenDestinationIdMissing() {
        var req = new CreateActivationRequest("seg_0001", null);
        var ex = assertThrows(ValidationException.class, () -> service.create(req, activationsDs));
        assertTrue(ex.getDetails().containsKey("destinationId"));
    }

    @Test
    void shouldFailCreateWhenSegmentNotFound() {
        when(segmentDao.findById("seg_0999")).thenReturn(Optional.empty());
        var req = new CreateActivationRequest("seg_0999", "dest_001");
        var ex = assertThrows(ValidationException.class, () -> service.create(req, activationsDs));
        assertTrue(ex.getDetails().containsKey("segmentId"));
    }

    @Test
    void shouldFailCreateWhenDestinationNotFound() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(new Segment()));
        when(destinationDao.findAll()).thenReturn(List.of());

        var req = new CreateActivationRequest("seg_0001", "dest_0999");
        var ex = assertThrows(ValidationException.class, () -> service.create(req, activationsDs));
        assertTrue(ex.getDetails().containsKey("destinationId"));
    }

    @Test
    void shouldRollbackOnCreateError() throws SQLException {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(new Segment()));
        when(destinationDao.findAll()).thenReturn(List.of(new Destination("dest_001", "G", "red")));
        when(activationsDs.getConnection()).thenReturn(connection);
        doThrow(new RuntimeException("DB error")).when(activationDao).create(any(), any(), any());

        var req = new CreateActivationRequest("seg_0001", "dest_001");
        assertThrows(RuntimeException.class, () -> service.create(req, activationsDs));
        verify(connection).rollback();
    }
}
