package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {
    public BusinessException(String message) {
        super(message, HttpStatus.valueOf(422), "BUSINESS_ERROR");
    }
}