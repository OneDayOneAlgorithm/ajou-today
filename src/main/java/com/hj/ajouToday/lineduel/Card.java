package com.hj.ajouToday.lineduel;

public record Card(
        int id,
        String name,
        CardType type,
        SpellEffectType spellEffectType,
        int cost,
        int attack,
        int hp,
        String description
) {
}