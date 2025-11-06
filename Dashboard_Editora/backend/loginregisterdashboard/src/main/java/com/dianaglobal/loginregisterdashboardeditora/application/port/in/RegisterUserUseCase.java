// src/main/java/com/dianaglobal/loginregister/application/port/in/RegisterUserUseCase.java
package com.dianaglobal.loginregisterdashboardeditora.application.port.in;

public interface RegisterUserUseCase {
    void register(String name, String email, String password);
    // registerOauthUser removido - Google OAuth não é mais suportado
}
