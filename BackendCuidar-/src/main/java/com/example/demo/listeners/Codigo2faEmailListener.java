package com.example.demo.listeners;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.demo.events.Codigo2faGeradoEvent;
import com.example.demo.services.EmailService;

@Component
public class Codigo2faEmailListener {

    private final EmailService emailService;

    public Codigo2faEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarEmail(Codigo2faGeradoEvent event) {
        emailService.enviarCodigoVerificacao(event.email(), event.codigo());
    }
}
