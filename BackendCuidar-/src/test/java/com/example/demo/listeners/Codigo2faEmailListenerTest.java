package com.example.demo.listeners;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.events.Codigo2faGeradoEvent;
import com.example.demo.services.EmailService;

@ExtendWith(MockitoExtension.class)
class Codigo2faEmailListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private Codigo2faEmailListener listener;

    @Test
    void deveEnviarEmailAoReceberEventoDeCodigoGerado() {
        listener.enviarEmail(new Codigo2faGeradoEvent("cuidador@email.com", "123456"));

        verify(emailService).enviarCodigoVerificacao("cuidador@email.com", "123456");
    }
}
