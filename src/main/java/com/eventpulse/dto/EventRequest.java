package com.eventpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Event source is required")
    @Size(max = 255, message = "Event source must not exceed 255 characters")
    private String source;

    @NotBlank(message = "Event type is required")
    @Size(max = 100, message = "Event type must not exceed 100 characters")
    private String type;

    @NotBlank(message = "Event message is required")
    @Size(max = 2000, message = "Event message must not exceed 2000 characters")
    private String message;
}
