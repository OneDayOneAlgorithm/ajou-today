package com.hj.ajouToday.survival;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int score;
    private LocalDateTime playedAt = LocalDateTime.now();

    public Score() {}

    public Long getId() { return id; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public LocalDateTime getPlayedAt() { return playedAt; }
}