let gameId = null;
let cards = [];
let stompClient = null;
let currentState = null;

async function startGame() {
    await loadCards();

    const response = await fetch("/api/lineduel/games", {
        method: "POST"
    });

    const state = await response.json();
    gameId = state.gameId;
    currentState = state;

    document.getElementById("game").classList.remove("hidden");

    connectSocket(gameId);
    render(state, []);
}

async function loadCards() {
    const response = await fetch("/api/lineduel/cards");
    cards = await response.json();
}

function playCard(playerNumber, cardId) {
    if (!stompClient || !stompClient.connected) {
        alert("WebSocket 연결이 아직 완료되지 않았습니다.");
        return;
    }

    stompClient.send("/app/lineduel/play", {}, JSON.stringify({
        gameId: gameId,
        playerNumber: playerNumber,
        cardId: cardId
    }));
}

    if (!response.ok) {
        const text = await response.text();
        console.error(text);
        alert("카드를 낼 수 없습니다.");
        return;
    }

    const result = await response.json();
    render(result.state, result.logs);
}

function render(state, logs) {
    document.getElementById("turn").innerText = `Turn ${state.turn}`;
    document.getElementById("winner").innerText =
        state.status === "FINISHED" ? `승자: ${state.winner}` : "";

    renderPlayer("p1", state.player1);
    renderPlayer("p2", state.player2);

    renderHand("p1Hand", 1, state.player1.hand, state);
    renderHand("p2Hand", 2, state.player2.hand, state);

    renderLogs(logs);

    renderPendingStatus(state);
}

function renderPendingStatus(state) {
    const logs = document.getElementById("logs");

    if (!state.pendingActions) return;
    if (state.status === "FINISHED") return;

    const p1Ready = state.pendingActions[1] ? "제출 완료" : "대기 중";
    const p2Ready = state.pendingActions[2] ? "제출 완료" : "대기 중";

    const li = document.createElement("li");
    li.innerText = `Player 1: ${p1Ready} / Player 2: ${p2Ready}`;
    logs.appendChild(li);
}

function renderPlayer(prefix, player) {
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

function renderHand(elementId, playerNumber, hand, state) {
    const handDiv = document.getElementById(elementId);
    handDiv.innerHTML = "";

    const alreadySubmitted =
        state.pendingActions && state.pendingActions[playerNumber];

    hand.forEach(cardId => {
        const card = cards.find(c => c.id === cardId);
        if (!card) return;

        const div = document.createElement("div");
        div.className = "card";

        div.innerHTML = `
            <strong>${card.name}</strong>
            <p>Cost ${card.cost}</p>
            <p>ATK ${card.attack} / HP ${card.hp}</p>
            <small>${card.description}</small>
        `;

        if (state.status !== "FINISHED" && !alreadySubmitted) {
            div.onclick = () => playCard(playerNumber, card.id);
        } else {
            div.style.opacity = "0.5";
            div.style.cursor = "not-allowed";
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
        ul.appendChild(li);
    });
}

function connectSocket(gameId) {
    const socket = new SockJS("/ws-lineduel");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("WebSocket 연결 성공");

        stompClient.subscribe(`/topic/lineduel/${gameId}`, function (message) {
            const result = JSON.parse(message.body);
            currentState = result.state;
            render(result.state, result.logs);
        });
    });
}