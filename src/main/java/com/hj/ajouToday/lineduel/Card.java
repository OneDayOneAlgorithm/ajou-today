package com.hj.ajouToday.lineduel;

public record Card(
        int id,
        String name,
        CardType type,
        int cost,
        int attack,
        int hp,
        String description
) {
}