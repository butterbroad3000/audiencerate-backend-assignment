package com.audiencerate.service;

import com.audiencerate.dao.ActivationDao;
import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.dao.SegmentDao;
import com.audiencerate.dao.SegmentDao.StatusCount;
import com.audiencerate.model.Overview;
import com.audiencerate.model.Overview.Kpis;
import com.audiencerate.model.Overview.TopSegment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OverviewService {

    private final DataSourceDao dataSourceDao;
    private final SegmentDao segmentDao;
    private final ActivationDao activationDao;

    public OverviewService(DataSourceDao dataSourceDao, SegmentDao segmentDao, ActivationDao activationDao) {
        this.dataSourceDao = dataSourceDao;
        this.segmentDao = segmentDao;
        this.activationDao = activationDao;
    }

    public Overview getOverview() {
        // Collect data from all three databases
        long totalProfiles = dataSourceDao.sumProfilesCount();
        int totalSegments = segmentDao.count();
        int activeSegments = segmentDao.countByStatus("active");
        BigDecimal avgMatchRate = segmentDao.averageMatchRate();
        int totalActivations = activationDao.count();

        // identitiesResolved = totalProfiles * avgMatchRate
        long identitiesResolved = BigDecimal.valueOf(totalProfiles)
                .multiply(avgMatchRate)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        // Build KPIs
        Kpis kpis = new Kpis(totalProfiles, totalSegments, activeSegments,
                avgMatchRate.setScale(3, RoundingMode.HALF_UP), identitiesResolved, totalActivations);

        // segmentsByStatus
        Map<String, Integer> byStatus = new LinkedHashMap<>();
        byStatus.put("active", 0);
        byStatus.put("draft", 0);
        byStatus.put("archived", 0);
        for (StatusCount sc : segmentDao.countGroupByStatus()) {
            byStatus.put(sc.status(), sc.count());
        }

        // Top 5 segments by audience size
        List<TopSegment> topSegments = segmentDao.findTopByAudienceSize(5).stream()
                .map(segment -> new TopSegment(segment.id(), segment.name(), segment.audienceSize()))
                .toList();

        return new Overview(kpis, byStatus, topSegments);
    }
}
