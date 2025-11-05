package com.dianaglobal.loginregisterdashboardeditora.application.service.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String msg) { super(msg); }
}
