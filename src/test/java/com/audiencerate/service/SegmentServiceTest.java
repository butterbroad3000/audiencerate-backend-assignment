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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegmentServiceTest {

    private static final String NON_EXISTENT_ID = "seg_0999";

    @Mock SegmentDao segmentDao;
    @Mock SegmentTrendDao trendDao;
    @Mock DataSourceDao dataSourceDao;

    private SegmentService service;

    @BeforeEach
    void setUp() {
        service = new SegmentService(segmentDao, trendDao, new SegmentValidator(dataSourceDao));
    }

    // ── getById ──

    @Test
    void shouldReturnSegmentById() {
        var segment = Instancio.of(Segment.class)
                .set(Select.field(Segment::id), "seg_0001")
                .create();
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(segment));

        var result = service.getById("seg_0001");

        assertEquals("seg_0001", result.id(), "returned segment must have requested id");
        verify(segmentDao).findById("seg_0001");
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    @Test
    void shouldThrowNotFoundWhenSegmentMissing() {
        when(segmentDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(NON_EXISTENT_ID),
                "missing segment must throw NotFoundException");
        verify(segmentDao).findById(NON_EXISTENT_ID);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    // ── list ──

    @Test
    void shouldReturnPagedList() {
        var segments = List.of(
                Instancio.of(Segment.class).set(Select.field(Segment::id), "seg_0001").create(),
                Instancio.of(Segment.class).set(Select.field(Segment::id), "seg_0002").create());
        when(segmentDao.list(any(), any(), any(), any(), any(), eq(1), eq(12)))
                .thenReturn(new SegmentListResult(segments, 2));

        var result = service.list(null, null, null, null, null, 1, 12);

        assertEquals(2, result.data().size(), "two segments in result");
        assertEquals(1, result.pagination().totalPages(), "single page for 2 items with pageSize 12");
        verify(segmentDao).list(null, null, null, null, null, 1, 12);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    @ParameterizedTest
    @CsvSource({"1,12,36,3", "2,12,36,3", "4,10,36,4"})
    void shouldComputePagination(int page, int pageSize, long total, int expectedPages) {
        var segments = Instancio.ofList(Segment.class).size((int) Math.min(pageSize, total)).create();
        when(segmentDao.list(any(), any(), any(), any(), any(), eq(page), eq(pageSize)))
                .thenReturn(new SegmentListResult(segments, total));

        var result = service.list(null, null, null, null, null, page, pageSize);

        assertEquals(expectedPages, result.pagination().totalPages(),
                "pagination must ceil(total / pageSize)");
        verify(segmentDao).list(null, null, null, null, null, page, pageSize);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    // ── trend ──

    @Test
    void shouldValidateTrendRangeBelow7() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(Instancio.create(Segment.class)));

        var ex = assertThrows(ValidationException.class, () -> service.getTrend("seg_0001", 5),
                "range below 7 must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("range"), "error details must contain 'range' field");
        verify(segmentDao).findById("seg_0001");
        verifyNoMoreInteractions(segmentDao, trendDao);
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldValidateTrendRangeAbove180() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(Instancio.create(Segment.class)));

        var ex = assertThrows(ValidationException.class, () -> service.getTrend("seg_0001", 200),
                "range above 180 must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("range"), "error details must contain 'range' field");
        verify(segmentDao).findById("seg_0001");
        verifyNoMoreInteractions(segmentDao, trendDao);
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldThrowNotFoundWhenSegmentMissingForTrend() {
        when(segmentDao.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getTrend(NON_EXISTENT_ID, 30),
                "missing segment for trend must throw NotFoundException");
        verify(segmentDao).findById(NON_EXISTENT_ID);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    @Test
    void shouldReturnTrendData() {
        when(segmentDao.findById("seg_0001")).thenReturn(Optional.of(Instancio.create(Segment.class)));
        var points = Instancio.ofList(com.audiencerate.model.SegmentTrendPoint.class).size(5).create();
        when(trendDao.findBySegmentId("seg_0001", 30)).thenReturn(points);

        var result = service.getTrend("seg_0001", 30);

        assertEquals(5, result.size(), "five trend points returned");
        verify(segmentDao).findById("seg_0001");
        verify(trendDao).findBySegmentId("seg_0001", 30);
        verifyNoMoreInteractions(segmentDao, trendDao);
        verifyNoInteractions(dataSourceDao);
    }

    // ── create ──

    @Test
    void shouldCreateSegment() {
        when(dataSourceDao.findExistingIds(any())).thenReturn(java.util.Set.of("ds_001"));
        when(segmentDao.create(eq("My Segment"), eq("desc"), eq("active"),
                eq(List.of("tag1")), eq(List.of("ds_001"))))
                .thenReturn(Instancio.of(Segment.class).create());

        var req = new CreateSegmentRequest("My Segment", "desc", "active",
                List.of("ds_001"), List.of("tag1"));

        var result = service.create(req);

        assertNotNull(result, "created segment must not be null");
        verify(dataSourceDao).findExistingIds(List.of("ds_001"));
        verify(segmentDao).create("My Segment", "desc", "active", List.of("tag1"), List.of("ds_001"));
        verifyNoMoreInteractions(segmentDao, dataSourceDao);
        verifyNoInteractions(trendDao);
    }

    @Test
    void shouldDefaultStatusToDraftWhenNull() {
        when(segmentDao.create(eq("Name"), eq(null), eq("draft"),
                eq(null), eq(null)))
                .thenReturn(Instancio.create(Segment.class));

        var req = new CreateSegmentRequest("Name", null, null, null, null);
        service.create(req);

        verify(segmentDao).create("Name", null, "draft", null, null);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    @Test
    void shouldFailCreateWhenDataSourceNotFound() {
        when(dataSourceDao.findExistingIds(any())).thenReturn(java.util.Set.of());

        var req = new CreateSegmentRequest("Name", null, null,
                List.of("ds_001"), null);
        var ex = assertThrows(ValidationException.class, () -> service.create(req),
                "non-existing data source must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("dataSourceIds"),
                "error details must contain 'dataSourceIds' field");
        verify(dataSourceDao).findExistingIds(List.of("ds_001"));
        verifyNoMoreInteractions(dataSourceDao);
        verifyNoInteractions(segmentDao, trendDao);
    }

    // ── update ──

    @Test
    void shouldUpdateSegment() {
        when(segmentDao.update(eq("seg_0001"), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(Instancio.create(Segment.class)));

        var req = new UpdateSegmentRequest("New Name", null, "active", null, null);
        var result = service.update("seg_0001", req);

        assertNotNull(result, "updated segment must not be null");
        verify(segmentDao).update(eq("seg_0001"), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    @Test
    void shouldFailUpdateWhenSegmentNotFound() {
        when(segmentDao.update(eq(NON_EXISTENT_ID), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        var req = new UpdateSegmentRequest("New Name", null, null, null, null);
        assertThrows(NotFoundException.class, () -> service.update(NON_EXISTENT_ID, req),
                "update of missing segment must throw NotFoundException");
        verify(segmentDao).update(eq(NON_EXISTENT_ID), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    // ── delete ──

    @Test
    void shouldDeleteSegment() {
        when(segmentDao.delete("seg_0001")).thenReturn(true);

        assertDoesNotThrow(() -> service.delete("seg_0001"),
                "delete of existing segment must not throw");
        verify(segmentDao).delete("seg_0001");
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }

    @Test
    void shouldFailDeleteWhenSegmentNotFound() {
        when(segmentDao.delete(NON_EXISTENT_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(NON_EXISTENT_ID),
                "delete of missing segment must throw NotFoundException");
        verify(segmentDao).delete(NON_EXISTENT_ID);
        verifyNoMoreInteractions(segmentDao);
        verifyNoInteractions(trendDao, dataSourceDao);
    }
}
