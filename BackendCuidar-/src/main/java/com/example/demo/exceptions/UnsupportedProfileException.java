package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class UnsupportedProfileException extends AppException {
    public UnsupportedProfileException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "UNSUPPORTED_PROFILE");
    }
}
