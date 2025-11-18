package com.dianaglobal.paineldoauthorbackend.config;

public final class ApiPaths {
    private ApiPaths() {}

    // Versão raiz
    public static final String API_V1_BASE       = "/api/v1";

    // Domínios principais da API
    public static final String AUTH_BASE         = API_V1_BASE + "/auth";
    public static final String AUTH_PASSWORD     = AUTH_BASE + "/password";
    public static final String AUTH_EMAIL        = AUTH_BASE + "/email";

    // Path para cookies (não usado mais - tokens removidos)
    public static final String AUTH_COOKIE_PATH  = AUTH_BASE;

    // Rotas de dados do usuário autenticado
    public static final String USER_BASE         = API_V1_BASE + "/user";
    
    // Painel admin (agora em v1)
    public static final String ADMIN_BASE        = API_V1_BASE + "/admin";
    
    // Painel user (agora em v1)
    public static final String USER_PANEL_BASE  = API_V1_BASE + "/user";
}
