# Prompt 5 — Metrics Aggregation API

> Create a new controller `/api/metrics` that provides:
> - Total event count
> - Count by event type
> - Count by event source
>
> Use JPQL queries and create a `MetricsService` to handle this logic.

## Implementation Summary

This prompt implemented comprehensive metrics and analytics capabilities by:

### Files Created:
- `src/main/java/com/eventpulse/controller/MetricsController.java` - Metrics API endpoints
- `src/main/java/com/eventpulse/service/MetricsService.java` - Analytics business logic
- `src/main/java/com/eventpulse/dto/MetricsResponse.java` - Structured metrics response
- `src/main/java/com/eventpulse/dto/TypeCount.java` - Event type aggregation
- `src/main/java/com/eventpulse/dto/SourceCount.java` - Event source aggregation
- `src/main/java/com/eventpulse/dto/CountResponse.java` - Simple count response

### Key Features Implemented:

#### Metrics Endpoints:
- `GET /api/metrics` - Comprehensive metrics with all aggregations
- `GET /api/metrics/total` - Total event count only
- `GET /api/metrics/type/{type}` - Count for specific event type
- `GET /api/metrics/source/{source}` - Count for specific event source
- `GET /api/metrics?startTime=X&endTime=Y` - Time-range filtered metrics
- `GET /api/metrics/recent?hours=N` - Recent events metrics

#### JPQL Queries:
- **Event Count Queries**:
  - `COUNT(e)` - Total event count
  - `COUNT(e) WHERE e.type = ?1` - Count by type
  - `COUNT(e) WHERE e.source = ?1` - Count by source
  - `COUNT(e) WHERE e.timestamp BETWEEN ?1 AND ?2` - Time range filtering

- **Aggregation Queries**:
  - `SELECT e.type, COUNT(e) FROM Event e GROUP BY e.type` - Events by type
  - `SELECT e.source, COUNT(e) FROM Event e GROUP BY e.source` - Events by source
  - Time-range filtered aggregations with `WHERE e.timestamp BETWEEN ?1 AND ?2`

#### Service Layer Features:
- **Comprehensive Metrics** - Total count, type breakdown, source breakdown
- **Time-based Filtering** - Metrics for specific time ranges
- **Recent Metrics** - Configurable time windows (hours)
- **Performance Optimized** - Efficient JPQL queries with proper indexing

#### Data Transfer Objects:
- **MetricsResponse** - Complete metrics with all aggregations
- **TypeCount** - Type-based aggregation results
- **SourceCount** - Source-based aggregation results
- **CountResponse** - Simple numeric count responses

#### Advanced Features:
- **Time Range Validation** - Ensures start time is before end time
- **Flexible Time Windows** - Recent metrics with configurable hours
- **Null Safety** - Proper handling of empty result sets
- **Performance Optimization** - Efficient database queries
- **Comprehensive Logging** - Metrics query logging for monitoring

### API Usage Examples:
```bash
# Get all metrics
GET /api/metrics

# Get total count
GET /api/metrics/total

# Get count by type
GET /api/metrics/type/login

# Get count by source
GET /api/metrics/source/user-service

# Get metrics for time range
GET /api/metrics?startTime=2023-12-01T00:00:00Z&endTime=2023-12-01T23:59:59Z

# Get recent metrics
GET /api/metrics/recent?hours=24
```

### Response Format:
```json
{
  "totalEvents": 1250,
  "eventsByType": [
    {"type": "login", "count": 450},
    {"type": "error", "count": 320}
  ],
  "eventsBySource": [
    {"source": "user-service", "count": 650},
    {"source": "payment-service", "count": 400}
  ]
}
```

This metrics API provided comprehensive analytics capabilities for monitoring event patterns, identifying trends, and supporting business intelligence requirements.
