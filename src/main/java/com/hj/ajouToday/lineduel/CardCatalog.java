package com.hj.ajouToday.lineduel;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CardCatalog {

    private final Map<Integer, Card> cards = Map.of(
            1, new Card(1, "병사", CardType.UNIT, SpellEffectType.NONE, 1, 2, 2, "기본 유닛"),
            2, new Card(2, "방패병", CardType.UNIT, SpellEffectType.NONE, 2, 1, 5, "체력이 높다"),
            3, new Card(3, "광전사", CardType.UNIT, SpellEffectType.NONE, 2, 4, 1, "공격력이 높다"),
            4, new Card(4, "화염구", CardType.SPELL, SpellEffectType.DAMAGE_HERO, 3, 3, 0, "상대 영웅에게 3 피해"),
            5, new Card(5, "치유", CardType.SPELL, SpellEffectType.HEAL_SELF, 2, 3, 0, "내 영웅의 HP를 3 회복"),
            6, new Card(6, "지식 탐구", CardType.SPELL, SpellEffectType.DRAW_CARD, 2, 2, 0, "카드를 2장 뽑는다")
    );

    public List<Card> getAllCards() {
        return new ArrayList<>(cards.values());
    }

    public Card getCard(int cardId) {
        return cards.get(cardId);
    }

    public boolean exists(int cardId) {
        return cards.containsKey(cardId);
    }

    public List<Integer> createDefaultDeck() {
        return new ArrayList<>(List.of(
                1, 1, 1,
                2, 2, 2,
                3, 3, 3,
                4, 4,
                5, 5,
                6, 6
        ));
    }
}