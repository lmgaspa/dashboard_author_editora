package com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.event;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthEmailEvent(
        String eventType,
        String userName,
        String userEmail,
        String token,
        String actionUrl,
        String plaintextPassword,
        String supportUrl
) {}
