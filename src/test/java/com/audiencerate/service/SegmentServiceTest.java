package com.audiencerate.service;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.dao.SegmentDao.SegmentListResult;
import com.audiencerate.dao.SegmentTrendDao;
import com.audiencerate.error.NotFoundException;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.Segment;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;
import com.audiencerate.validation.SegmentValidator;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegmentServiceTest {

    @Mock SegmentDao segmentDao;
    @Mock SegmentTrendDao trendDao;
    @Mock DataSourceDao dataSourceDao;
    @Mock DataSource dataSource;
    @Mock Connection connection;

    private SegmentService service;

    @BeforeEach
    void setUp() {
        service = new SegmentService(segmentDao, trendDao, new SegmentValidator(), dataSourceDao);
    }

    // getById

    @Test
    void shouldReturnSegmentById() {
        var segment = Instancio.of(Segment.class)
                .set(Select.field(Segment::getId), "seg_0001")
                .create();
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(segment));

        var result = service.getById("seg_0001");
        assertEquals("seg_0001", result.getId());
    }

    @Test
    void shouldThrowNotFoundWhenSegmentMissing() {
        when(segmentDao.findById("seg_0999")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById("seg_0999"));
    }

    // list

    @Test
    void shouldReturnPagedList() {
        var segments = List.of(
                Instancio.of(Segment.class).set(Select.field(Segment::getId), "seg_0001").create(),
                Instancio.of(Segment.class).set(Select.field(Segment::getId), "seg_0002").create());
        when(segmentDao.list(any(), any(), any(), any(), any(), eq(1), eq(12)))
                .thenReturn(new SegmentListResult(segments, 2));

        var result = service.list(null, null, null, null, null, 1, 12);
        assertEquals(2, result.data().size());
        assertEquals(1, result.pagination().totalPages());
    }

    @ParameterizedTest
    @CsvSource({"1,12,36,3", "2,12,36,3", "4,10,36,4"})
    void shouldComputePagination(int page, int pageSize, long total, int expectedPages) {
        var segments = Instancio.ofList(Segment.class).size((int) Math.min(pageSize, total)).create();
        when(segmentDao.list(any(), any(), any(), any(), any(), eq(page), eq(pageSize)))
                .thenReturn(new SegmentListResult(segments, total));

        var result = service.list(null, null, null, null, null, page, pageSize);
        assertEquals(expectedPages, result.pagination().totalPages());
    }

    // trend

    @Test
    void shouldValidateTrendRangeBelow7() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(new Segment()));
        var ex = assertThrows(ValidationException.class, () -> service.getTrend("seg_0001", 5));
        assertTrue(ex.getDetails().containsKey("range"));
    }

    @Test
    void shouldValidateTrendRangeAbove180() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(new Segment()));
        var ex = assertThrows(ValidationException.class, () -> service.getTrend("seg_0001", 200));
        assertTrue(ex.getDetails().containsKey("range"));
    }

    @Test
    void shouldThrowNotFoundWhenSegmentMissingForTrend() {
        when(segmentDao.findById("seg_0999")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getTrend("seg_0999", 30));
    }

    @Test
    void shouldReturnTrendData() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(new Segment()));
        var points = Instancio.ofList(com.audiencerate.model.SegmentTrendPoint.class).size(5).create();
        when(trendDao.findBySegmentId("seg_0001", 30)).thenReturn(points);

        var result = service.getTrend("seg_0001", 30);
        assertEquals(5, result.size());
    }

    // create

    @Test
    void shouldCreateSegment() throws SQLException {
        when(dataSourceDao.findExistingIds(any())).thenReturn(Set.of("ds_001"));
        when(dataSource.getConnection()).thenReturn(connection);
        when(segmentDao.nextId()).thenReturn("seg_0100");
        when(segmentDao.insert(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Instancio.of(Segment.class).set(Select.field(Segment::getId), "seg_0100").create());
        when(segmentDao.findById("seg_0100"))
                .thenReturn(Optional.of(Instancio.of(Segment.class).set(Select.field(Segment::getId), "seg_0100").create()));

        var req = new CreateSegmentRequest("My Segment", "desc", "active",
                List.of("ds_001"), List.of("tag1"));

        var result = service.create(req, dataSource);
        assertNotNull(result);
        verify(segmentDao).insertTags(any(), eq("seg_0100"), eq(List.of("tag1")));
        verify(segmentDao).insertDataSources(any(), eq("seg_0100"), eq(List.of("ds_001")));
        verify(connection).commit();
    }

    @Test
    void shouldDefaultStatusToDraftWhenNull() throws SQLException {
        when(dataSourceDao.findExistingIds(any())).thenReturn(Set.of());
        when(dataSource.getConnection()).thenReturn(connection);
        when(segmentDao.nextId()).thenReturn("seg_0100");
        when(segmentDao.insert(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Instancio.create(Segment.class));
        when(segmentDao.findById(any())).thenReturn(Optional.of(Instancio.create(Segment.class)));

        var req = new CreateSegmentRequest("Name", null, null, null, null);
        service.create(req, dataSource);
        verify(segmentDao).insert(any(), any(), any(), any(), eq("draft"), any(), any(), any());
    }

    @Test
    void shouldRollbackOnError() throws SQLException {
        when(dataSourceDao.findExistingIds(any())).thenReturn(Set.of());
        when(dataSource.getConnection()).thenReturn(connection);
        when(segmentDao.nextId()).thenReturn("seg_0100");
        doThrow(new RuntimeException("DB error")).when(segmentDao)
                .insert(any(), any(), any(), any(), any(), any(), any(), any());

        var req = new CreateSegmentRequest("Name", null, null, null, null);
        assertThrows(RuntimeException.class, () -> service.create(req, dataSource));
        verify(connection).rollback();
    }

    @Test
    void shouldFailCreateWhenDataSourceNotFound() {
        when(dataSourceDao.findExistingIds(any())).thenReturn(Set.of());
        var req = new CreateSegmentRequest("Name", null, null,
                List.of("ds_001"), null);
        var ex = assertThrows(ValidationException.class, () -> service.create(req, dataSource));
        assertTrue(ex.getDetails().containsKey("dataSourceIds"));
    }

    // update

    @Test
    void shouldUpdateSegment() {
        when(segmentDao.update(eq("seg_0001"), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(Instancio.create(Segment.class)));

        var req = new UpdateSegmentRequest("New Name", null, "active", null, null);
        var result = service.update("seg_0001", req);
        assertNotNull(result);
    }

    @Test
    void shouldFailUpdateWhenSegmentNotFound() {
        when(segmentDao.update(eq("seg_0999"), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        var req = new UpdateSegmentRequest("New Name", null, null, null, null);
        assertThrows(NotFoundException.class, () -> service.update("seg_0999", req));
    }

    // delete

    @Test
    void shouldDeleteSegment() {
        when(segmentDao.delete("seg_0001")).thenReturn(true);
        assertDoesNotThrow(() -> service.delete("seg_0001"));
    }

    @Test
    void shouldFailDeleteWhenSegmentNotFound() {
        when(segmentDao.delete("seg_0999")).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.delete("seg_0999"));
    }
}
