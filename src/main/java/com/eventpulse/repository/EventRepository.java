package com.eventpulse.repository;

import com.eventpulse.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
     * Find events by type (case-insensitive)
     */
    List<Event> findByTypeIgnoreCase(String type);

    /**
     * Find events by source (case-insensitive)
     */
    List<Event> findBySourceIgnoreCase(String source);

    /**
     * Find events within a timestamp range (inclusive)
     */
    List<Event> findByTimestampBetween(Instant startTime, Instant endTime);

    /**
     * Find events by type and source (case-insensitive)
     */
    List<Event> findByTypeIgnoreCaseAndSourceIgnoreCase(String type, String source);

    /**
     * Find events by type within a timestamp range
     */
    List<Event> findByTypeIgnoreCaseAndTimestampBetween(String type, Instant startTime, Instant endTime);

    /**
     * Find events by source within a timestamp range
     */
    List<Event> findBySourceIgnoreCaseAndTimestampBetween(String source, Instant startTime, Instant endTime);

    /**
     * Find events by type, source, and timestamp range
     */
    List<Event> findByTypeIgnoreCaseAndSourceIgnoreCaseAndTimestampBetween(
            String type, String source, Instant startTime, Instant endTime);

    // Metrics aggregation queries

    /**
     * Get total count of events
     */
    @Query("SELECT COUNT(e) FROM Event e")
    Long getTotalEventCount();

    /**
     * Get total count of events within a time range
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.timestamp BETWEEN :startTime AND :endTime")
    Long getTotalEventCountByTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Get count of events grouped by type
     */
    @Query("SELECT e.type as type, COUNT(e) as count FROM Event e GROUP BY e.type ORDER BY COUNT(e) DESC")
    List<Object[]> getEventCountByType();

    /**
     * Get count of events grouped by type within a time range
     */
    @Query("SELECT e.type as type, COUNT(e) as count FROM Event e WHERE e.timestamp BETWEEN :startTime AND :endTime GROUP BY e.type ORDER BY COUNT(e) DESC")
    List<Object[]> getEventCountByTypeInTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Get count of events grouped by source
     */
    @Query("SELECT e.source as source, COUNT(e) as count FROM Event e GROUP BY e.source ORDER BY COUNT(e) DESC")
    List<Object[]> getEventCountBySource();

    /**
     * Get count of events grouped by source within a time range
     */
    @Query("SELECT e.source as source, COUNT(e) as count FROM Event e WHERE e.timestamp BETWEEN :startTime AND :endTime GROUP BY e.source ORDER BY COUNT(e) DESC")
    List<Object[]> getEventCountBySourceInTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Get count of events by specific type
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.type = :type")
    Long getEventCountBySpecificType(@Param("type") String type);

    /**
     * Get count of events by specific source
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.source = :source")
    Long getEventCountBySpecificSource(@Param("source") String source);

    /**
     * Get recent events count (last N hours)
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.timestamp >= :since")
    Long getRecentEventsCount(@Param("since") Instant since);
}
