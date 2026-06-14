package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AppException {
    public InvalidTokenException() {
        super("Token invalido ou expirado", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
}
