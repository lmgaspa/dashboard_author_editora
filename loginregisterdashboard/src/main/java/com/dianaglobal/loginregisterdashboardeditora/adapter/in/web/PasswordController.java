package com.dianaglobal.loginregisterdashboardeditora.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.password.ChangePasswordRequest;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail.PasswordSetEmailService;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.config.ApiPaths;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequestMapping(    ApiPaths.AUTH_PASSWORD)
@RequiredArgsConstructor
@Validated
public class PasswordController {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final PasswordSetEmailService passwordSetEmailService;

    // CHANGE PASSWORD (usuário autenticado)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/change", consumes = "application/json", produces = "application/json")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid ChangePasswordRequest body
    ) {
        var user = userRepositoryPort.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(body.currentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(body.newPassword()));
        user.setPasswordSet(true);
        userRepositoryPort.save(user);

        try {
            passwordSetEmailService.sendChange(user.getEmail(), user.getName());
        } catch (Exception ignored) {}

        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    public record MessageResponse(String message) {}
}
