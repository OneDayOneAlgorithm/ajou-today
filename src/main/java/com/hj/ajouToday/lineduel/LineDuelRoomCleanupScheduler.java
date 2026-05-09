package com.hj.ajouToday.lineduel;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LineDuelRoomCleanupScheduler {

    private final LineDuelService service;

    public LineDuelRoomCleanupScheduler(LineDuelService service) {
        this.service = service;
    }

    @Scheduled(fixedRate = 1000 * 60 * 10)
    //@Scheduled(fixedRate = 1000 * 10)
    public void cleanupExpiredRooms() {
        int removedCount = service.cleanupExpiredRooms();

        if (removedCount > 0) {
            System.out.println("[LineDuel] 만료된 게임방 " + removedCount + "개 정리 완료");
        }
    }
}