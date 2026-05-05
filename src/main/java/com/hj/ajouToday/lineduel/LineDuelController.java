package com.hj.ajouToday.lineduel;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineduel")
public class LineDuelController {

    private final LineDuelService service;

    public LineDuelController(LineDuelService service) {
        this.service = service;
    }

    @GetMapping("/cards")
    public List<Card> getCards() {
        return service.getCards();
    }

    @PostMapping("/games")
    public GameState startGame() {
        return service.startGame();
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
}