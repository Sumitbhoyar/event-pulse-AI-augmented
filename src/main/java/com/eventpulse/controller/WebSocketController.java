package com.eventpulse.controller;

import com.eventpulse.dto.EventWebSocketMessage;
import com.eventpulse.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService webSocketService;

    /**
     * Handle incoming messages from clients
     * @param message the message from the client
     * @param headerAccessor accessor for message headers
     * @return response message
     */
    @MessageMapping("/events")
    @SendToUser("/queue/events")
    public EventWebSocketMessage handleEventMessage(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.debug("Received message from client {}: {}", sessionId, message);
        
        // Send connection established message back to the client
        webSocketService.sendConnectionEstablished(sessionId);
        
        return EventWebSocketMessage.builder()
                .messageType("MESSAGE_RECEIVED")
                .timestamp(java.time.Instant.now())
                .description("Your message has been received: " + message)
                .build();
    }

    /**
     * Handle client subscription to events
     * @param headerAccessor accessor for message headers
     */
    @MessageMapping("/subscribe")
    public void handleSubscription(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Client {} subscribed to event updates", sessionId);
        
        // Send welcome message to the newly subscribed client
        webSocketService.sendConnectionEstablished(sessionId);
    }

    /**
     * Handle client unsubscription
     * @param headerAccessor accessor for message headers
     */
    @MessageMapping("/unsubscribe")
    public void handleUnsubscription(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Client {} unsubscribed from event updates", sessionId);
    }
}
