package com.hj.ajouToday.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;

@Component // 스프링이 관리하는 부품으로 등록
public class JwtUtil {

    // 💡 서버만의 비밀 도장 (이 키가 털리면 끝장납니다. 실무에선 엄청 길고 복잡하게 씁니다)
    // 최소 32바이트(256비트) 이상이어야 작동합니다.
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 토큰 유효기간: 1시간 (1000ms * 60 * 60)
    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
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