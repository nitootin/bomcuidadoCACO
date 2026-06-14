package com.example.demo.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.AlertasDTO;
import com.example.demo.entity.Alertas;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Prescricao;
import com.example.demo.enums.StatusAlertas;
import com.example.demo.enums.TipoAlerta;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.AlertasMapper;
import com.example.demo.repository.AlertasRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.PrescricaoRepository;
import com.example.demo.repository.VinculoRepository;

@Service
public class AlertasService {

    private final AlertasRepository repository;
    private final IdosoRepository idosoRepository;
    private final PrescricaoRepository prescricaoRepository;
    private final VinculoRepository vinculoRepository;

    public AlertasService(
            AlertasRepository repository,
            IdosoRepository idosoRepository,
            PrescricaoRepository prescricaoRepository,
            VinculoRepository vinculoRepository) {
        this.repository = repository;
        this.idosoRepository = idosoRepository;
        this.prescricaoRepository = prescricaoRepository;
        this.vinculoRepository = vinculoRepository;
    }

    public Page<AlertasDTO> listarAtivosDoCuidador(Integer cuidadorId, Pageable pageable) {
        validarCuidador(cuidadorId);
        return repository.findNaoCanceladosPorCuidador(cuidadorId, StatusAlertas.CANCELADO, pageable)
                .map(AlertasMapper::toDTO);
    }

    public Page<AlertasDTO> listarPorIdoso(Integer idosoId, Integer cuidadorId, Pageable pageable) {
        validarCuidador(cuidadorId);
        validarIdosoVinculado(idosoId, cuidadorId);
        return repository.findNaoCanceladosPorIdosoECuidador(idosoId, cuidadorId, StatusAlertas.CANCELADO, pageable)
                .map(AlertasMapper::toDTO);
    }

    public Page<AlertasDTO> listarDoIdoso(Integer idosoId, Pageable pageable) {
        validarIdoso(idosoId);
        return repository.findByIdosoIdAndStatusAlertasNot(idosoId, StatusAlertas.CANCELADO, pageable)
                .map(AlertasMapper::toDTO);
    }

    public AlertasDTO buscarPorId(int id, Integer cuidadorId) {
        validarCuidador(cuidadorId);
        Alertas alerta = buscarAlerta(id);
        validarIdosoVinculado(alerta.getIdoso().getId(), cuidadorId);
        return AlertasMapper.toDTO(alerta);
    }

    public AlertasDTO criar(AlertasDTO dto, Integer cuidadorId) {
        validarCuidador(cuidadorId);
        validar(dto);

        Idoso idoso = buscarIdoso(dto.getIdosoId());
        validarIdosoVinculado(idoso.getId(), cuidadorId);
        Prescricao prescricao = buscarPrescricaoParaAlerta(dto, idoso);

        return AlertasMapper.toDTO(repository.save(AlertasMapper.toEntity(dto, idoso, prescricao)));
    }

    public AlertasDTO atualizar(int id, AlertasDTO dto, Integer cuidadorId) {
        validarCuidador(cuidadorId);
        validar(dto);

        Alertas alerta = buscarAlerta(id);
        validarIdosoVinculado(alerta.getIdoso().getId(), cuidadorId);

        Idoso idoso = buscarIdoso(dto.getIdosoId());
        validarIdosoVinculado(idoso.getId(), cuidadorId);
        Prescricao prescricao = buscarPrescricaoParaAlerta(dto, idoso);

        AlertasMapper.updateEntity(alerta, dto, idoso, prescricao);
        return AlertasMapper.toDTO(repository.save(alerta));
    }

    public void cancelar(int id, Integer cuidadorId) {
        validarCuidador(cuidadorId);

        Alertas alerta = buscarAlerta(id);
        validarIdosoVinculado(alerta.getIdoso().getId(), cuidadorId);

        alerta.setStatusAlertas(StatusAlertas.CANCELADO);
        alerta.setData_atualizacao(LocalDateTime.now());
        repository.save(alerta);
    }

    public AlertasDTO confirmar(int id, Integer idosoId) {
        validarIdoso(idosoId);

        Alertas alerta = buscarAlerta(id);
        if (alerta.getIdoso() == null || alerta.getIdoso().getId() != idosoId) {
            throw new UnauthorizedException("Alerta nao pertence ao idoso autenticado");
        }

        if (alerta.getStatusAlertas() == StatusAlertas.CANCELADO) {
            throw new InvalidRequestException("Alerta cancelado nao pode ser confirmado");
        }

        alerta.setStatusAlertas(StatusAlertas.REALIZADO);
        alerta.setData_atualizacao(LocalDateTime.now());
        return AlertasMapper.toDTO(repository.save(alerta));
    }

    private Alertas buscarAlerta(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", (long) id));
    }

    private Idoso buscarIdoso(Integer id) {
        return idosoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", id.longValue()));
    }

    private Prescricao buscarPrescricaoParaAlerta(AlertasDTO dto, Idoso idoso) {
        if (dto.getTipoAlerta() != TipoAlerta.REMEDIO) {
            return null;
        }

        if (dto.getPrescricaoId() == null) {
            throw new InvalidRequestException("Prescricao e obrigatoria para alerta de remedio");
        }

        Prescricao prescricao = prescricaoRepository.findById(dto.getPrescricaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Prescricao", dto.getPrescricaoId().longValue()));

        if (prescricao.getIdoso() == null || prescricao.getIdoso().getId() != idoso.getId()) {
            throw new InvalidRequestException("Prescricao nao pertence ao idoso informado");
        }

        return prescricao;
    }

    private void validar(AlertasDTO dto) {
        if (dto == null) {
            throw new InvalidRequestException("Dados do alerta nao informados");
        }

        if (dto.getIdosoId() == null) {
            throw new InvalidRequestException("Idoso e obrigatorio");
        }

        if (dto.getTipoAlerta() == null) {
            throw new InvalidRequestException("Tipo do alerta e obrigatorio");
        }

        if (dto.getDataAgendada() == null) {
            throw new InvalidRequestException("Data agendada e obrigatoria");
        }
    }

    private void validarCuidador(Integer cuidadorId) {
        if (cuidadorId == null) {
            throw new UnauthorizedException("Cuidador autenticado nao identificado");
        }
    }

    private void validarIdoso(Integer idosoId) {
        if (idosoId == null) {
            throw new UnauthorizedException("Idoso autenticado nao identificado");
        }
    }

    private void validarIdosoVinculado(Integer idosoId, Integer cuidadorId) {
        if (!vinculoRepository.existsByIdosoIdAndCuidadorId(idosoId, cuidadorId)) {
            throw new UnauthorizedException("Cuidador nao possui vinculo com o idoso informado");
        }
    }
}
