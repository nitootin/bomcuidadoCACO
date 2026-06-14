package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.example.demo.support.TestDataFactory.idoso;
import static com.example.demo.support.TestDataFactory.prescricao;
import static com.example.demo.support.TestDataFactory.prescricaoDTO;
import static com.example.demo.support.TestDataFactory.remedio;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dtos.PrescricaoDTO;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Prescricao;
import com.example.demo.entity.Remedio;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.PrescricaoRepository;
import com.example.demo.repository.RemedioRepository;

@ExtendWith(MockitoExtension.class)
class PrescricaoServiceTest {

    @Mock
    private PrescricaoRepository prescricaoRepository;

    @Mock
    private RemedioRepository remedioRepository;

    @Mock
    private IdosoRepository idosoRepository;

    @InjectMocks
    private PrescricaoService service;

    @Test
    void deveCriarPrescricao() {
        PrescricaoDTO dto = prescricaoDTO();
        Remedio remedio = remedio();
        Idoso idoso = idoso();
        Prescricao salva = prescricao(1, remedio, idoso, Status.ATIVO);

        when(remedioRepository.findById(10)).thenReturn(Optional.of(remedio));
        when(idosoRepository.findById(20)).thenReturn(Optional.of(idoso));
        when(prescricaoRepository.save(any(Prescricao.class))).thenReturn(salva);

        PrescricaoDTO resultado = service.criar(dto);

        assertEquals(1, resultado.getId());
        assertEquals(10, resultado.getRemedioId());
        assertEquals(20, resultado.getIdosoId());
        assertEquals("Dipirona", resultado.getRemedioNome());
        assertEquals("Maria", resultado.getIdosoNome());
        assertEquals(Status.ATIVO, resultado.getStatus());
    }

    @Test
    void deveAtualizarPrescricao() {
        PrescricaoDTO dto = prescricaoDTO();
        dto.setDosagem("2 comprimidos");
        Remedio remedio = remedio();
        Idoso idoso = idoso();
        Prescricao existente = prescricao(1, remedio, idoso, Status.ATIVO);

        when(prescricaoRepository.findById(1)).thenReturn(Optional.of(existente));
        when(remedioRepository.findById(10)).thenReturn(Optional.of(remedio));
        when(idosoRepository.findById(20)).thenReturn(Optional.of(idoso));
        when(prescricaoRepository.save(existente)).thenReturn(existente);

        PrescricaoDTO resultado = service.atualizar(1, dto);

        assertEquals("2 Comprimidos", resultado.getDosagem());
        verify(prescricaoRepository).save(existente);
    }

    @Test
    void deveInativarPrescricao() {
        Prescricao prescricao = prescricao(1, remedio(), idoso(), Status.ATIVO);

        when(prescricaoRepository.findById(1)).thenReturn(Optional.of(prescricao));

        service.inativar(1);

        ArgumentCaptor<Prescricao> captor = ArgumentCaptor.forClass(Prescricao.class);
        verify(prescricaoRepository).save(captor.capture());
        assertEquals(Status.INATIVO, captor.getValue().getStatus());
    }

    @Test
    void deveFalharAoCriarComDtoNulo() {
        assertThrows(InvalidRequestException.class, () -> service.criar(null));
    }

    @Test
    void deveFalharAoCriarSemRemedio() {
        PrescricaoDTO dto = prescricaoDTO();
        dto.setRemedioId(null);

        assertThrows(InvalidRequestException.class, () -> service.criar(dto));
    }

    @Test
    void deveFalharAoCriarSemIdoso() {
        PrescricaoDTO dto = prescricaoDTO();
        dto.setIdosoId(null);

        assertThrows(InvalidRequestException.class, () -> service.criar(dto));
    }

    @Test
    void deveFalharAoCriarSemDosagem() {
        PrescricaoDTO dto = prescricaoDTO();
        dto.setDosagem(" ");

        assertThrows(InvalidRequestException.class, () -> service.criar(dto));
    }

    @Test
    void deveFalharAoCriarComIntervaloInvalido() {
        PrescricaoDTO dto = prescricaoDTO();
        dto.setIntervalo(0.0);

        assertThrows(InvalidRequestException.class, () -> service.criar(dto));
    }

    @Test
    void deveFalharAoCriarComRemedioInexistente() {
        PrescricaoDTO dto = prescricaoDTO();

        when(remedioRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.criar(dto));
    }

    @Test
    void deveFalharAoCriarComIdosoInexistente() {
        PrescricaoDTO dto = prescricaoDTO();

        when(remedioRepository.findById(10)).thenReturn(Optional.of(remedio()));
        when(idosoRepository.findById(20)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.criar(dto));
    }

    @Test
    void deveFalharAoBuscarPrescricaoInexistente() {
        when(prescricaoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99));
    }
}
