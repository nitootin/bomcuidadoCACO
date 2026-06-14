package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends AppException {
    public InvalidRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
    }

    public InvalidRequestException(String field, String reason) {
        super(field + ": " + reason, HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
    }
}
