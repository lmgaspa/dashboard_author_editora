// src/main/java/com/dianaglobal/paineldoauthor/application/port/in/RegisterUserUseCase.java
package com.dianaglobal.paineldoauthorbackend.application.port.in;

public interface RegisterUserUseCase {
    void register(String name, String email, String password);
    // registerOauthUser removido - Google OAuth não é mais suportado
}
