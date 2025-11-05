package com.dianaglobal.loginregisterdashboardeditora.application.service;

public interface AccountConfirmationTokenService {

    //** Emite um novo token com validade em minutos. */
    String issue(String userId, int minutes);

    /** Invalida todos os tokens válidos do usuário. */
    void invalidateAllFor(String userId);

    /**
     * Consome (marca como usado) e valida o token.
     * Lança IllegalArgumentException se inválido/expirado/já usado.
     */
    /** Consome o token (uso único); lança se inválido/expirado/usado. */
    ConfirmationPayload consume(String rawToken);

    /** Payload retornado pelo consume(...) */
    record ConfirmationPayload(String userId) {}
}
