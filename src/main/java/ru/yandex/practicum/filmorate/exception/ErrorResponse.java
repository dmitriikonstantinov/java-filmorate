package ru.yandex.practicum.filmorate.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String error;
    private String timestamp;

    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getError() {
        return error;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
