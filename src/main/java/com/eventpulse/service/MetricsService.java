package com.eventpulse.service;

import com.eventpulse.dto.MetricsResponse;
import com.eventpulse.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsService {

    private final EventRepository eventRepository;

    public MetricsService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public MetricsResponse getMetrics() {
        long total = eventRepository.countAllEvents();

        Map<String, Long> byType = convertTuples(eventRepository.countByType());
        Map<String, Long> bySource = convertTuples(eventRepository.countBySource());

        return new MetricsResponse(total, byType, bySource);
    }

    private Map<String, Long> convertTuples(List<Object[]> tuples) {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : tuples) {
            if (row == null || row.length < 2) {
                continue;
            }
            String key = row[0] != null ? row[0].toString() : "";
            Long value = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            result.put(key, value);
        }
        return result;
    }
}


