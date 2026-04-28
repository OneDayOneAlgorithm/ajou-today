package com.hj.ajouToday.quote;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteRepository repository;

    // 💡 관리자 비밀번호 설정 (실무에서는 이렇게 코드에 직접 쓰지 않고 설정 파일(.env)에 숨깁니다!)
    private final String ADMIN_PASSWORD = "ajou!";

    public QuoteController(QuoteRepository repository) {
        this.repository = repository;
    }

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

    // 🔒 [보안 추가] 등록할 때 "Admin-Key"라는 이름의 헤더(비밀통로)를 검사합니다.
    @PostMapping
    public Quote createQuote(@RequestBody Quote quote, @RequestHeader(value = "Admin-Key", required = false) String adminKey) {
        if (!ADMIN_PASSWORD.equals(adminKey)) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다!");
        }
        return repository.save(quote);
    }

    // 🔒 [보안 추가] 삭제할 때도 동일하게 검사합니다.
    @DeleteMapping("/{id}")
    public void deleteQuote(@PathVariable Long id, @RequestHeader(value = "Admin-Key", required = false) String adminKey) {
        if (!ADMIN_PASSWORD.equals(adminKey)) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다!");
        }
        repository.deleteById(id);
    }
}