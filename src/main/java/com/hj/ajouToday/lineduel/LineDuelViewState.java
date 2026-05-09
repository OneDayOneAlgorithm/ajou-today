package com.hj.ajouToday.lineduel;

import java.util.Map;

public class LineDuelViewState {
    private String gameId;
    private int turn;
    private PlayerViewState player1;
    private PlayerViewState player2;
    private String status;
    private String winner;
    private Map<Integer, Integer> pendingActions;
    private Map<Integer, Boolean> players;
    private int myPlayerNumber;

    public LineDuelViewState(
            String gameId,
            int turn,
            PlayerViewState player1,
            PlayerViewState player2,
            String status,
            String winner,
            Map<Integer, Integer> pendingActions,
            Map<Integer, Boolean> players,
            int myPlayerNumber
    ) {
        this.gameId = gameId;
        this.turn = turn;
        this.player1 = player1;
        this.player2 = player2;
        this.status = status;
        this.winner = winner;
        this.pendingActions = pendingActions;
        this.players = players;
        this.myPlayerNumber = myPlayerNumber;
    }

    public String getGameId() {
        return gameId;
    }

    public int getTurn() {
        return turn;
    }

    public PlayerViewState getPlayer1() {
        return player1;
    }

    public PlayerViewState getPlayer2() {
        return player2;
    }

    public String getStatus() {
        return status;
    }

    public String getWinner() {
        return winner;
    }

    public Map<Integer, Integer> getPendingActions() {
        return pendingActions;
    }

    public Map<Integer, Boolean> getPlayers() {
        return players;
    }

    public int getMyPlayerNumber() {
        return myPlayerNumber;
    }
}