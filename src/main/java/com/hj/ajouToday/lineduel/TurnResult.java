package com.hj.ajouToday.lineduel;

import java.util.List;

public class TurnResult {
    private GameState state;
    private List<String> logs;
    private boolean resolved;

    public TurnResult(GameState state, List<String> logs, boolean resolved) {
        this.state = state;
        this.logs = logs;
        this.resolved = resolved;
    }

    public GameState getState() {
        return state;
    }

    public List<String> getLogs() {
        return logs;
    }

    public boolean isResolved() {
        return resolved;
    }
}