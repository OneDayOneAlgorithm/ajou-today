package com.hj.ajouToday.lineduel;

import java.util.List;

public class PlayerViewState {
    private String name;
    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;
    private List<Integer> hand;
    private int handCount;
    private List<UnitState> field;
    private int maxFieldSize;
    private boolean handVisible;

    public PlayerViewState(
            String name,
            int hp,
            int maxHp,
            int mana,
            int maxMana,
            List<Integer> hand,
            int handCount,
            List<UnitState> field,
            int maxFieldSize,
            boolean handVisible
    ) {
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.mana = mana;
        this.maxMana = maxMana;
        this.hand = hand;
        this.handCount = handCount;
        this.field = field;
        this.maxFieldSize = maxFieldSize;
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

    public int getMaxFieldSize() {
        return maxFieldSize;
    }

    public List<UnitState> getField() {
        return field;
    }

    public boolean isHandVisible() {
        return handVisible;
    }
}