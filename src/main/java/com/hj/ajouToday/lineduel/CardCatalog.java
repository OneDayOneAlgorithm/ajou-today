package com.hj.ajouToday.lineduel;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CardCatalog {

    private final Map<Integer, Card> cards = Map.of(
            1, new Card(1, "병사", 1, 2, 2, "기본 유닛"),
            2, new Card(2, "방패병", 2, 1, 5, "체력이 높다"),
            3, new Card(3, "광전사", 2, 4, 1, "공격력이 높다"),
            4, new Card(4, "화염구", 3, 20, 0, "상대 영웅에게 3 피해"),
            5, new Card(5, "기사", 4, 5, 4, "강력한 중급 유닛")
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

    public int getRandomCardId() {
        List<Integer> cardIds = new ArrayList<>(cards.keySet());
        int randomIndex = (int) (Math.random() * cardIds.size());
        return cardIds.get(randomIndex);
    }
}