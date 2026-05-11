package com.hj.ajouToday.lineduel;

import java.util.List;

public class PlayerViewState {
    private String name;
    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;
    private int fatigueDamage;
    private List<Integer> hand;
    private int handCount;
    private List<UnitState> field;
    private int maxFieldSize;
    private int deckCount;
    private int maxDeckSize;
    private boolean handVisible;

    public PlayerViewState(
            String name,
            int hp,
            int maxHp,
            int mana,
            int maxMana,
            int fatigueDamage,
            List<Integer> hand,
            int handCount,
            List<UnitState> field,
            int maxFieldSize,
            int deckCount,
            int maxDeckSize,
            boolean handVisible
    ) {
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.mana = mana;
        this.maxMana = maxMana;
        this.fatigueDamage = fatigueDamage;
        this.hand = hand;
        this.handCount = handCount;
        this.field = field;
        this.maxFieldSize = maxFieldSize;
        this.deckCount = deckCount;
        this.maxDeckSize = maxDeckSize;
        this.handVisible = handVisible;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public List<Integer> getHand() {
        return hand;
    }

    public int getHandCount() {
        return handCount;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getFatigueDamage() {
        return fatigueDamage;
    }

    public int getMaxFieldSize() {
        return maxFieldSize;
    }

    public List<UnitState> getField() {
        return field;
    }

    public int getDeckCount() {
        return deckCount;
    }

    public int getMaxDeckSize() {
        return maxDeckSize;
    }

    public boolean isHandVisible() {
        return handVisible;
    }
}