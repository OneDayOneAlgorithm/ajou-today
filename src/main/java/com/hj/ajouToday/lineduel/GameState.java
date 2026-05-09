package com.hj.ajouToday.lineduel;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private String gameId;
    private int turn;
    private PlayerState player1;
    private PlayerState player2;
    private String status;
    private String winner;

    private Map<Integer, Integer> pendingActions = new HashMap<>();

    // 추가: playerNumber -> occupied 여부
    private Map<Integer, Boolean> players = new HashMap<>();

    public GameState() {}

    public GameState(String gameId) {
        this.gameId = gameId;
        this.turn = 1;
        this.player1 = new PlayerState("Player 1");
        this.player2 = new PlayerState("Player 2");
        this.status = "WAITING_PLAYER";

        this.players.put(1, false);
        this.players.put(2, false);
    }

    public String getGameId() { return gameId; }
    public int getTurn() { return turn; }
    public PlayerState getPlayer1() { return player1; }
    public PlayerState getPlayer2() { return player2; }
    public String getStatus() { return status; }
    public String getWinner() { return winner; }
    public Map<Integer, Integer> getPendingActions() { return pendingActions; }
    public Map<Integer, Boolean> getPlayers() { return players; }

    public int joinPlayer() {
        if (!players.get(1)) {
            players.put(1, true);
            return 1;
        }

        if (!players.get(2)) {
            players.put(2, true);
            this.status = "WAITING_ACTION";
            return 2;
        }

        throw new IllegalStateException("이미 방이 가득 찼습니다.");
    }

    public boolean isPlayerJoined(int playerNumber) {
        return Boolean.TRUE.equals(players.get(playerNumber));
    }

    public void submitAction(int playerNumber, int cardId) {
        this.pendingActions.put(playerNumber, cardId);
    }

    public boolean bothSubmitted() {
        return pendingActions.containsKey(1) && pendingActions.containsKey(2);
    }

    public void clearActions() {
        this.pendingActions.clear();
    }

    public void nextTurn() {
        this.turn++;
        this.status = "WAITING_ACTION";
    }

    public void finish(String winner) {
        this.status = "FINISHED";
        this.winner = winner;
    }
}