package com.eventpulse.dto;

import java.util.Map;

public class MetricsResponse {

    private long totalCount;
    private Map<String, Long> countByType;
    private Map<String, Long> countBySource;

    public MetricsResponse() {}

    public MetricsResponse(long totalCount, Map<String, Long> countByType, Map<String, Long> countBySource) {
        this.totalCount = totalCount;
        this.countByType = countByType;
        this.countBySource = countBySource;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public Map<String, Long> getCountByType() {
        return countByType;
    }

    public void setCountByType(Map<String, Long> countByType) {
        this.countByType = countByType;
    }

    public Map<String, Long> getCountBySource() {
        return countBySource;
    }

    public void setCountBySource(Map<String, Long> countBySource) {
        this.countBySource = countBySource;
    }
}


