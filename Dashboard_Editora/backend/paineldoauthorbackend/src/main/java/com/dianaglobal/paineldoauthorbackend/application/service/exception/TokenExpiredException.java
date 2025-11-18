package com.dianaglobal.paineldoauthorbackend.application.service.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String msg) { super(msg); }
}

