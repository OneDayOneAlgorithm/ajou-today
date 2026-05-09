package com.hj.ajouToday.lineduel;

public class RoomJoinViewResult {
    private LineDuelViewState state;
    private int playerNumber;

    public RoomJoinViewResult(LineDuelViewState state, int playerNumber) {
        this.state = state;
        this.playerNumber = playerNumber;
    }

    public LineDuelViewState getState() {
        return state;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }
}