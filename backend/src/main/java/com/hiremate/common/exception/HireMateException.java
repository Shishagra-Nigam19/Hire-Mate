package com.hiremate.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HireMateException extends RuntimeException {

    private final HttpStatus status;

    public HireMateException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HireMateException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}
