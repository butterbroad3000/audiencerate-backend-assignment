package com.audiencerate.service;

import com.audiencerate.dao.ActivationDao;
import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.dao.SegmentDao.StatusCount;
import com.audiencerate.model.Segment;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OverviewServiceTest {

    @Mock DataSourceDao dataSourceDao;
    @Mock SegmentDao segmentDao;
    @Mock ActivationDao activationDao;

    private OverviewService service;

    @BeforeEach
    void setUp() {
        service = new OverviewService(dataSourceDao, segmentDao, activationDao);
    }

    @Test
    void shouldAggregateAcrossAllDatabases() {
        when(dataSourceDao.sumProfilesCount()).thenReturn(29_013_000L);
        when(segmentDao.count()).thenReturn(36);
        when(segmentDao.countByStatus("active")).thenReturn(14);
        when(segmentDao.averageMatchRate()).thenReturn(new BigDecimal("0.701"));
        when(activationDao.count()).thenReturn(19);
        when(segmentDao.countGroupByStatus()).thenReturn(List.of(
                new StatusCount("active", 14),
                new StatusCount("draft", 12),
                new StatusCount("archived", 10)));

        String expectedTopName = "Auto Intenders";
        long expectedTopAudience = 4_018_500L;
        var topSegments = List.of(
                Instancio.of(Segment.class)
                        .set(Select.field(Segment::id), "seg_0004")
                        .set(Select.field(Segment::name), expectedTopName)
                        .set(Select.field(Segment::audienceSize), expectedTopAudience)
                        .create());
        when(segmentDao.findTopByAudienceSize(5)).thenReturn(topSegments);

        var result = service.getOverview();

        assertEquals(29_013_000L, result.kpis().totalProfiles(), "total profiles across all data sources");
        assertEquals(36, result.kpis().totalSegments(), "total segment count");
        assertEquals(14, result.kpis().activeSegments(), "active segments count");
        assertEquals(new BigDecimal("0.701"), result.kpis().avgMatchRate(), "average match rate");
        assertEquals(19, result.kpis().totalActivations(), "total activations count");
        assertEquals(3, result.segmentsByStatus().size(), "three distinct status groups");
        assertEquals(14, result.segmentsByStatus().get("active"), "active segments in status breakdown");
        assertEquals(1, result.topSegments().size(), "single top segment returned");
        assertEquals(expectedTopName, result.topSegments().getFirst().name(), "top segment name matches expected");

        verify(dataSourceDao).sumProfilesCount();
        verify(segmentDao).count();
        verify(segmentDao).countByStatus("active");
        verify(segmentDao).averageMatchRate();
        verify(activationDao).count();
        verify(segmentDao).countGroupByStatus();
        verify(segmentDao).findTopByAudienceSize(5);
        verifyNoMoreInteractions(dataSourceDao, segmentDao, activationDao);
    }

    @Test
    void shouldHandleEmptyData() {
        when(dataSourceDao.sumProfilesCount()).thenReturn(0L);
        when(segmentDao.count()).thenReturn(0);
        when(segmentDao.countByStatus("active")).thenReturn(0);
        when(segmentDao.averageMatchRate()).thenReturn(BigDecimal.ZERO);
        when(activationDao.count()).thenReturn(0);
        when(segmentDao.countGroupByStatus()).thenReturn(List.of());
        when(segmentDao.findTopByAudienceSize(5)).thenReturn(List.of());

        var result = service.getOverview();

        assertEquals(0L, result.kpis().totalProfiles(), "zero profiles when no data sources");
        assertEquals(0, result.kpis().totalSegments(), "zero segments when empty");
        assertTrue(result.topSegments().isEmpty(), "empty top segments when no data");

        verify(dataSourceDao).sumProfilesCount();
        verify(segmentDao).count();
        verify(segmentDao).countByStatus("active");
        verify(segmentDao).averageMatchRate();
        verify(activationDao).count();
        verify(segmentDao).countGroupByStatus();
        verify(segmentDao).findTopByAudienceSize(5);
        verifyNoMoreInteractions(dataSourceDao, segmentDao, activationDao);
    }
}
