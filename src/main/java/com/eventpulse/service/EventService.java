package com.eventpulse.service;

import com.eventpulse.entity.Event;
import com.eventpulse.repository.EventRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final Validator validator;

    public EventService(EventRepository eventRepository, Validator validator) {
        this.eventRepository = Objects.requireNonNull(eventRepository, "eventRepository");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public Event save(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }

        // Default timestamp if not provided
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }

        // Validate using bean validation
        Set<ConstraintViolation<Event>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return eventRepository.save(event);
    }

    public List<Event> findByFilters(String type, String source, Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }

        Specification<Event> spec = Specification.where(null);

        if (StringUtils.hasText(type)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }

        if (StringUtils.hasText(source)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("source"), source));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), to));
        }

        return eventRepository.findAll(spec);
    }
}


