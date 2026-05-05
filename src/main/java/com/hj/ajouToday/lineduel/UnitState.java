package com.hj.ajouToday.lineduel;

public class UnitState {
    private String name;
    private int attack;
    private int hp;

    public UnitState() {}

    public UnitState(String name, int attack, int hp) {
        this.name = name;
        this.attack = attack;
        this.hp = hp;
    }

    public String getName() { return name; }
    public int getAttack() { return attack; }
    public int getHp() { return hp; }

    public void damage(int amount) {
        this.hp -= amount;
    }

    public boolean isDead() {
        return hp <= 0;
    }
}