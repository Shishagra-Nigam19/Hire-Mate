package com.hiremate.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HireMateException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
