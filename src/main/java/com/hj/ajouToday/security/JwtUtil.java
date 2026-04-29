package com.hj.ajouToday.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component // 스프링이 관리하는 부품으로 등록
public class JwtUtil {

    // 💡 실무 포인트 1: 핵심 보안 객체는 무조건 final로 선언하여 중간에 바뀌는 것을 원천 차단합니다.
    private final Key key;

    // 토큰 유효기간: 1시간 (1000ms * 60 * 60)
    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    // 💡 실무 포인트 2: '생성자 주입' 방식
    // 스프링이 JwtUtil을 생성할 때, application.yml에서 jwt.secret 값을 강제로 가져와서 넣어줍니다.
    // 값이 없으면 아예 객체 생성을 막아버려서 NullPointerException을 사전에 방지합니다.
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        // 주입받은 비밀번호를 이용해 안전하게 암호 키 뼈대를 완성합니다.
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 1. JWT 토큰을 발급(생성)하는 메서드
    public String generateToken() {
        return Jwts.builder()
                .setSubject("admin") // 이 토큰의 주인은 'admin'이다
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 비밀 도장 쾅!
                .compact(); // 문자열로 압축!
    }

    // 2. JWT 토큰이 진짜인지 검사하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 에러 없이 통과하면 진짜 토큰!
        } catch (Exception e) {
            return false; // 기간이 지났거나, 위조되었으면 가짜!
        }
    }
}