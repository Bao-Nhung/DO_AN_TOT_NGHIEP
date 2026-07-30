package com.zestia.datn.zestia.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PosReservationExpiryScheduler {
    private final PosReservationService reservationService;

    @Scheduled(fixedDelayString = "${app.pos-reservation.cleanup-ms:60000}")
    public void releaseExpiredReservations() {
        reservationService.findExpiredIds(100)
                .forEach(reservationService::expireById);
    }
}
