package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceInUseException extends AppException {
    public ResourceInUseException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_IN_USE");
    }
}
