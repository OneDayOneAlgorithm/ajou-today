package com.hj.ajouToday.survival;

import com.hj.ajouToday.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreRepository scoreRepository;
    private final JwtUtil jwtUtil;

    public ScoreController(ScoreRepository scoreRepository, JwtUtil jwtUtil) {
        this.scoreRepository = scoreRepository;
        this.jwtUtil = jwtUtil;
    }

    // 1. 게임 시작 시 '입장권(토큰)' 발급
    @GetMapping("/start")
    public String getGameToken() {
        return jwtUtil.generateToken();
    }

    // 2. 점수 저장 (입장권 검사 수행)
    @PostMapping
    public ResponseEntity<String> saveScore(
            @RequestBody ScoreRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 입장권이 없습니다.");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 유효하지 않은 입장권입니다.");
        }

        Score newScore = new Score();
        newScore.setPlayerName(request.getPlayerName());
        newScore.setScore(request.getScore());
        scoreRepository.save(newScore);

        return ResponseEntity.ok("성공적으로 저장되었습니다!");
    }

    // 3. 상위 랭킹 조회
    @GetMapping("/top")
    public List<Score> getTopScores() {
        return scoreRepository.findTop10ByOrderByScoreDesc();
    }
}