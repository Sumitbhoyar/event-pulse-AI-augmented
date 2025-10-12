# Prompt 6 — Real-Time Updates (Optional Advanced)

> Add WebSocket support using Spring STOMP:
> - Configure WebSocket endpoint at `/ws`
> - Broadcast new events to `/topic/events` when they are created
> - Add a simple JavaScript client example

## Implementation Summary

This prompt implemented real-time event broadcasting using WebSocket technology by:

### Files Created:
- `src/main/java/com/eventpulse/config/WebSocketConfig.java` - WebSocket configuration
- `src/main/java/com/eventpulse/service/WebSocketService.java` - Real-time messaging service
- `src/main/java/com/eventpulse/dto/WebSocketMessage.java` - WebSocket message DTO
- `src/main/resources/static/websocket-test.html` - JavaScript client example
- `src/main/java/com/eventpulse/controller/WebSocketController.java` - WebSocket controller

### Key Features Implemented:

#### WebSocket Configuration:
- **STOMP Protocol** - Simple Text Oriented Messaging Protocol for WebSocket
- **Endpoint Registration** - WebSocket endpoint at `/ws`
- **Message Broker** - In-memory broker for topic-based messaging
- **CORS Support** - Cross-origin resource sharing for web clients
- **SockJS Fallback** - Graceful degradation for older browsers

#### Real-Time Broadcasting:
- **Event Broadcasting** - Automatic notification when events are created
- **Topic-based Messaging** - `/topic/events` for event notifications
- **Structured Messages** - JSON-formatted event data
- **Connection Management** - Proper WebSocket connection handling

#### Service Integration:
- **EventController Integration** - Automatic broadcasting on event creation
- **WebSocketService** - Centralized messaging service
- **Message Serialization** - JSON serialization for WebSocket messages
- **Error Handling** - Graceful handling of WebSocket errors

#### JavaScript Client Example:
- **SockJS Client** - Modern WebSocket client library
- **STOMP Protocol** - Full STOMP implementation
- **Real-time Updates** - Live event display
- **Connection Management** - Connect/disconnect functionality
- **Interactive Testing** - Send messages and view real-time updates

### Technical Implementation:

#### WebSocket Configuration:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .withSockJS();
    }
}
```

#### Event Broadcasting:
```java
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    
    public void broadcastEvent(Event event) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type("EVENT_CREATED")
            .data(event)
            .timestamp(Instant.now())
            .build();
            
        messagingTemplate.convertAndSend("/topic/events", message);
    }
}
```

#### JavaScript Client:
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    stompClient.subscribe('/topic/events', function(message) {
        const eventMessage = JSON.parse(message.body);
        console.log('New event:', eventMessage);
        displayEvent(eventMessage.data);
    });
});
```

### Features:

#### Real-Time Capabilities:
- **Live Event Notifications** - Instant updates when events are created
- **Multiple Client Support** - Multiple clients can connect simultaneously
- **Connection Resilience** - Automatic reconnection on connection loss
- **Message History** - Clients receive current events on connection

#### Testing Interface:
- **Interactive Web Page** - HTML page for testing WebSocket functionality
- **Event Display** - Real-time event list with timestamps
- **Message Sending** - Ability to send test messages
- **Connection Status** - Visual connection status indicator

#### Integration Benefits:
- **Event-Driven Architecture** - Real-time event propagation
- **Microservices Communication** - WebSocket-based service communication
- **Dashboard Updates** - Live dashboard and monitoring updates
- **User Notifications** - Real-time user notifications

### Usage Examples:

#### Connect to WebSocket:
```javascript
// Connect to WebSocket endpoint
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);
```

#### Subscribe to Events:
```javascript
// Subscribe to event updates
stompClient.subscribe('/topic/events', function(message) {
    const eventMessage = JSON.parse(message.body);
    console.log('New event:', eventMessage);
});
```

#### Send Messages:
```javascript
// Send message to server
stompClient.send('/app/events', {}, JSON.stringify({
    message: 'Hello from client'
}));
```

This WebSocket implementation provided real-time capabilities for the EventPulse platform, enabling live event monitoring, dashboard updates, and interactive client applications.
