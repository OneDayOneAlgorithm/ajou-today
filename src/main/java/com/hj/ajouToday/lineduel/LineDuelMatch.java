package com.hj.ajouToday.lineduel;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "line_duel_match")
public class LineDuelMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String gameId;

    @Column(nullable = false)
    private String player1Name;

    @Column(nullable = false)
    private String player2Name;

    private String winner;

    private String loser;

    private String endReason;

    private int turnCount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    public LineDuelMatch() {
    }

    public LineDuelMatch(String gameId) {
        this.gameId = gameId;
        this.player1Name = "Player 1";
        this.player2Name = "Player 2";
        this.status = "IN_PROGRESS";
        this.turnCount = 1;
        this.startedAt = LocalDateTime.now();
    }

    public void finish(String winner, String loser, String endReason, int turnCount) {
        this.winner = winner;
        this.loser = loser;
        this.endReason = endReason;
        this.turnCount = turnCount;
        this.status = "FINISHED";
        this.endedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public String getWinner() {
        return winner;
    }

    public String getLoser() {
        return loser;
    }

    public String getEndReason() {
        return endReason;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }
}