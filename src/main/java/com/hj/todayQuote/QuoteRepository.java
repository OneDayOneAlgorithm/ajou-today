package com.hj.todayQuote;

import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<관리할양식, 고유번호타입> 을 상속받으면 모든 준비가 끝납니다!
public interface QuoteRepository extends JpaRepository<Quote, Long> {
}