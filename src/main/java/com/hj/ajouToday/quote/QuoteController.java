package com.hj.ajouToday.quote;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@RestController
public class QuoteController {

    private final QuoteRepository quoteRepository;

    // 가게가 열리면 매니저(Repository)를 컨트롤러에 출근시킵니다.
    public QuoteController(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    // 기능 1: 장부에서 무작위 명언 꺼내주기
    @GetMapping("/api/quote")
    public String getRandomQuote() {
        List<Quote> quotes = quoteRepository.findAll(); // 장부에 있는 모든 명언 가져오기

        if (quotes.isEmpty()) {
            return "아직 장부에 명언이 없습니다. 먼저 명언을 추가해주세요!";
        }

        Random random = new Random();
        int randomIndex = random.nextInt(quotes.size());
        return quotes.get(randomIndex).getContent();
    }

    // 기능 2: 장부에 새로운 명언 추가하기
    // (원래 데이터 추가는 POST 방식을 써야 하지만, 지금은 화면이 없으니 브라우저 주소창에서 쉽게 테스트하려고 임시로 GET 방식을 씁니다!)
    @GetMapping("/api/quote/add")
    public String addQuote(@RequestParam String text) {
        Quote newQuote = new Quote(text);
        quoteRepository.save(newQuote); // 매니저에게 장부에 적으라고 지시!
        return "장부에 성공적으로 저장되었습니다: " + text;
    }

    // 기능 3: CI/CD 자동 배포 테스트용 API (헬스 체크)
    @GetMapping("/api/health")
    public String healthCheck() {
        return "삐리삐리 🤖 로봇 매니저가 성공적으로 코드를 배포했습니다! 서버 100% 정상 작동 중입니다 🚀";
    }
}