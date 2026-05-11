package com.hj.ajouToday.lineduel;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

@Service
public class LineDuelService {

    private static final long ROOM_EXPIRE_MILLIS = 1000L * 60 * 60;

    private final Map<String, GameState> games = new ConcurrentHashMap<>();
    private final CardCatalog cardCatalog;
    private final LineDuelMatchRepository matchRepository;

    public LineDuelService(
            CardCatalog cardCatalog,
            LineDuelMatchRepository matchRepository
    ) {
        this.cardCatalog = cardCatalog;
        this.matchRepository = matchRepository;
    }

    public List<Card> getCards() {
        return cardCatalog.getAllCards();
    }

    public RoomJoinResult startGame() {
        String gameId = generateRoomCode();

        GameState state = new GameState(gameId);

        setupInitialDecks(state);

        int playerNumber = state.joinPlayer();

        state.touch();
        state.addLog("방이 생성되었습니다.");
        state.addLog("Player 1이 입장했습니다.");
        state.addLog("Player 2를 기다리는 중입니다.");

        games.put(gameId, state);

        LineDuelMatch match = new LineDuelMatch(gameId);
        matchRepository.save(match);

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

        state.touch();
        state.addLog("Player " + playerNumber + "이 입장했습니다.");

        if ("WAITING_ACTION".equals(state.getStatus())) {
            state.addLog("양쪽 플레이어가 모두 입장했습니다. 게임을 시작할 수 있습니다.");
        }

        return new RoomJoinResult(state, playerNumber);
    }

    public RoomJoinResult reconnectGame(String gameId, int playerNumber) {
        GameState state = getGame(gameId);

        if (playerNumber != 1 && playerNumber != 2) {
            throw new IllegalArgumentException("잘못된 플레이어 번호입니다.");
        }

        if (!state.isPlayerJoined(playerNumber)) {
            throw new IllegalStateException("해당 플레이어는 이 방에 입장한 기록이 없습니다.");
        }

        state.touch();
        state.addLog("Player " + playerNumber + "이(가) 재접속했습니다.");

        return new RoomJoinResult(state, playerNumber);
    }

    public GameState getGame(String gameId) {
        GameState state = games.get(gameId);

        if (state == null) {
            throw new IllegalArgumentException("존재하지 않는 게임입니다.");
        }

        if (isExpired(state)) {
            games.remove(gameId);
            throw new IllegalStateException("만료된 게임방입니다.");
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

        Card card = cardCatalog.getCard(cardId);

        if (card == null) {
            throw new IllegalArgumentException("존재하지 않는 카드입니다.");
        }

        if (!me.getHand().contains(cardId)) {
            throw new IllegalArgumentException("손패에 없는 카드입니다.");
        }

        if (me.getMana() < card.cost()) {
            throw new IllegalArgumentException("마나가 부족합니다.");
        }

        if (card.type() == CardType.UNIT && me.isFieldFull()) {
            throw new IllegalStateException("필드가 가득 차서 유닛을 소환할 수 없습니다.");
        }

        if (state.getPendingActions().containsKey(playerNumber)) {
            throw new IllegalStateException("이미 이번 턴 행동을 제출했습니다.");
        }

        state.submitAction(playerNumber, cardId);
        state.touch();

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

    public TurnResult surrender(String gameId, int playerNumber) {
        GameState state = getGame(gameId);

        if ("FINISHED".equals(state.getStatus())) {
            throw new IllegalStateException("이미 종료된 게임입니다.");
        }

        if (playerNumber != 1 && playerNumber != 2) {
            throw new IllegalArgumentException("잘못된 플레이어 번호입니다.");
        }

        if (!state.isPlayerJoined(playerNumber)) {
            throw new IllegalStateException("입장하지 않은 플레이어입니다.");
        }

        int winnerNumber = playerNumber == 1 ? 2 : 1;

        if (!state.isPlayerJoined(winnerNumber)) {
            throw new IllegalStateException("상대 플레이어가 아직 입장하지 않았습니다.");
        }

        String loserName = "Player " + playerNumber;
        String winnerName = "Player " + winnerNumber;

        state.clearActions();
        state.finish(winnerName);
        state.touch();

        List<String> newLogs = new ArrayList<>();
        newLogs.add(loserName + "이(가) 항복했습니다.");
        newLogs.add(winnerName + "이(가) 승리했습니다.");

        saveMatchResult(
                state,
                winnerName,
                loserName,
                "SURRENDER"
        );

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

        if (card.type() == CardType.UNIT) {
            if (me.isFieldFull()) {
                logs.add(me.getName() + "의 필드가 가득 차서 "
                        + card.name() + "을(를) 소환하지 못했습니다.");
                return;
            }

            UnitState unit = new UnitState(card.name(), card.attack(), card.hp());
            me.getField().add(unit);

            logs.add(me.getName() + "이(가) " + card.name() + "을(를) 소환했습니다.");
            return;
        }

        if (card.type() == CardType.SPELL) {
            applySpell(state, me, enemy, card, logs);
            return;
        }

        throw new IllegalStateException("지원하지 않는 카드 타입입니다.");
    }

    private void applySpell(
            GameState state,
            PlayerState me,
            PlayerState enemy,
            Card card,
            List<String> logs
    ) {
        if (card.spellEffectType() == SpellEffectType.DAMAGE_HERO) {
            enemy.reduceHp(card.attack());

            logs.add(me.getName() + "이(가) " + card.name() + "으로 상대 영웅에게 "
                    + card.attack() + " 피해를 줬습니다.");
            return;
        }

        if (card.spellEffectType() == SpellEffectType.HEAL_SELF) {
            int healedAmount = me.healHp(card.attack());

            if (healedAmount <= 0) {
                logs.add(me.getName() + "이(가) " + card.name() + "을(를) 사용했지만 HP가 이미 최대입니다.");
            } else {
                logs.add(me.getName() + "이(가) " + card.name() + "으로 HP를 "
                        + healedAmount + " 회복했습니다.");
            }

            return;
        }

        if (card.spellEffectType() == SpellEffectType.DRAW_CARD) {
            int drawCount = card.attack();

            logs.add(me.getName() + "이(가) " + card.name()
                    + " 효과로 카드를 " + drawCount + "장 뽑으려 합니다.");

            for (int i = 0; i < drawCount; i++) {
                drawCardOrTakeFatigue(me, logs);
            }

            return;
        }

        throw new IllegalStateException("지원하지 않는 주문 효과입니다.");
    }

    private void resolveCombat(GameState state, List<String> logs) {
        PlayerState p1 = state.getPlayer1();
        PlayerState p2 = state.getPlayer2();

        List<UnitState> p1Field = p1.getField();
        List<UnitState> p2Field = p2.getField();

        int maxSize = Math.max(p1Field.size(), p2Field.size());

        if (maxSize == 0) {
            logs.add("전투할 유닛이 없습니다.");
            return;
        }

        for (int i = 0; i < maxSize; i++) {
            UnitState p1Unit = i < p1Field.size() ? p1Field.get(i) : null;
            UnitState p2Unit = i < p2Field.size() ? p2Field.get(i) : null;

            if (p1Unit != null && p2Unit != null) {
                fightUnits(p1Unit, p2Unit, logs);
            } else if (p1Unit != null) {
                attackHero(p1, p2, p1Unit, logs);
            } else if (p2Unit != null) {
                attackHero(p2, p1, p2Unit, logs);
            }
        }

        p1Field.removeIf(UnitState::isDead);
        p2Field.removeIf(UnitState::isDead);
    }

    private void fightUnits(UnitState p1Unit, UnitState p2Unit, List<String> logs) {
        int p1Attack = p1Unit.getAttack();
        int p2Attack = p2Unit.getAttack();

        p1Unit.damage(p2Attack);
        p2Unit.damage(p1Attack);

        logs.add(p1Unit.getName() + "와(과) " + p2Unit.getName()
                + "이(가) 전투했습니다. "
                + "(" + p1Attack + " ↔ " + p2Attack + ")");

        if (p1Unit.isDead()) {
            logs.add(p1Unit.getName() + "이(가) 쓰러졌습니다.");
        }

        if (p2Unit.isDead()) {
            logs.add(p2Unit.getName() + "이(가) 쓰러졌습니다.");
        }
    }

    private void attackHero(
            PlayerState attacker,
            PlayerState defender,
            UnitState unit,
            List<String> logs
    ) {
        defender.reduceHp(unit.getAttack());

        logs.add(attacker.getName() + "의 " + unit.getName()
                + "이(가) " + defender.getName()
                + "에게 직접 공격하여 "
                + unit.getAttack() + " 피해를 줬습니다.");
    }

    private void drawAndMana(GameState state, List<String> logs) {
        state.getPlayer1().increaseMana();
        state.getPlayer2().increaseMana();

        drawCardOrTakeFatigue(state.getPlayer1(), logs);
        drawCardOrTakeFatigue(state.getPlayer2(), logs);
    }

    private void resolveSubmittedTurn(GameState state, List<String> logs) {
        int p1CardId = state.getPendingActions().get(1);
        int p2CardId = state.getPendingActions().get(2);

        Card p1Card = cardCatalog.getCard(p1CardId);
        Card p2Card = cardCatalog.getCard(p2CardId);

        applyCard(state, state.getPlayer1(), state.getPlayer2(), p1Card, logs);
        applyCard(state, state.getPlayer2(), state.getPlayer1(), p2Card, logs);

        resolveCombat(state, logs);

        drawAndMana(state, logs);

        state.clearActions();

        if (!checkAndFinishGame(state, logs, "HP_ZERO")) {
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
                player.getMaxHp(),
                player.getMana(),
                player.getMaxMana(),
                player.getFatigueDamage(),
                visibleHand,
                player.getHand().size(),
                new ArrayList<>(player.getField()),
                player.getMaxFieldSize(),
                player.getDeckCount(),
                player.getMaxDeckSize(),
                handVisible
        );
    }

    private boolean isExpired(GameState state) {
        long now = System.currentTimeMillis();
        return now - state.getLastActiveAt() > ROOM_EXPIRE_MILLIS;
    }

    public int cleanupExpiredRooms() {
        int before = games.size();

        games.entrySet().removeIf(entry -> isExpired(entry.getValue()));

        int after = games.size();

        return before - after;
    }

    public int getActiveRoomCount() {
        return games.size();
    }

    private void saveMatchResult(GameState state, String winner, String loser, String endReason) {
        LineDuelMatch match = matchRepository.findByGameId(state.getGameId())
                .orElseThrow(() -> new IllegalStateException("매치 기록을 찾을 수 없습니다."));

        if ("FINISHED".equals(match.getStatus())) {
            return;
        }

        match.finish(
                winner,
                loser,
                endReason,
                state.getTurn()
        );

        matchRepository.save(match);
    }

    public List<LineDuelMatch> getMatches() {
        return matchRepository.findAll();
    }

    public LineDuelMatch getMatch(String gameId) {
        return matchRepository.findByGameId(gameId)
                .orElseThrow(() -> new IllegalArgumentException("매치 기록을 찾을 수 없습니다."));
    }

    private void setupInitialDecks(GameState state) {
        setupPlayerDeck(state.getPlayer1());
        setupPlayerDeck(state.getPlayer2());
    }

    private void setupPlayerDeck(PlayerState player) {
        List<Integer> deck = cardCatalog.createDefaultDeck();

        Collections.shuffle(deck);

        player.initializeDeck(deck);

        for (int i = 0; i < 4; i++) {
            player.drawFromDeck();
        }
    }

    private void drawCardOrTakeFatigue(
            PlayerState player,
            List<String> logs
    ) {
        Integer drawnCardId = player.drawFromDeck();

        if (drawnCardId != null) {
            Card card = cardCatalog.getCard(drawnCardId);
            logs.add(player.getName() + "이(가) 카드를 1장 뽑았습니다: " + card.name());
            return;
        }

        int damage = player.takeFatigueDamage();

        logs.add(player.getName() + "의 덱이 비어 있어 피로 피해 "
                + damage + "을(를) 받았습니다.");
    }

    private boolean checkAndFinishGame(
            GameState state,
            List<String> logs,
            String endReason
    ) {
        if (state.getPlayer1().getHp() <= 0 && state.getPlayer2().getHp() <= 0) {
            state.finish("DRAW");
            logs.add("게임이 무승부로 종료되었습니다.");

            saveMatchResult(
                    state,
                    "DRAW",
                    "DRAW",
                    endReason
            );

            return true;
        }

        if (state.getPlayer1().getHp() <= 0) {
            state.finish("Player 2");
            logs.add("Player 2이(가) 승리했습니다.");

            saveMatchResult(
                    state,
                    "Player 2",
                    "Player 1",
                    endReason
            );

            return true;
        }

        if (state.getPlayer2().getHp() <= 0) {
            state.finish("Player 1");
            logs.add("Player 1이(가) 승리했습니다.");

            saveMatchResult(
                    state,
                    "Player 1",
                    "Player 2",
                    endReason
            );

            return true;
        }

        return false;
    }
}