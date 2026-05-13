package com.hj.ajouToday.lineduel;

public class DrawResult {
    private final Integer cardId;
    private final boolean deckEmpty;
    private final boolean burned;

    private DrawResult(Integer cardId, boolean deckEmpty, boolean burned) {
        this.cardId = cardId;
        this.deckEmpty = deckEmpty;
        this.burned = burned;
    }

    public static DrawResult drawn(Integer cardId) {
        return new DrawResult(cardId, false, false);
    }

    public static DrawResult deckEmpty() {
        return new DrawResult(null, true, false);
    }

    public static DrawResult burned(Integer cardId) {
        return new DrawResult(cardId, false, true);
    }

    public Integer getCardId() {
        return cardId;
    }

    public boolean isDeckEmpty() {
        return deckEmpty;
    }

    public boolean isBurned() {
        return burned;
    }

    public boolean isDrawn() {
        return cardId != null && !burned;
    }
}