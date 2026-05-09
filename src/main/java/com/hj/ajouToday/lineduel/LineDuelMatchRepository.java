package com.hj.ajouToday.lineduel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineDuelMatchRepository extends JpaRepository<LineDuelMatch, Long> {

    Optional<LineDuelMatch> findByGameId(String gameId);
}