package com.hj.ajouToday.lineduel;

import java.util.List;

public class LineDuelErrorResult {
    private boolean error;
    private String message;
    private List<String> logs;

    public LineDuelErrorResult(String message) {
        this.error = true;
        this.message = message;
        this.logs = List.of(message);
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getLogs() {
        return logs;
    }
}