package com.hj.ajouToday.game;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class GameScoreController {

    private final GameScoreRepository repository;

    public GameScoreController(GameScoreRepository repository) {
        this.repository = repository;
    }

    // 기능 1: 명예의 전당(Top 5) 데이터 프론트엔드로 보내주기
    @GetMapping("/top")
    public List<GameScore> getTopScores() {
        return repository.findTop5ByOrderByScoreDesc();
    }

    // 기능 2: 게임이 끝나면 유저 이름과 점수를 받아서 DB에 저장하기 (POST 방식)
    @PostMapping
    public String saveScore(@RequestParam String playerName, @RequestParam int score) {
        repository.save(new GameScore(playerName, score));
        return "점수 저장 성공!";
    }
}