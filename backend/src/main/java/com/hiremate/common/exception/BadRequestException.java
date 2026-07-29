package com.hiremate.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends HireMateException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}
