package com.hj.ajouToday.quote;

import jakarta.persistence.*;

@Entity // "이 클래스는 데이터베이스의 테이블(표) 역할을 할 거야!" 라는 뜻
public class Quote {

    @Id // 이 항목이 고유한 번호(주민번호)라는 뜻
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 번호를 1번부터 자동으로 매겨달라는 뜻
    private Long id;

    private String content; // 명언 내용

    // 기본 생성자 (JPA 매니저가 쓰기 위해 꼭 필요함)
    public Quote() {}

    // 명언 내용을 넣기 위한 생성자
    public Quote(String content) {
        this.content = content;
    }

    // 명언 내용을 꺼내보기 위한 기능
    public String getContent() {
        return content;
    }
}