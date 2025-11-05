package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.login;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(
            String id,
            String name,
            String email,
            String role,
            String authProvider,
            boolean passwordSet
    ) {}
}
