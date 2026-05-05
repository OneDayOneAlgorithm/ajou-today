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

    // 핵심: 이번 턴에 제출된 행동 저장
    private Map<Integer, Integer> pendingActions = new HashMap<>();

    public GameState() {}

    public GameState(String gameId) {
        this.gameId = gameId;
        this.turn = 1;
        this.player1 = new PlayerState("Player 1");
        this.player2 = new PlayerState("Player 2");
        this.status = "WAITING_ACTION";
    }

    public String getGameId() { return gameId; }
    public int getTurn() { return turn; }
    public PlayerState getPlayer1() { return player1; }
    public PlayerState getPlayer2() { return player2; }
    public String getStatus() { return status; }
    public String getWinner() { return winner; }
    public Map<Integer, Integer> getPendingActions() { return pendingActions; }

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