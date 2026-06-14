package com.example.demo.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.ContatoDTO;
import com.example.demo.dtos.VinculoDTO;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Vinculo;
import com.example.demo.enums.TipoVinculo;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mappers.ContatoMapper;
import com.example.demo.mappers.VinculoMapper;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.VinculoRepository;

@Service
public class VinculoService {

    private final VinculoRepository repository;
    private final IdosoRepository idosoRepository;
    private final CuidadorRepository cuidadorRepository;

    public VinculoService(
            VinculoRepository repository,
            IdosoRepository idosoRepository,
            CuidadorRepository cuidadorRepository) {
        this.repository = repository;
        this.idosoRepository = idosoRepository;
        this.cuidadorRepository = cuidadorRepository;
    }

    public Page<VinculoDTO> listarTodos(Pageable pageable) {
        return repository.findAll(pageable).map(VinculoMapper::toDTO);
    }

    public Page<VinculoDTO> listarPorIdoso(Integer idosoId, Pageable pageable) {
        return repository.findByIdosoId(idosoId, pageable).map(VinculoMapper::toDTO);
    }

    public Page<VinculoDTO> listarPorCuidador(Integer cuidadorId, Pageable pageable) {
        return repository.findByCuidadorId(cuidadorId, pageable).map(VinculoMapper::toDTO);
    }

    public VinculoDTO buscarPorId(Integer id) {
        Vinculo vinculo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo", id.longValue()));
        return VinculoMapper.toDTO(vinculo);
    }

    public VinculoDTO criar(VinculoDTO dto) {
        if (dto.getIdosoId() == null || dto.getCuidadorId() == null) {
            throw new InvalidRequestException("Idoso e Cuidador são obrigatórios para criar um vínculo");
        }

        if (repository.existsByIdosoIdAndCuidadorId(dto.getIdosoId(), dto.getCuidadorId())) {
            throw new DuplicateResourceException("Já existe um vínculo entre este idoso e este cuidador");
        }

        boolean primeiroVinculo = !repository.existsByIdosoId(dto.getIdosoId());
        if (primeiroVinculo) {
            dto.setTipoVinculo(TipoVinculo.EMERGENCIA);
        } else if (TipoVinculo.EMERGENCIA.equals(dto.getTipoVinculo()) &&
                repository.existsByIdosoIdAndTipoVinculo(dto.getIdosoId(), TipoVinculo.EMERGENCIA)) {
            throw new DuplicateResourceException("Este idoso já possui um cuidador de emergência");
        }

        Idoso idoso = idosoRepository.findById(dto.getIdosoId())
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", dto.getIdosoId().longValue()));

        Cuidador cuidador = cuidadorRepository.findById(dto.getCuidadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador", dto.getCuidadorId().longValue()));

        Vinculo vinculo = VinculoMapper.toEntity(dto, idoso, cuidador);
        return VinculoMapper.toDTO(repository.save(vinculo));
    }

    @Transactional
    public VinculoDTO definirCuidadorEmergencia(Integer vinculoId) {
        Vinculo vinculo = repository.findById(vinculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo", vinculoId.longValue()));

        repository.findByIdosoIdAndTipoVinculo(vinculo.getIdoso().getId(), TipoVinculo.EMERGENCIA)
                .ifPresent(atual -> {
                    atual.setTipoVinculo(TipoVinculo.PADRAO);
                    repository.save(atual);
                });

        vinculo.setTipoVinculo(TipoVinculo.EMERGENCIA);
        return VinculoMapper.toDTO(repository.save(vinculo));
    }

    @Transactional(readOnly = true)
    public ContatoDTO buscarContatoDeEmergencia(Integer idosoId) {
        Vinculo vinculo = repository.findByIdosoIdAndTipoVinculo(idosoId, TipoVinculo.EMERGENCIA)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador de emergência para o idoso", idosoId.longValue()));

        Cuidador cuidador = vinculo.getCuidador();
        if (cuidador.getContato() == null) {
            throw new ResourceNotFoundException("Contato do cuidador de emergência", (long) cuidador.getId());
        }

        return ContatoMapper.toDTO(cuidador.getContato());
    }

    @Transactional
    public void deletar(Integer id) {
        Vinculo vinculo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo", id.longValue()));

        if (TipoVinculo.EMERGENCIA.equals(vinculo.getTipoVinculo())) {
            repository.findFirstByIdosoIdAndIdNot(vinculo.getIdoso().getId(), id)
                    .ifPresent(outro -> {
                        outro.setTipoVinculo(TipoVinculo.EMERGENCIA);
                        repository.save(outro);
                    });
        }

        repository.delete(vinculo);
    }
}
