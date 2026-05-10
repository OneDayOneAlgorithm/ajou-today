let gameId = null;
let cards = [];
let stompClient = null;
let currentState = null;
let myPlayerNumber = null;
let selectedCardId = null;
let selectedHandIndex = null;
let lastHistoryRefreshGameId = null;

const SAVED_GAME_ID_KEY = "lineDuelGameId";
const SAVED_PLAYER_NUMBER_KEY = "lineDuelPlayerNumber";

async function createRoom() {
    await loadCards();

    const response = await fetch("/api/lineduel/games", {
        method: "POST"
    });

    if (!response.ok) {
        alert("방 만들기에 실패했습니다.");
        return;
    }

    const result = await response.json();

    gameId = result.state.gameId;
    currentState = result.state;
    myPlayerNumber = result.playerNumber;
    selectedCardId = null;
    selectedHandIndex = null;
    lastHistoryRefreshGameId = null;

    saveCurrentGame();
    hideResultPanel();

    showRoomInfo();
    connectSocket(gameId);
    render(currentState, currentState.logs || []);
    updateReconnectPanel();
}

async function joinRoom() {
    await loadCards();

    const input = document.getElementById("joinGameId");
    const joinGameId = input.value.trim().toUpperCase();

    if (!joinGameId) {
        alert("gameId를 입력하세요.");
        return;
    }

    const response = await fetch("/api/lineduel/games/join", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            gameId: joinGameId
        })
    });

    if (!response.ok) {
        const text = await response.text();
        console.error(text);
        alert("방 입장에 실패했습니다.");
        return;
    }

    const result = await response.json();

    gameId = result.state.gameId;
    currentState = result.state;
    myPlayerNumber = result.playerNumber;
    selectedCardId = null;
    selectedHandIndex = null;
    lastHistoryRefreshGameId = null;

    saveCurrentGame();
    hideResultPanel();

    showRoomInfo();
    connectSocket(gameId);
    render(currentState, currentState.logs || []);
    updateReconnectPanel();
}

async function loadCards() {
    const response = await fetch("/api/lineduel/cards");
    cards = await response.json();
}

function showRoomInfo() {
    document.getElementById("roomInfo").classList.remove("hidden");
    document.getElementById("game").classList.remove("hidden");

    document.getElementById("gameIdText").innerText = gameId;
    document.getElementById("myPlayerText").innerText = `Player ${myPlayerNumber}`;
}

async function copyRoomCode() {
    if (!gameId) {
        alert("복사할 방 코드가 없습니다.");
        return;
    }

    try {
        await navigator.clipboard.writeText(gameId);
        showCopyMessage("방 코드가 복사되었습니다.");
    } catch (error) {
        console.error(error);

        // clipboard API가 막힌 환경을 위한 예비 방식
        fallbackCopyText(gameId);
    }
}

function fallbackCopyText(text) {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.style.position = "fixed";
    textarea.style.left = "-9999px";
    textarea.style.top = "-9999px";

    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();

    try {
        document.execCommand("copy");
        showCopyMessage("방 코드가 복사되었습니다.");
    } catch (error) {
        console.error(error);
        showCopyMessage("복사에 실패했습니다. 직접 방 코드를 선택해 복사하세요.");
    }

    document.body.removeChild(textarea);
}

function showCopyMessage(message) {
    const copyMessage = document.getElementById("copyMessage");

    if (!copyMessage) {
        return;
    }

    copyMessage.innerText = message;

    setTimeout(() => {
        copyMessage.innerText = "";
    }, 2000);
}

function selectCard(playerNumber, cardId, handIndex) {
    if (playerNumber !== myPlayerNumber) {
        alert("내 손패만 조작할 수 있습니다.");
        return;
    }

    if (!currentState) {
        return;
    }

    if (currentState.status !== "WAITING_ACTION") {
        alert("아직 카드를 제출할 수 없습니다.");
        return;
    }

    const alreadySubmitted =
        currentState.pendingActions && currentState.pendingActions[myPlayerNumber];

    if (alreadySubmitted) {
        alert("이미 이번 턴 행동을 제출했습니다.");
        return;
    }

    const myPlayer = myPlayerNumber === 1
        ? currentState.player1
        : currentState.player2;

    const card = cards.find(c => c.id === cardId);

    if (!card) {
        alert("존재하지 않는 카드입니다.");
        return;
    }

    if (myPlayer.mana < card.cost) {
        alert("마나가 부족해서 선택할 수 없습니다.");
        return;
    }

    selectedCardId = cardId;
    selectedHandIndex = handIndex;

    render(currentState, getCurrentLogTexts());
    updateSelectedCardPanel();
}

function submitSelectedCard() {
    if (currentState && currentState.status === "FINISHED") {
        alert("이미 종료된 게임입니다.");
        return;
    }

    if (!selectedCardId) {
        alert("제출할 카드를 먼저 선택하세요.");
        return;
    }

    if (!currentState) {
        alert("게임 상태를 불러오지 못했습니다.");
        return;
    }

    const myPlayer = myPlayerNumber === 1
        ? currentState.player1
        : currentState.player2;

    const card = cards.find(c => c.id === selectedCardId);

    if (!card) {
        alert("존재하지 않는 카드입니다.");
        selectedCardId = null;
        selectedHandIndex = null;
        updateSelectedCardPanel();
        return;
    }

    if (myPlayer.mana < card.cost) {
        alert("마나가 부족해서 제출할 수 없습니다.");
        selectedCardId = null;
        selectedHandIndex = null;
        render(currentState, getCurrentLogTexts());
        updateSelectedCardPanel();
        return;
    }

    if (!stompClient || !stompClient.connected) {
        alert("WebSocket 연결이 아직 완료되지 않았습니다.");
        return;
    }

    stompClient.send("/app/lineduel/play", {}, JSON.stringify({
        gameId: gameId,
        playerNumber: myPlayerNumber,
        cardId: selectedCardId
    }));

    selectedCardId = null;
    selectedHandIndex = null;
    updateSelectedCardPanel();
}

function updateSelectedCardPanel() {
    const text = document.getElementById("selectedCardText");
    const button = document.getElementById("submitCardButton");

    if (!text || !button) {
        return;
    }

    if (!currentState || currentState.status !== "WAITING_ACTION") {
        text.innerText = "선택한 카드: 없음";
        button.disabled = true;
        return;
    }

    const alreadySubmitted =
        currentState.pendingActions && currentState.pendingActions[myPlayerNumber];

    if (alreadySubmitted) {
        text.innerText = "이번 턴 행동 제출 완료";
        button.disabled = true;
        return;
    }

    if (!selectedCardId) {
        text.innerText = "선택한 카드: 없음";
        button.disabled = true;
        return;
    }

    const card = cards.find(c => c.id === selectedCardId);

    if (!card) {
        text.innerText = "선택한 카드: 없음";
        button.disabled = true;
        return;
    }

    const myPlayer = myPlayerNumber === 1
        ? currentState.player1
        : currentState.player2;

    if (myPlayer.mana < card.cost) {
        text.innerText = `선택한 카드: ${card.name} / ${getCardTypeText(card.type)} / 마나 부족`;
        button.disabled = true;
        return;
    }

    text.innerText = `선택한 카드: ${card.name} / ${getCardTypeText(card.type)} / Cost ${card.cost}`;
    button.disabled = false;
}

function render(state, logs) {
    logs = logs || [];

    currentState = state;

    document.getElementById("turn").innerText = `Turn ${state.turn}`;
    document.getElementById("winner").innerText =
        state.status === "FINISHED" ? `승자: ${state.winner}` : "";

    const waitingText = document.getElementById("waitingText");

    if (state.status === "WAITING_PLAYER") {
        waitingText.innerText = "상대 플레이어를 기다리는 중...";
    } else {
        waitingText.innerText = "";
    }

    renderPerspectiveBoard(state);

    renderLogs(logs);
    renderPendingStatus(state);
    updateWaitingPanel(state);
    updateSelectedCardPanel();
    updateResultPanel(state);
}

function renderPerspectiveBoard(state) {
    const myNumber = myPlayerNumber;
    const enemyNumber = myNumber === 1 ? 2 : 1;

    const myPlayer = myNumber === 1 ? state.player1 : state.player2;
    const enemyPlayer = enemyNumber === 1 ? state.player1 : state.player2;

    document.getElementById("meTitle").innerText = `나 - Player ${myNumber}`;
    document.getElementById("enemyTitle").innerText = `상대 - Player ${enemyNumber}`;

    document.getElementById("meHandTitle").innerText = `내 손패`;
    document.getElementById("enemyHandTitle").innerText = `상대 손패`;

    renderPlayerToElements("me", myPlayer);
    renderPlayerToElements("enemy", enemyPlayer);

    renderHand("meHand", myNumber, myPlayer, state);
    renderHand("enemyHand", enemyNumber, enemyPlayer, state);
}

function renderPendingStatus(state) {
    const logs = document.getElementById("logs");

    if (!state.pendingActions) return;
    if (state.status === "FINISHED") return;

    const p1Ready = state.pendingActions[1] ? "제출 완료" : "대기 중";
    const p2Ready = state.pendingActions[2] ? "제출 완료" : "대기 중";

    const li = document.createElement("li");
    li.innerText = `Player 1: ${p1Ready} / Player 2: ${p2Ready}`;
    li.classList.add("system-log");
    logs.appendChild(li);

    scrollLogsToBottom();
}

function renderPlayerToElements(prefix, player) {
    document.getElementById(`${prefix}Hp`).innerText = player.hp;
    document.getElementById(`${prefix}Mana`).innerText = player.mana;

    const field = document.getElementById(`${prefix}Field`);
    field.innerHTML = "";

    player.field.forEach(unit => {
        const div = document.createElement("div");
        div.className = "unit";
        div.innerHTML = `
            <strong>${unit.name}</strong>
            <p>ATK ${unit.attack} / HP ${unit.hp}</p>
        `;
        field.appendChild(div);
    });
}

function renderHand(elementId, playerNumber, player, state) {
    const handDiv = document.getElementById(elementId);
    handDiv.innerHTML = "";

    const isMyHand = playerNumber === myPlayerNumber;

    const alreadySubmitted =
        state.pendingActions && state.pendingActions[playerNumber];

    if (!isMyHand) {
        for (let i = 0; i < player.handCount; i++) {
            const div = document.createElement("div");
            div.className = "card hidden-card";
            div.innerHTML = `
                <strong>상대 카드</strong>
                <p>?</p>
            `;
            div.style.cursor = "not-allowed";
            handDiv.appendChild(div);
        }
        return;
    }

    player.hand.forEach((cardId, index) => {
        const card = cards.find(c => c.id === cardId);
        if (!card) return;

        const canAfford = player.mana >= card.cost;
        const canSelect =
            state.status === "WAITING_ACTION" &&
            !alreadySubmitted &&
            canAfford;

        const div = document.createElement("div");
        div.className = "card";

        if (selectedCardId === card.id && selectedHandIndex === index) {
            div.classList.add("selected-card");
        }

        if (!canAfford) {
            div.classList.add("disabled-card");
        }

        div.innerHTML = `
            <strong>${card.name}</strong>
            <p class="card-type">${getCardTypeText(card.type)}</p>
            ${
                    card.type === "SPELL"
                        ? `<p class="spell-effect">${getSpellEffectText(card.spellEffectType)}</p>`
                        : ""
                }
            <p>Cost ${card.cost}</p>
            <p>${getCardStatText(card)}</p>
            <small>${card.description}</small>
            ${!canAfford ? `<p class="card-warning">마나 부족</p>` : ""}
        `;

        if (canSelect) {
            div.onclick = () => selectCard(playerNumber, card.id, index);
        } else {
            div.style.cursor = "not-allowed";
        }

        if (state.status !== "WAITING_ACTION" || alreadySubmitted) {
            div.style.opacity = "0.5";
        }

        handDiv.appendChild(div);
    });
}

function renderLogs(logs) {
    const ul = document.getElementById("logs");
    ul.innerHTML = "";

    logs.forEach(log => {
        const li = document.createElement("li");
        li.innerText = log;

        if (log.includes("피해")) {
            li.classList.add("damage-log");
        } else if (log.includes("소환")) {
            li.classList.add("summon-log");
        } else if (log.includes("입장") || log.includes("생성")) {
            li.classList.add("system-log");
        } else if (log.includes("승리") || log.includes("종료")) {
            li.classList.add("finish-log");
        }

        ul.appendChild(li);
    });

    scrollLogsToBottom();
}

function getCurrentLogTexts() {
    const ul = document.getElementById("logs");
    if (!ul) return [];

    return Array.from(ul.querySelectorAll("li"))
        .map(li => li.innerText)
        .filter(text => !text.startsWith("Player 1:"));
}

function connectSocket(gameId) {
    const socket = new SockJS("/ws-lineduel");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("WebSocket 연결 성공");

        stompClient.subscribe(
            `/topic/lineduel/${gameId}/player/${myPlayerNumber}`,
            function (message) {
                const result = JSON.parse(message.body);

                if (result.error) {
                    handleServerError(result);
                    return;
                }

                selectedCardId = null;
                selectedHandIndex = null;
                currentState = result.state;

                render(result.state, result.logs);
            }
        );
    });
}

function handleServerError(result) {
    const errorMessage = result.message || "알 수 없는 오류가 발생했습니다.";

    alert(errorMessage);

    const currentLogs = getCurrentLogTexts();
    currentLogs.push(`오류: ${errorMessage}`);

    if (currentState) {
        render(currentState, currentLogs);
    } else {
        renderLogs(currentLogs);
    }

    selectedCardId = null;
    selectedHandIndex = null;
    updateSelectedCardPanel();
}

function goToLobby() {
    gameId = null;
    currentState = null;
    myPlayerNumber = null;
    selectedCardId = null;
    selectedHandIndex = null;

    if (stompClient && stompClient.connected) {
        stompClient.disconnect(function () {
            console.log("WebSocket 연결 해제");
        });
    }

    stompClient = null;

    document.getElementById("roomInfo").classList.add("hidden");
    document.getElementById("game").classList.add("hidden");

    document.getElementById("gameIdText").innerText = "";
    document.getElementById("myPlayerText").innerText = "";
    document.getElementById("waitingText").innerText = "";
    document.getElementById("copyMessage").innerText = "";

    document.getElementById("joinGameId").value = "";

    hideResultPanel();
    renderLogs([]);
    updateSelectedCardPanel();

    const waitingPanel = document.getElementById("waitingPanel");
    const actionPanel = document.getElementById("actionPanel");
    const surrenderButton = document.getElementById("surrenderButton");

    if (waitingPanel) {
        waitingPanel.classList.add("hidden");
    }

    if (actionPanel) {
        actionPanel.classList.add("hidden");
    }

    if (surrenderButton) {
        surrenderButton.classList.add("hidden");
    }

    updateReconnectPanel();
}

function updateResultPanel(state) {
    const panel = document.getElementById("resultPanel");
    const title = document.getElementById("resultTitle");
    const message = document.getElementById("resultMessage");

    if (!panel || !title || !message) {
        return;
    }

    if (!state || state.status !== "FINISHED") {
        hideResultPanel();
        return;
    }

    panel.classList.remove("hidden");

    if (lastHistoryRefreshGameId !== state.gameId) {
        lastHistoryRefreshGameId = state.gameId;
        loadMatchHistory();
    }

    if (state.winner === "DRAW") {
        title.innerText = "무승부";
        message.innerText = "양쪽 플레이어가 동시에 쓰러졌습니다.";
        return;
    }

    title.innerText = `${state.winner} 승리`;

    if (state.winner === `Player ${myPlayerNumber}`) {
        message.innerText = "승리했습니다!";
    } else {
        message.innerText = "패배했습니다.";
    }
}

function hideResultPanel() {
    const panel = document.getElementById("resultPanel");

    if (!panel) {
        return;
    }

    panel.classList.add("hidden");
}

function scrollLogsToBottom() {
    const ul = document.getElementById("logs");

    if (!ul) {
        return;
    }

    ul.scrollTop = ul.scrollHeight;
}

function updateWaitingPanel(state) {
    const waitingPanel = document.getElementById("waitingPanel");
    const actionPanel = document.getElementById("actionPanel");
    const waitingRoomCode = document.getElementById("waitingRoomCode");
    const surrenderButton = document.getElementById("surrenderButton");

    if (!waitingPanel || !actionPanel || !waitingRoomCode) {
        return;
    }

    if (!state) {
        waitingPanel.classList.add("hidden");
        actionPanel.classList.add("hidden");

        if (surrenderButton) {
            surrenderButton.classList.add("hidden");
        }

        return;
    }

    if (state.status === "WAITING_PLAYER") {
        waitingPanel.classList.remove("hidden");
        actionPanel.classList.add("hidden");
        waitingRoomCode.innerText = state.gameId;

        if (surrenderButton) {
            surrenderButton.classList.add("hidden");
        }

        return;
    }

    waitingPanel.classList.add("hidden");

    if (state.status === "FINISHED") {
        actionPanel.classList.add("hidden");

        if (surrenderButton) {
            surrenderButton.classList.add("hidden");
        }
    } else {
        actionPanel.classList.remove("hidden");

        if (surrenderButton) {
            surrenderButton.classList.remove("hidden");
        }
    }
}

function surrenderGame() {
    if (!currentState) {
        alert("게임 상태를 불러오지 못했습니다.");
        return;
    }

    if (currentState.status === "FINISHED") {
        alert("이미 종료된 게임입니다.");
        return;
    }

    if (currentState.status === "WAITING_PLAYER") {
        alert("상대 플레이어가 아직 입장하지 않았습니다.");
        return;
    }

    const confirmed = confirm("정말 항복하시겠습니까?");

    if (!confirmed) {
        return;
    }

    if (!stompClient || !stompClient.connected) {
        alert("WebSocket 연결이 아직 완료되지 않았습니다.");
        return;
    }

    stompClient.send("/app/lineduel/surrender", {}, JSON.stringify({
        gameId: gameId,
        playerNumber: myPlayerNumber
    }));
}

function saveCurrentGame() {
    if (!gameId || !myPlayerNumber) {
        return;
    }

    localStorage.setItem(SAVED_GAME_ID_KEY, gameId);
    localStorage.setItem(SAVED_PLAYER_NUMBER_KEY, String(myPlayerNumber));
}

function clearSavedGame() {
    localStorage.removeItem(SAVED_GAME_ID_KEY);
    localStorage.removeItem(SAVED_PLAYER_NUMBER_KEY);

    updateReconnectPanel();
}

function getSavedGame() {
    const savedGameId = localStorage.getItem(SAVED_GAME_ID_KEY);
    const savedPlayerNumber = Number(localStorage.getItem(SAVED_PLAYER_NUMBER_KEY));

    if (!savedGameId || !savedPlayerNumber) {
        return null;
    }

    if (savedPlayerNumber !== 1 && savedPlayerNumber !== 2) {
        return null;
    }

    return {
        gameId: savedGameId,
        playerNumber: savedPlayerNumber
    };
}

function updateReconnectPanel() {
    const panel = document.getElementById("reconnectPanel");
    const savedGameText = document.getElementById("savedGameText");

    if (!panel || !savedGameText) {
        return;
    }

    const savedGame = getSavedGame();

    if (!savedGame || gameId) {
        panel.classList.add("hidden");
        return;
    }

    savedGameText.innerText = `${savedGame.gameId} / Player ${savedGame.playerNumber}`;
    panel.classList.remove("hidden");
}

async function reconnectGame() {
    const savedGame = getSavedGame();

    if (!savedGame) {
        alert("저장된 게임 정보가 없습니다.");
        return;
    }

    await loadCards();

    const response = await fetch("/api/lineduel/games/reconnect", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            gameId: savedGame.gameId,
            playerNumber: savedGame.playerNumber
        })
    });

    if (!response.ok) {
        const text = await response.text();
        console.error(text);
        alert("재접속에 실패했습니다. 저장된 게임 기록을 삭제합니다.");
        clearSavedGame();
        return;
    }

    const result = await response.json();

    gameId = result.state.gameId;
    currentState = result.state;
    myPlayerNumber = result.playerNumber;
    selectedCardId = null;
    selectedHandIndex = null;

    saveCurrentGame();
    hideResultPanel();

    showRoomInfo();
    connectSocket(gameId);
    render(currentState, currentState.logs || []);
    updateReconnectPanel();
}

window.addEventListener("DOMContentLoaded", function () {
    updateReconnectPanel();
    loadMatchHistory();
});

async function loadMatchHistory() {
    const list = document.getElementById("matchHistoryList");

    if (!list) {
        return;
    }

    list.innerHTML = `<p class="empty-history">경기 기록을 불러오는 중...</p>`;

    try {
        const response = await fetch("/api/lineduel/matches");

        if (!response.ok) {
            list.innerHTML = `<p class="empty-history">경기 기록을 불러오지 못했습니다.</p>`;
            return;
        }

        const matches = await response.json();

        renderMatchHistory(matches);
    } catch (error) {
        console.error(error);
        list.innerHTML = `<p class="empty-history">경기 기록을 불러오지 못했습니다.</p>`;
    }
}

function renderMatchHistory(matches) {
    const list = document.getElementById("matchHistoryList");

    if (!list) {
        return;
    }

    if (!matches || matches.length === 0) {
        list.innerHTML = `<p class="empty-history">아직 경기 기록이 없습니다.</p>`;
        return;
    }

    const sortedMatches = [...matches].sort((a, b) => {
        const aTime = a.endedAt || a.startedAt;
        const bTime = b.endedAt || b.startedAt;

        return new Date(bTime) - new Date(aTime);
    });

    list.innerHTML = "";

    sortedMatches.slice(0, 10).forEach(match => {
        const item = document.createElement("div");
        item.className = "match-item";

        if (match.status === "FINISHED") {
            item.classList.add("finished-match");
        } else {
            item.classList.add("progress-match");
        }

        const resultText = getMatchResultText(match);
        const reasonText = getEndReasonText(match.endReason);
        const timeText = formatMatchTime(match.endedAt || match.startedAt);

        item.innerHTML = `
            <div class="match-main">
                <strong>${resultText}</strong>
                <span class="match-status">${match.status}</span>
            </div>

            <div class="match-sub">
                <span>방 코드: ${match.gameId}</span>
                <span>턴: ${match.turnCount}</span>
                <span>종료 사유: ${reasonText}</span>
            </div>

            <div class="match-time">
                ${timeText}
            </div>
        `;

        list.appendChild(item);
    });
}

function getMatchResultText(match) {
    if (match.status !== "FINISHED") {
        return "진행 중인 경기";
    }

    if (match.winner === "DRAW") {
        return "무승부";
    }

    return `${match.winner} 승리`;
}

function getEndReasonText(endReason) {
    if (!endReason) {
        return "-";
    }

    if (endReason === "SURRENDER") {
        return "항복";
    }

    if (endReason === "HP_ZERO") {
        return "HP 0";
    }

    return endReason;
}

function formatMatchTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function getCardTypeText(type) {
    if (type === "UNIT") {
        return "유닛";
    }

    if (type === "SPELL") {
        return "주문";
    }

    return type || "-";
}

function getCardStatText(card) {
    if (card.type === "UNIT") {
        return `ATK ${card.attack} / HP ${card.hp}`;
    }

    if (card.type === "SPELL") {
        if (card.spellEffectType === "DAMAGE_HERO") {
            return `Damage ${card.attack}`;
        }

        if (card.spellEffectType === "HEAL_SELF") {
            return `Heal ${card.attack}`;
        }

        if (card.spellEffectType === "DRAW_CARD") {
            return `Draw ${card.attack}`;
        }

        return "Spell";
    }

    return `ATK ${card.attack} / HP ${card.hp}`;
}

function getSpellEffectText(effectType) {
    if (effectType === "DAMAGE_HERO") {
        return "상대 피해";
    }

    if (effectType === "HEAL_SELF") {
        return "자기 회복";
    }

    if (effectType === "DRAW_CARD") {
        return "카드 드로우";
    }

    return "";
}