package ru.practicum.ewm.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String status;
    private String reason;
    private String message;
    private String timestamp;

    public ErrorResponse(String status, String reason, String message) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = String.valueOf(LocalDateTime.now());
    }
}
