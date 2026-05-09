package com.hj.ajouToday.lineduel;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LineDuelService {

    private final Map<String, GameState> games = new HashMap<>();

    private final Map<Integer, Card> cards = Map.of(
            1, new Card(1, "병사", 1, 2, 2, "기본 유닛"),
            2, new Card(2, "방패병", 2, 1, 5, "체력이 높다"),
            3, new Card(3, "광전사", 2, 4, 1, "공격력이 높다"),
            4, new Card(4, "화염구", 3, 20, 0, "상대 영웅에게 20 피해")
    );

    public List<Card> getCards() {
        return new ArrayList<>(cards.values());
    }

    public RoomJoinResult startGame() {
        String gameId = generateRoomCode();

        GameState state = new GameState(gameId);

        int playerNumber = state.joinPlayer();

        state.addLog("방이 생성되었습니다.");
        state.addLog("Player 1이 입장했습니다.");
        state.addLog("Player 2를 기다리는 중입니다.");

        games.put(gameId, state);

        return new RoomJoinResult(state, playerNumber);
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();

        while (true) {
            StringBuilder code = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                int index = random.nextInt(chars.length());
                code.append(chars.charAt(index));
            }

            String roomCode = code.toString();

            if (!games.containsKey(roomCode)) {
                return roomCode;
            }
        }
    }

    public RoomJoinResult joinGame(String gameId) {
        GameState state = getGame(gameId);

        if ("FINISHED".equals(state.getStatus())) {
            throw new IllegalStateException("이미 종료된 게임입니다.");
        }

        int playerNumber = state.joinPlayer();

        state.addLog("Player " + playerNumber + "이 입장했습니다.");

        if ("WAITING_ACTION".equals(state.getStatus())) {
            state.addLog("양쪽 플레이어가 모두 입장했습니다. 게임을 시작할 수 있습니다.");
        }

        return new RoomJoinResult(state, playerNumber);
    }

    public GameState getGame(String gameId) {
        GameState state = games.get(gameId);
        if (state == null) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다.");
        }
        return state;
    }

    public TurnResult playCard(String gameId, PlayCardRequest request) {
        GameState state = getGame(gameId);

        if ("FINISHED".equals(state.getStatus())) {
            throw new IllegalStateException("이미 종료된 게임입니다.");
        }

        if ("WAITING_PLAYER".equals(state.getStatus())) {
            throw new IllegalStateException("상대 플레이어를 기다리는 중입니다.");
        }

        int playerNumber = request.getPlayerNumber();
        int cardId = request.getCardId();

        if (playerNumber != 1 && playerNumber != 2) {
            throw new IllegalArgumentException("잘못된 플레이어 번호입니다.");
        }

        if (!state.isPlayerJoined(playerNumber)) {
            throw new IllegalStateException("입장하지 않은 플레이어입니다.");
        }

        PlayerState me = playerNumber == 1 ? state.getPlayer1() : state.getPlayer2();

        Card card = cards.get(cardId);

        if (card == null) {
            throw new IllegalArgumentException("존재하지 않는 카드입니다.");
        }

        if (!me.getHand().contains(cardId)) {
            throw new IllegalArgumentException("손패에 없는 카드입니다.");
        }

        if (me.getMana() < card.cost()) {
            throw new IllegalArgumentException("마나가 부족합니다.");
        }

        if (state.getPendingActions().containsKey(playerNumber)) {
            throw new IllegalStateException("이미 이번 턴 행동을 제출했습니다.");
        }

        state.submitAction(playerNumber, cardId);

        List<String> newLogs = new ArrayList<>();
        newLogs.add("Turn " + state.getTurn() + " - " + me.getName() + "이(가) 행동을 제출했습니다.");

        if (!state.bothSubmitted()) {
            state.addLogs(newLogs);
            return new TurnResult(state, new ArrayList<>(state.getLogs()), false);
        }

        newLogs.add("Turn " + state.getTurn() + " - 양쪽 플레이어가 모두 행동을 제출했습니다.");

        resolveSubmittedTurn(state, newLogs);

        state.addLogs(newLogs);

        return new TurnResult(state, new ArrayList<>(state.getLogs()), true);
    }

    private void applyCard(
            GameState state,
            PlayerState me,
            PlayerState enemy,
            Card card,
            List<String> logs
    ) {
        me.useMana(card.cost());
        me.removeCardFromHand(card.id());

        if (card.hp() > 0) {
            UnitState unit = new UnitState(card.name(), card.attack(), card.hp());
            me.getField().add(unit);
            logs.add(me.getName() + "이(가) " + card.name() + "을(를) 소환했습니다.");
        } else {
            enemy.reduceHp(card.attack());
            logs.add(me.getName() + "이(가) " + card.name() + "으로 상대 영웅에게 "
                    + card.attack() + " 피해를 줬습니다.");
        }
    }

    private void resolveCombat(GameState state, List<String> logs) {
        PlayerState p1 = state.getPlayer1();
        PlayerState p2 = state.getPlayer2();

        if (!p1.getField().isEmpty() && !p2.getField().isEmpty()) {
            UnitState u1 = p1.getField().get(0);
            UnitState u2 = p2.getField().get(0);

            u1.damage(u2.getAttack());
            u2.damage(u1.getAttack());

            logs.add(u1.getName() + "와(과) " + u2.getName() + "이(가) 전투했습니다.");

            p1.getField().removeIf(UnitState::isDead);
            p2.getField().removeIf(UnitState::isDead);
        } else if (!p1.getField().isEmpty()) {
            UnitState u1 = p1.getField().get(0);
            p2.reduceHp(u1.getAttack());
            logs.add("Player 1의 " + u1.getName() + "이(가) Player 2에게 직접 공격했습니다.");
        } else if (!p2.getField().isEmpty()) {
            UnitState u2 = p2.getField().get(0);
            p1.reduceHp(u2.getAttack());
            logs.add("Player 2의 " + u2.getName() + "이(가) Player 1에게 직접 공격했습니다.");
        }
    }

    private void drawAndMana(GameState state) {
        Random random = new Random();

        state.getPlayer1().increaseMana();
        state.getPlayer2().increaseMana();

        state.getPlayer1().drawCard(random.nextInt(4) + 1);
        state.getPlayer2().drawCard(random.nextInt(4) + 1);
    }

    private void resolveSubmittedTurn(GameState state, List<String> logs) {
        int p1CardId = state.getPendingActions().get(1);
        int p2CardId = state.getPendingActions().get(2);

        Card p1Card = cards.get(p1CardId);
        Card p2Card = cards.get(p2CardId);

        applyCard(state, state.getPlayer1(), state.getPlayer2(), p1Card, logs);
        applyCard(state, state.getPlayer2(), state.getPlayer1(), p2Card, logs);

        resolveCombat(state, logs);

        drawAndMana(state);

        state.clearActions();

        if (state.getPlayer1().getHp() <= 0 && state.getPlayer2().getHp() <= 0) {
            state.finish("DRAW");
        } else if (state.getPlayer1().getHp() <= 0) {
            state.finish("Player 2");
        } else if (state.getPlayer2().getHp() <= 0) {
            state.finish("Player 1");
        } else {
            state.nextTurn();
        }
    }

    public LineDuelViewState toViewState(GameState state, int viewerPlayerNumber) {
        PlayerViewState player1View = toPlayerView(
                state.getPlayer1(),
                viewerPlayerNumber == 1
        );

        PlayerViewState player2View = toPlayerView(
                state.getPlayer2(),
                viewerPlayerNumber == 2
        );

        return new LineDuelViewState(
                state.getGameId(),
                state.getTurn(),
                player1View,
                player2View,
                state.getStatus(),
                state.getWinner(),
                state.getPendingActions(),
                state.getPlayers(),
                viewerPlayerNumber
        );
    }

    private PlayerViewState toPlayerView(PlayerState player, boolean handVisible) {
        List<Integer> visibleHand = handVisible
                ? new ArrayList<>(player.getHand())
                : List.of();

        return new PlayerViewState(
                player.getName(),
                player.getHp(),
                player.getMana(),
                player.getMaxMana(),
                visibleHand,
                player.getHand().size(),
                new ArrayList<>(player.getField()),
                handVisible
        );
    }
}