package com.example.demo.services;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CodigoVerificacao;
import com.example.demo.events.Codigo2faGeradoEvent;
import com.example.demo.exceptions.VerificationCodeException;
import com.example.demo.repository.CodigoVerificacaoRepository;

@Service
public class TwoFactorService {

    private final CodigoVerificacaoRepository repository;
    private final ApplicationEventPublisher publisher;

    public TwoFactorService(CodigoVerificacaoRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public void enviarCodigo(String email) {
        repository.deleteByEmail(email);

        String codigo = gerarCodigo();

        CodigoVerificacao verificacao = new CodigoVerificacao();
        verificacao.setEmail(email);
        verificacao.setCodigo(codigo);
        verificacao.setExpiracao(LocalDateTime.now().plusMinutes(10));
        verificacao.setUsado(false);

        repository.save(verificacao);
        publisher.publishEvent(new Codigo2faGeradoEvent(email, codigo));
    }

    @Transactional
    public void validarCodigo(String email, String codigo) {
        CodigoVerificacao verificacao = repository
                .findTopByEmailAndUsadoFalseOrderByExpiracaoDesc(email)
                .orElseThrow(() -> new VerificationCodeException("Nenhum codigo ativo encontrado para este email"));

        if (verificacao.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeException("Codigo expirado, solicite um novo");
        }

        if (!verificacao.getCodigo().equals(codigo)) {
            throw new VerificationCodeException("Codigo invalido");
        }

        verificacao.setUsado(true);
        repository.save(verificacao);
    }

    private String gerarCodigo() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
