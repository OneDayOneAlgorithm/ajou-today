package com.hj.ajouToday.game;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameScoreRepository extends JpaRepository<GameScore, Long> {
    // 💡 마법의 코드: "점수(Score) 기준으로 내림차순(Desc) 정렬해서 상위 5개(Top5)만 가져와라!"
    List<GameScore> findTop5ByOrderByScoreDesc();
}