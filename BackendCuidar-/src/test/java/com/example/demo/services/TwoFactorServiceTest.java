package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.demo.entity.CodigoVerificacao;
import com.example.demo.events.Codigo2faGeradoEvent;
import com.example.demo.repository.CodigoVerificacaoRepository;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceTest {

    @Mock
    private CodigoVerificacaoRepository repository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private TwoFactorService service;

    @Test
    void devePublicarEventoAposGerarCodigo() {
        service.enviarCodigo("cuidador@email.com");

        verify(repository).deleteByEmail("cuidador@email.com");
        verify(repository).save(any(CodigoVerificacao.class));

        ArgumentCaptor<Codigo2faGeradoEvent> captor = ArgumentCaptor.forClass(Codigo2faGeradoEvent.class);
        verify(publisher).publishEvent(captor.capture());

        Codigo2faGeradoEvent event = captor.getValue();
        assertEquals("cuidador@email.com", event.email());
        assertEquals(6, Objects.requireNonNull(event.codigo()).length());
    }
}
