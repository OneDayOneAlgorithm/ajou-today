package com.hj.ajouToday.lineduel;

import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    public static final int MAX_FIELD_SIZE = 5;

    private String name;
    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;

    private List<Integer> hand = new ArrayList<>();
    private List<UnitState> field = new ArrayList<>();

    public PlayerState() {}

    public PlayerState(String name) {
        this.name = name;
        this.maxHp = 20;
        this.hp = this.maxHp;
        this.maxMana = 5;
        this.mana = 5;
        this.hand.addAll(List.of(1, 2, 3, 4));
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMana() { return mana; }
    public List<Integer> getHand() { return hand; }
    public List<UnitState> getField() { return field; }

    public void reduceHp(int amount) {
        this.hp -= amount;
    }

    public int healHp(int amount) {
        int beforeHp = this.hp;

        this.hp = Math.min(this.maxHp, this.hp + amount);

        return this.hp - beforeHp;
    }

    public void useMana(int cost) {
        this.mana -= cost;
    }

    public void increaseMana() {
        this.maxMana = Math.min(10, this.maxMana + 1);
        this.mana = this.maxMana;
    }

    public void removeCardFromHand(int cardId) {
        this.hand.remove(Integer.valueOf(cardId));
    }

    public void drawCard(int cardId) {
        this.hand.add(cardId);
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public boolean isFieldFull() {
        return this.field.size() >= MAX_FIELD_SIZE;
    }

    public int getMaxFieldSize() {
        return MAX_FIELD_SIZE;
    }
}