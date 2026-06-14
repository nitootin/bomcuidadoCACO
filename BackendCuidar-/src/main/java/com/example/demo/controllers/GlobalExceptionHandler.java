package com.example.demo.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.exceptions.AppException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Captura todas as suas exceptions customizadas (ResourceNotFound, Business, Unauthorized)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of(
            "code", ex.getErrorCode(),
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Captura erros de validação (@Valid nos DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();

        return ResponseEntity.badRequest().body(Map.of(
            "code", "VALIDATION_ERROR",
            "errors", errors,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Captura qualquer erro inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        logger.error("Erro inesperado no servidor", ex);
        return ResponseEntity.internalServerError().body(Map.of(
            "code", "INTERNAL_ERROR",
            "message", "Erro inesperado no servidor",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
