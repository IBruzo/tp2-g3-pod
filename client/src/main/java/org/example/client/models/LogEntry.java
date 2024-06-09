package org.example.client.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LogEntry {
    private final String timestamp;
    private final String message;

    @Override
    public String toString() {
        return timestamp + " - " + message;
    }
}
