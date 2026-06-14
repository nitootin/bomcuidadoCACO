package com.example.demo.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.PrescricaoDTO;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Prescricao;
import com.example.demo.entity.Remedio;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mappers.PrescricaoMapper;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.PrescricaoRepository;
import com.example.demo.repository.RemedioRepository;

@Service
public class PrescricaoService {

    private final PrescricaoRepository repository;
    private final RemedioRepository remedioRepository;
    private final IdosoRepository idosoRepository;

    public PrescricaoService(
            PrescricaoRepository repository,
            RemedioRepository remedioRepository,
            IdosoRepository idosoRepository) {
        this.repository = repository;
        this.remedioRepository = remedioRepository;
        this.idosoRepository = idosoRepository;
    }

    public Page<PrescricaoDTO> listarAtivas(Pageable pageable) {
        return repository.findAtivasNaoVencidas(Status.ATIVO, LocalDateTime.now(), pageable).map(PrescricaoMapper::toDTO);
    }

    public Page<PrescricaoDTO> listarPorIdoso(Integer idosoId, Pageable pageable) {
        return repository.findAtivasNaoVencidasPorIdoso(idosoId, Status.ATIVO, LocalDateTime.now(), pageable).map(PrescricaoMapper::toDTO);
    }

    public PrescricaoDTO buscarPorId(int id) {
        Prescricao prescricao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescricao", (long) id));
        return PrescricaoMapper.toDTO(prescricao);
    }

    public PrescricaoDTO criar(PrescricaoDTO dto) {
        validar(dto);

        Remedio remedio = buscarRemedio(dto.getRemedioId());
        Idoso idoso = buscarIdoso(dto.getIdosoId());

        return PrescricaoMapper.toDTO(repository.save(PrescricaoMapper.toEntity(dto, remedio, idoso)));
    }

    public PrescricaoDTO atualizar(int id, PrescricaoDTO dto) {
        validar(dto);

        Prescricao prescricao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescricao", (long) id));
        Remedio remedio = buscarRemedio(dto.getRemedioId());
        Idoso idoso = buscarIdoso(dto.getIdosoId());

        PrescricaoMapper.updateEntity(prescricao, dto, remedio, idoso);
        return PrescricaoMapper.toDTO(repository.save(prescricao));
    }

    public void inativar(int id) {
        Prescricao prescricao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescricao", (long) id));
        prescricao.setStatus(Status.INATIVO);
        repository.save(prescricao);
    }

    private Remedio buscarRemedio(Integer id) {
        return remedioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remedio", id.longValue()));
    }

    private Idoso buscarIdoso(Integer id) {
        return idosoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", id.longValue()));
    }

    private void validar(PrescricaoDTO dto) {
        if (dto == null) {
            throw new InvalidRequestException("Dados da prescricao nao informados");
        }

        if (dto.getRemedioId() == null) {
            throw new InvalidRequestException("Remedio e obrigatorio");
        }

        if (dto.getIdosoId() == null) {
            throw new InvalidRequestException("Idoso e obrigatorio");
        }

        if (dto.getDosagem() == null || dto.getDosagem().isBlank()) {
            throw new InvalidRequestException("Dosagem e obrigatoria");
        }

        if (dto.getIntervalo() == null || dto.getIntervalo() <= 0) {
            throw new InvalidRequestException("Intervalo deve ser maior que zero");
        }
    }
}
