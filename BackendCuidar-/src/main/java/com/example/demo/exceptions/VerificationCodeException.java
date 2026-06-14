package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class VerificationCodeException extends AppException {
    public VerificationCodeException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "VERIFICATION_CODE_ERROR");
    }
}
