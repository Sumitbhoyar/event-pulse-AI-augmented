# Phase 5: WebSocket Support

## Objective
Add real-time event broadcasting using Spring WebSocket with STOMP protocol.

## Requirements

- Configure WebSocket endpoint at `/ws`
- Broadcast new events to `/topic/events` when created
- Provide JavaScript client example

## Implementation Steps

### 1. Added WebSocket Dependency

Updated `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. WebSocket Configuration

Created `WebSocketConfig.java`:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

### 3. Broadcasting from Controller

Updated `EventController.java`:
- Injected `SimpMessagingTemplate`
- After saving event, broadcast to WebSocket subscribers:
```java
messagingTemplate.convertAndSend("/topic/events", response);
```

### 4. JavaScript Client

Created `ws-client.html`:
- WebSocket connection setup
- STOMP protocol handshake
- Subscribe to `/topic/events`
- Create sample events via HTTP
- Display received events in real-time

## Files Created/Modified

- `src/main/java/com/eventpulse/config/WebSocketConfig.java` (new)
- `src/main/resources/static/ws-client.html` (new)
- `src/main/java/com/eventpulse/controller/EventController.java` (modified)
- `pom.xml` (modified)

## Technical Details

### WebSocket Flow

```
Client connects to /ws
        ↓
STOMP handshake
        ↓
Subscribe to /topic/events
        ↓
Server creates event (POST /api/events)
        ↓
Server broadcasts to /topic/events
        ↓
All subscribers receive event
```

### STOMP Configuration

- **Endpoint**: `/ws` (WebSocket connection point)
- **Broker**: `/topic` (simple in-memory broker)
- **Application prefix**: `/app` (for client messages)
- **Allowed origins**: `*` (all origins, configure for production)

### Client Implementation

**Minimal STOMP frames**:
```javascript
// Connect
socket.send('CONNECT\naccept-version:1.2\n\n\u0000');

// Subscribe
socket.send('SUBSCRIBE\nid:sub-1\ndestination:/topic/events\n\n\u0000');
```

## Challenges Encountered

### SimpMessagingTemplate Injection
**Issue**: How to inject messaging template into controller.

**Solution**: Constructor injection alongside EventService.

### WebSocket Security
**Issue**: WebSocket endpoints need to be accessible without JWT.

**Solution**: Added `/ws` and `/ws/**` to permitAll in SecurityConfig.

### Browser WebSocket Testing
**Issue**: Need easy way to test WebSocket functionality.

**Solution**: Created standalone HTML client with:
- Pure JavaScript (no external dependencies)
- Manual STOMP frame handling
- Event creation form
- Real-time log display

## Outcome

✅ WebSocket endpoint configured at /ws  
✅ STOMP protocol implemented  
✅ Real-time event broadcasting working  
✅ JavaScript client functional  
✅ Events appear in real-time when created

