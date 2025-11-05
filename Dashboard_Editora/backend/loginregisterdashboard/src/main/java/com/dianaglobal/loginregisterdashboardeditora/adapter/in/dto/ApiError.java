package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto;

import lombok.Builder;
import java.time.Instant;

@Builder
public record ApiError(
        String error,
        String message,
        Boolean canResend,
        Integer cooldownSecondsRemaining,
        Integer attemptsToday,
        Integer maxPerDay,
        Instant nextAllowedAt
) {}
