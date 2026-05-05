package com.hj.ajouToday.lineduel;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LineDuelSocketController {

    private final LineDuelService lineDuelService;
    private final SimpMessagingTemplate messagingTemplate;

    public LineDuelSocketController(
            LineDuelService lineDuelService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.lineDuelService = lineDuelService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/lineduel/play")
    public void play(LineDuelSocketRequest request) {
        PlayCardRequest playCardRequest = new PlayCardRequest();
        playCardRequest.setPlayerNumber(request.getPlayerNumber());
        playCardRequest.setCardId(request.getCardId());

        TurnResult result = lineDuelService.playCard(request.getGameId(), playCardRequest);

        messagingTemplate.convertAndSend(
                "/topic/lineduel/" + request.getGameId(),
                result
        );
    }
}