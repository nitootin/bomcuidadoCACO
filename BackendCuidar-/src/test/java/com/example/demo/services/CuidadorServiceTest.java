package com.example.demo.services;

import static com.example.demo.support.TestDataFactory.contatoDTO;
import static com.example.demo.support.TestDataFactory.cuidador;
import static com.example.demo.support.TestDataFactory.instituicao;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dtos.CuidadorDTO;
import com.example.demo.entity.Cuidador;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.InstituicaoRepository;

@ExtendWith(MockitoExtension.class)
class CuidadorServiceTest {

    @Mock
    private CuidadorRepository cuidadorRepository;

    @Mock
    private IdosoRepository idosoRepository;

    @Mock
    private InstituicaoRepository instituicaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SenhaService senhaService;

    @InjectMocks
    private CuidadorService service;

    @Test
    void deveBloquearCriacaoComCpfDeIdoso() {
        CuidadorDTO dto = cuidadorDTO();

        when(cuidadorRepository.existsByCpf("12345678901")).thenReturn(false);
        when(idosoRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.criar(dto));
    }

    @Test
    void deveBloquearAtualizacaoComCpfDeIdoso() {
        CuidadorDTO dto = cuidadorDTO();
        dto.setCpf("10987654321");
        Cuidador existente = cuidador();
        existente.setStatus(Status.ATIVO);

        when(cuidadorRepository.findById(2)).thenReturn(Optional.of(existente));
        when(cuidadorRepository.existsByCpf("10987654321")).thenReturn(false);
        when(idosoRepository.existsByCpf("10987654321")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.atualizar(2, dto));
    }

    @Test
    void deveBloquearReativacaoComCpfDeIdoso() {
        CuidadorDTO dto = new CuidadorDTO();
        dto.setCpf("10987654321");
        Cuidador existente = cuidador();
        existente.setStatus(Status.INATIVO);

        when(cuidadorRepository.findById(2)).thenReturn(Optional.of(existente));
        when(cuidadorRepository.existsByCpf("10987654321")).thenReturn(false);
        when(idosoRepository.existsByCpf("10987654321")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.reativar(2, dto));
    }

    private CuidadorDTO cuidadorDTO() {
        CuidadorDTO dto = new CuidadorDTO();
        dto.setNome("Cuidador");
        dto.setCpf("12345678901");
        dto.setEmail("cuidador@email.com");
        dto.setSenha("Senha@123");
        dto.setInstituicaoId(instituicao().getId());
        dto.setContato(contatoDTO());
        return dto;
    }
}
