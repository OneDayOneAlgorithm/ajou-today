package com.hj.ajouToday.lineduel;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineduel")
public class LineDuelController {

    private final LineDuelService service;
    private final SimpMessagingTemplate messagingTemplate;

    public LineDuelController(
            LineDuelService service,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/cards")
    public List<Card> getCards() {
        return service.getCards();
    }

    @PostMapping("/games")
    public RoomJoinViewResult startGame() {
        RoomJoinResult result = service.startGame();

        LineDuelViewState viewState = service.toViewState(
                result.getState(),
                result.getPlayerNumber()
        );

        return new RoomJoinViewResult(viewState, result.getPlayerNumber());
    }

    @PostMapping("/games/join")
    public RoomJoinViewResult joinGame(@RequestBody RoomJoinRequest request) {
        RoomJoinResult result = service.joinGame(request.getGameId());
        GameState state = result.getState();

        // Player 1 화면 즉시 갱신
        messagingTemplate.convertAndSend(
                "/topic/lineduel/" + state.getGameId() + "/player/1",
                new TurnViewResult(
                        service.toViewState(state, 1),
                        state.getLogs(),
                        false
                )
        );

        // Player 2에게 반환할 화면 데이터
        LineDuelViewState viewState = service.toViewState(
                state,
                result.getPlayerNumber()
        );

        return new RoomJoinViewResult(viewState, result.getPlayerNumber());
    }

    @PostMapping("/games/reconnect")
    public RoomJoinViewResult reconnectGame(@RequestBody ReconnectRequest request) {
        RoomJoinResult result = service.reconnectGame(
                request.getGameId(),
                request.getPlayerNumber()
        );

        GameState state = result.getState();

        LineDuelViewState viewState = service.toViewState(
                state,
                result.getPlayerNumber()
        );

        return new RoomJoinViewResult(viewState, result.getPlayerNumber());
    }

    @GetMapping("/games/{gameId}")
    public GameState getGame(@PathVariable String gameId) {
        return service.getGame(gameId);
    }

    @PostMapping("/games/{gameId}/play")
    public TurnResult playCard(
            @PathVariable String gameId,
            @RequestBody PlayCardRequest request
    ) {
        return service.playCard(gameId, request);
    }

    @GetMapping("/rooms/count")
    public int getRoomCount() {
        return service.getActiveRoomCount();
    }

    @PostMapping("/rooms/cleanup")
    public int cleanupRooms() {
        return service.cleanupExpiredRooms();
    }
}