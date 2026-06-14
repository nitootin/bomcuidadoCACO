package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;

public class EmailSendingException extends AppException {
    public EmailSendingException() {
        super("Falha ao enviar codigo de verificacao", HttpStatus.BAD_GATEWAY, "EMAIL_SENDING_ERROR");
    }
}
