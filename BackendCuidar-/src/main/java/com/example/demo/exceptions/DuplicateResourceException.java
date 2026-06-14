package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends AppException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String resource, String field) {
        super(resource + " ja existe com esse " + field, HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
}
