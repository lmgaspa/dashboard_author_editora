// src/main/java/com/dianaglobal/paineldoauthor/application/service/exception/EmailAlreadyUsedException.java
package com.dianaglobal.paineldoauthorbackend.application.service.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String email) {
        super("E-mail already in use: " + email);
    }
}
