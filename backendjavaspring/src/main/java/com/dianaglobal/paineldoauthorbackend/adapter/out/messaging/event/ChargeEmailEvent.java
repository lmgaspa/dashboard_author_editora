package com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChargeEmailEvent(
        String eventType,
        String userName,
        String userEmail,
        int chargeMonth,
        int chargeYear,
        double amount,
        String dueDate,
        String pixCode,
        String pixImageUrl,
        String confirmedAt,
        Long daysOverdue,
        List<String> adminEmails
) {}
