package com.hj.ajouToday.lineduel;

public class RoomJoinResult {
    private GameState state;
    private int playerNumber;

    public RoomJoinResult(GameState state, int playerNumber) {
        this.state = state;
        this.playerNumber = playerNumber;
    }

    public GameState getState() {
        return state;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }
}