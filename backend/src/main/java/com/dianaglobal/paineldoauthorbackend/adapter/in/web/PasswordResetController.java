// src/main/java/com/dianaglobal/paineldoauthor/adapter/in/web/PasswordResetController.java
package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.password.ForgotPasswordRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.password.ResetPasswordRequest;
import com.dianaglobal.paineldoauthorbackend.application.service.PasswordResetService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.AUTH_BASE)
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService service;

    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    @PostMapping({"/forgot-password", "/password/forgot"})
    public ResponseEntity<Void> forgot(@RequestBody @Valid ForgotPasswordRequest body) {
        service.requestReset(body.email(), frontendBaseUrl);
        return ResponseEntity.noContent().build();
    }

    @PostMapping({"/reset-password", "/password/reset"})
    public ResponseEntity<Void> reset(@RequestBody @Valid ResetPasswordRequest body) {
        service.resetPassword(body.token(), body.newPassword());
        return ResponseEntity.ok().build();
    }
}
