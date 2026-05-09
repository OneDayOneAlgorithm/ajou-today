package com.hj.ajouToday.lineduel;

import java.util.List;

public class TurnViewResult {
    private LineDuelViewState state;
    private List<String> logs;
    private boolean resolved;

    public TurnViewResult(LineDuelViewState state, List<String> logs, boolean resolved) {
        this.state = state;
        this.logs = logs;
        this.resolved = resolved;
    }

    public LineDuelViewState getState() {
        return state;
    }

    public List<String> getLogs() {
        return logs;
    }

    public boolean isResolved() {
        return resolved;
    }
}