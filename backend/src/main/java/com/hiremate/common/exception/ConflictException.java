package com.hiremate.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends HireMateException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
