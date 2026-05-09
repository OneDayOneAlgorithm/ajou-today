package com.hj.ajouToday.lineduel;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LineDuelSocketController {

    private final LineDuelService service;
    private final SimpMessagingTemplate messagingTemplate;

    public LineDuelSocketController(
            LineDuelService service,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/lineduel/play")
    public void play(LineDuelSocketRequest request) {
        try {
            PlayCardRequest playRequest = new PlayCardRequest();
            playRequest.setPlayerNumber(request.getPlayerNumber());
            playRequest.setCardId(request.getCardId());

            TurnResult result = service.playCard(request.getGameId(), playRequest);

            GameState state = result.getState();

            sendToPlayer(state, 1, result);
            sendToPlayer(state, 2, result);

        } catch (Exception e) {
            sendError(request, e.getMessage());
        }
    }

    private void sendToPlayer(GameState state, int playerNumber, TurnResult result) {
        if (!state.isPlayerJoined(playerNumber)) {
            return;
        }

        TurnViewResult viewResult = new TurnViewResult(
                service.toViewState(state, playerNumber),
                result.getLogs(),
                result.isResolved()
        );

        messagingTemplate.convertAndSend(
                "/topic/lineduel/" + state.getGameId() + "/player/" + playerNumber,
                viewResult
        );
    }

    private void sendError(LineDuelSocketRequest request, String message) {
        if (request == null || request.getGameId() == null) {
            return;
        }

        int playerNumber = request.getPlayerNumber();

        if (playerNumber != 1 && playerNumber != 2) {
            return;
        }

        messagingTemplate.convertAndSend(
                "/topic/lineduel/" + request.getGameId() + "/player/" + playerNumber,
                new LineDuelErrorResult(message == null ? "알 수 없는 오류가 발생했습니다." : message)
        );
    }
}