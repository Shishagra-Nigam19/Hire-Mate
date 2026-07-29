package com.hiremate.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends HireMateException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
