package com.eventpulse.service;

import com.eventpulse.dto.EventWebSocketMessage;
import com.eventpulse.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast a new event to all connected clients
     * @param event the newly created event
     */
    public void broadcastNewEvent(Event event) {
        try {
            EventWebSocketMessage message = EventWebSocketMessage.newEventCreated(event);
            messagingTemplate.convertAndSend("/topic/events", message);
            log.info("Broadcasted new event to WebSocket clients: eventId={}, type={}, source={}", 
                    event.getId(), event.getType(), event.getSource());
        } catch (Exception e) {
            log.error("Failed to broadcast new event: {}", e.getMessage(), e);
            // Don't throw exception to avoid breaking the main event creation flow
        }
    }

    /**
     * Broadcast event deletion to all connected clients
     * @param eventId the ID of the deleted event
     */
    public void broadcastEventDeleted(UUID eventId) {
        try {
            EventWebSocketMessage message = EventWebSocketMessage.eventDeleted(eventId);
            messagingTemplate.convertAndSend("/topic/events", message);
            log.info("Broadcasted event deletion to WebSocket clients: eventId={}", eventId);
        } catch (Exception e) {
            log.error("Failed to broadcast event deletion: {}", e.getMessage(), e);
            // Don't throw exception to avoid breaking the main event deletion flow
        }
    }

    /**
     * Send a message to a specific user (if user-specific messaging is needed in the future)
     * @param username the username to send the message to
     * @param message the message to send
     */
    public void sendToUser(String username, EventWebSocketMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/events", message);
            log.debug("Sent message to user: {}", username);
        } catch (Exception e) {
            log.error("Failed to send message to user {}: {}", username, e.getMessage(), e);
        }
    }

    /**
     * Send a connection established message to a specific user
     * @param username the username (session ID)
     */
    public void sendConnectionEstablished(String username) {
        try {
            EventWebSocketMessage message = EventWebSocketMessage.connectionEstablished();
            messagingTemplate.convertAndSendToUser(username, "/queue/events", message);
            log.debug("Sent connection established message to user: {}", username);
        } catch (Exception e) {
            log.error("Failed to send connection established message to user {}: {}", username, e.getMessage(), e);
        }
    }
}
