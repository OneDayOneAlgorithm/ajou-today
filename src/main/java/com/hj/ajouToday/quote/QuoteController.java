package com.hj.ajouToday.quote;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.hj.ajouToday.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteRepository repository;
    private final JwtUtil jwtUtil; // 💡 1. 우리가 만든 도장 공방(JwtUtil)을 불러옵니다.

    @Value("${admin.password}")
    private String adminPassword;

    // 생성자에 JwtUtil 추가
    public QuoteController(QuoteRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    // ==========================================
    // 🔓 누구나 접근 가능한 영역 (조회)
    // ==========================================
    @GetMapping("/random")
    public Quote getRandomQuote() {
        List<Quote> all = repository.findAll();
        if (all.isEmpty()) return new Quote("등록된 명언이 없습니다.");
        return all.get((int) (Math.random() * all.size()));
    }

    @GetMapping
    public List<Quote> getAllQuotes() {
        return repository.findAll();
    }

    // 💡 2. [신규 추가] 로그인 창구: 비밀번호를 받아서 마패(토큰)를 내어줍니다.
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> request) {
        String password = request.get("password");

        if (adminPassword.equals(password)) {
            return jwtUtil.generateToken(); // 비밀번호가 맞으면 JWT 토큰 발급!
        }
        throw new IllegalArgumentException("비밀번호가 틀렸습니다!");
    }

    // ==========================================
    // 🔒 관리자만 접근 가능한 영역 (등록, 삭제)
    // ==========================================

    // 💡 3. 마패 검사 전담 메서드 (토큰이 없거나 가짜면 에러를 던짐)
    private void checkAdminToken(String authHeader) {
        // 실무 표준: 토큰은 항상 "Bearer "라는 글자로 시작하도록 약속되어 있습니다.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("마패(토큰)가 없습니다!");
        }

        // "Bearer " 7글자를 잘라내고 진짜 토큰(알맹이)만 꺼냅니다.
        String token = authHeader.substring(7);

        // JwtUtil에게 진짜 도장이 맞는지 물어봅니다.
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("위조되었거나 만료된 마패입니다!");
        }
    }

    // 등록 API: 헤더 이름이 "Admin-Key"에서 실무 표준인 "Authorization"으로 바뀌었습니다.
    @PostMapping
    public Quote createQuote(@RequestBody Quote quote, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        checkAdminToken(authHeader); // 👈 여기서 마패 검사!
        return repository.save(quote);
    }

    // 삭제 API: 동일하게 마패 검사 적용
    @DeleteMapping("/{id}")
    public void deleteQuote(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        checkAdminToken(authHeader); // 👈 여기서 마패 검사!
        repository.deleteById(id);
    }
}