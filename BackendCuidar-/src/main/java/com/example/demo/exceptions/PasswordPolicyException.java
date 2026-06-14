package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class PasswordPolicyException extends AppException {
    public PasswordPolicyException() {
        super(
                "A senha deve ter no minimo 8 caracteres, com letra maiuscula, minuscula, numero e caractere especial",
                HttpStatus.UNPROCESSABLE_ENTITY,
                "PASSWORD_POLICY_VIOLATION");
    }

    public PasswordPolicyException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_POLICY_VIOLATION");
    }
}
