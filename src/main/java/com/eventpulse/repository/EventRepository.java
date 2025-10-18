package com.eventpulse.repository;

import com.eventpulse.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    @Query("select count(e) from Event e")
    long countAllEvents();

    @Query("select e.type as key, count(e) as value from Event e group by e.type")
    List<Object[]> countByType();

    @Query("select e.source as key, count(e) as value from Event e group by e.source")
    List<Object[]> countBySource();
}
