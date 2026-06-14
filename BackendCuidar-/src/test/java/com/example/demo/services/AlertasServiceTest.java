package com.example.demo.services;

import static com.example.demo.support.TestDataFactory.alerta;
import static com.example.demo.support.TestDataFactory.alertaDTO;
import static com.example.demo.support.TestDataFactory.idoso;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dtos.AlertasDTO;
import com.example.demo.entity.Alertas;
import com.example.demo.entity.Idoso;
import com.example.demo.enums.StatusAlertas;
import com.example.demo.enums.TipoAlerta;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repository.AlertasRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.VinculoRepository;

@ExtendWith(MockitoExtension.class)
class AlertasServiceTest {

    @Mock
    private AlertasRepository alertasRepository;

    @Mock
    private IdosoRepository idosoRepository;

    @Mock
    private VinculoRepository vinculoRepository;

    @InjectMocks
    private AlertasService service;

    @Test
    void deveCriarAlertaParaIdosoVinculadoAoCuidador() {
        AlertasDTO dto = alertaDTO();
        Idoso idoso = idoso();
        Alertas salvo = alerta(1, idoso, StatusAlertas.AGENDADO);

        when(idosoRepository.findById(20)).thenReturn(Optional.of(idoso));
        when(vinculoRepository.existsByIdosoIdAndCuidadorId(20, 2)).thenReturn(true);
        when(alertasRepository.save(any(Alertas.class))).thenReturn(salvo);

        AlertasDTO resultado = service.criar(dto, 2);

        assertEquals(1, resultado.getId());
        assertEquals(20, resultado.getIdosoId());
        assertEquals("Maria", resultado.getIdosoNome());
        assertEquals(TipoAlerta.REMEDIO, resultado.getTipoAlerta());
        assertEquals(StatusAlertas.AGENDADO, resultado.getStatusAlertas());
    }

    @Test
    void deveFalharAoCriarAlertaParaIdosoNaoVinculadoAoCuidador() {
        AlertasDTO dto = alertaDTO();
        Idoso idoso = idoso();

        when(idosoRepository.findById(20)).thenReturn(Optional.of(idoso));
        when(vinculoRepository.existsByIdosoIdAndCuidadorId(20, 2)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.criar(dto, 2));
    }

    @Test
    void deveAtualizarAlertaDeIdosoVinculadoAoCuidador() {
        AlertasDTO dto = alertaDTO();
        dto.setTipoAlerta(TipoAlerta.CONSULTA);
        Idoso idoso = idoso();
        Alertas existente = alerta(1, idoso, StatusAlertas.AGENDADO);

        when(alertasRepository.findById(1)).thenReturn(Optional.of(existente));
        when(idosoRepository.findById(20)).thenReturn(Optional.of(idoso));
        when(vinculoRepository.existsByIdosoIdAndCuidadorId(20, 2)).thenReturn(true);
        when(alertasRepository.save(existente)).thenReturn(existente);

        AlertasDTO resultado = service.atualizar(1, dto, 2);

        assertEquals(TipoAlerta.CONSULTA, resultado.getTipoAlerta());
        verify(alertasRepository).save(existente);
    }

    @Test
    void deveCancelarAlertaDeIdosoVinculadoAoCuidador() {
        Alertas alerta = alerta(1, idoso(), StatusAlertas.AGENDADO);

        when(alertasRepository.findById(1)).thenReturn(Optional.of(alerta));
        when(vinculoRepository.existsByIdosoIdAndCuidadorId(20, 2)).thenReturn(true);

        service.cancelar(1, 2);

        ArgumentCaptor<Alertas> captor = ArgumentCaptor.forClass(Alertas.class);
        verify(alertasRepository).save(captor.capture());
        assertEquals(StatusAlertas.CANCELADO, captor.getValue().getStatusAlertas());
    }

    @Test
    void deveFalharAoCriarComDtoNulo() {
        assertThrows(InvalidRequestException.class, () -> service.criar(null, 2));
    }

    @Test
    void deveFalharAoCriarSemIdoso() {
        AlertasDTO dto = alertaDTO();
        dto.setIdosoId(null);

        assertThrows(InvalidRequestException.class, () -> service.criar(dto, 2));
    }

    @Test
    void deveFalharAoCriarSemTipoAlerta() {
        AlertasDTO dto = alertaDTO();
        dto.setTipoAlerta(null);

        assertThrows(InvalidRequestException.class, () -> service.criar(dto, 2));
    }

    @Test
    void deveFalharAoCriarSemDataAgendada() {
        AlertasDTO dto = alertaDTO();
        dto.setDataAgendada(null);

        assertThrows(InvalidRequestException.class, () -> service.criar(dto, 2));
    }

    @Test
    void deveFalharAoCriarComIdosoInexistente() {
        AlertasDTO dto = alertaDTO();

        when(idosoRepository.findById(20)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.criar(dto, 2));
    }

    @Test
    void deveFalharAoBuscarAlertaInexistente() {
        when(alertasRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99, 2));
    }

    @Test
    void deveFalharSemCuidadorAutenticado() {
        assertThrows(UnauthorizedException.class, () -> service.criar(alertaDTO(), null));
    }
}
