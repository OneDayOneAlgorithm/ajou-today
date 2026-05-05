package com.hj.ajouToday.lineduel;

public class PlayCardRequest {
    private int playerNumber;
    private int cardId;

    public int getPlayerNumber() {
        return playerNumber;
    }

    public int getCardId() {
        return cardId;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }
}