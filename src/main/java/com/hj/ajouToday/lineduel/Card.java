package com.hj.ajouToday.lineduel;

public record Card(
        int id,
        String name,
        int cost,
        int attack,
        int hp,
        String description
) {
}