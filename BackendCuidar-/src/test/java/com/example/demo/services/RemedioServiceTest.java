package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.example.demo.support.TestDataFactory.cuidador;
import static com.example.demo.support.TestDataFactory.remedio;
import static com.example.demo.support.TestDataFactory.remedioDTO;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dtos.RemedioDTO;
import com.example.demo.entity.Prescricao;
import com.example.demo.entity.Remedio;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.PrescricaoRepository;
import com.example.demo.repository.RemedioRepository;

@ExtendWith(MockitoExtension.class)
class RemedioServiceTest {

    private static final Integer CUIDADOR_ID = 2;

    @Mock
    private RemedioRepository remedioRepository;

    @Mock
    private PrescricaoRepository prescricaoRepository;

    @Mock
    private CuidadorRepository cuidadorRepository;

    @InjectMocks
    private RemedioService service;

    @Test
    void deveCriarRemedioNovo() {
        RemedioDTO dto = remedioDTO("Dipirona", "Tomar com agua", null);
        Remedio salvo = remedio(1, "DIPIRONA", "TOMAR COM AGUA", Status.ATIVO);

        when(remedioRepository.findByNomeAndCuidadorId("DIPIRONA", CUIDADOR_ID)).thenReturn(Optional.empty());
        when(cuidadorRepository.findById(CUIDADOR_ID)).thenReturn(Optional.of(cuidador()));
        when(remedioRepository.save(any(Remedio.class))).thenReturn(salvo);

        RemedioDTO resultado = service.criar(dto, CUIDADOR_ID);

        assertEquals(1, resultado.getId());
        assertEquals("Dipirona", resultado.getNome());
        assertEquals(Status.ATIVO, resultado.getStatus());
    }

    @Test
    void deveBloquearCriacaoDeRemedioAtivoDuplicado() {
        RemedioDTO dto = remedioDTO("Dipirona", null, null);
        Remedio existente = remedio(1, "DIPIRONA", null, Status.ATIVO);

        when(remedioRepository.findByNomeAndCuidadorId("DIPIRONA", CUIDADOR_ID)).thenReturn(Optional.of(existente));

        assertThrows(DuplicateResourceException.class, () -> service.criar(dto, CUIDADOR_ID));
    }

    @Test
    void deveReativarRemedioInativoAoCriarComMesmoNome() {
        RemedioDTO dto = remedioDTO("Dipirona", "Nova observação", null);
        Remedio existente = remedio(1, "DIPIRONA", "ANTIGA OBSERVACAO", Status.INATIVO);

        when(remedioRepository.findByNomeAndCuidadorId("DIPIRONA", CUIDADOR_ID)).thenReturn(Optional.of(existente));
        when(remedioRepository.save(existente)).thenReturn(existente);

        RemedioDTO resultado = service.criar(dto, CUIDADOR_ID);

        assertEquals(Status.ATIVO, resultado.getStatus());
        assertEquals("Nova observação", resultado.getObservacao());
        verify(remedioRepository).save(existente);
    }

    @Test
    void deveBuscarRemedioPorId() {
        Remedio remedio = remedio(1, "DIPIRONA", null, Status.ATIVO);

        when(remedioRepository.findByIdAndCuidadorId(1, CUIDADOR_ID)).thenReturn(Optional.of(remedio));

        RemedioDTO resultado = service.buscarPorId(1, CUIDADOR_ID);

        assertEquals(1, resultado.getId());
        assertEquals("Dipirona", resultado.getNome());
    }

    @Test
    void deveFalharAoBuscarRemedioInexistente() {
        when(remedioRepository.findByIdAndCuidadorId(99, CUIDADOR_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99, CUIDADOR_ID));
    }

    @Test
    void deveBloquearAtualizacaoComNomeJaEmUso() {
        RemedioDTO dto = remedioDTO("Paracetamol", null, Status.ATIVO);
        Remedio remedio = remedio(1, "DIPIRONA", null, Status.ATIVO);

        when(remedioRepository.findByIdAndCuidadorId(1, CUIDADOR_ID)).thenReturn(Optional.of(remedio));
        when(remedioRepository.existsByNomeAndCuidadorId("PARACETAMOL", CUIDADOR_ID)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.atualizar(1, dto, CUIDADOR_ID));
    }

    @Test
    void deveInativarRemedioEPrescricoesAtivas() {
        Remedio remedio = remedio(1, "Dipirona", null, Status.ATIVO);
        Prescricao prescricao = new Prescricao();
        prescricao.setStatus(Status.ATIVO);

        when(remedioRepository.findByIdAndCuidadorId(1, CUIDADOR_ID)).thenReturn(Optional.of(remedio));
        when(prescricaoRepository.findByRemedioIdAndStatus(1, Status.ATIVO)).thenReturn(List.of(prescricao));

        service.inativar(1, CUIDADOR_ID);

        ArgumentCaptor<Remedio> captor = ArgumentCaptor.forClass(Remedio.class);
        verify(remedioRepository).save(captor.capture());
        assertEquals(Status.INATIVO, captor.getValue().getStatus());
        assertEquals(Status.INATIVO, prescricao.getStatus());
    }
}
