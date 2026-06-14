package com.example.demo.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.ContatoDTO;
import com.example.demo.dtos.IdosoDTO;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Vinculo;
import com.example.demo.enums.Status;
import com.example.demo.enums.TipoVinculo;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mappers.ContatoMapper;
import com.example.demo.mappers.IdosoMapper;
import com.example.demo.repository.ContatoRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.VinculoRepository;

@Service
public class IdosoService {

    private final IdosoRepository repository;
    private final CuidadorRepository cuidadorRepository;
    private final ContatoRepository contatoRepository;
    private final VinculoRepository vinculoRepository;

    public IdosoService(
            IdosoRepository repository,
            CuidadorRepository cuidadorRepository,
            ContatoRepository contatoRepository,
            VinculoRepository vinculoRepository) {
        this.repository = repository;
        this.cuidadorRepository = cuidadorRepository;
        this.contatoRepository = contatoRepository;
        this.vinculoRepository = vinculoRepository;
    }

    public Page<IdosoDTO> listarAtivos(Pageable pageable) {
        return repository.findByStatus(Status.ATIVO, pageable).map(IdosoMapper::toDTO);
    }

    public IdosoDTO buscarPorId(Integer id) {
        Idoso idoso = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", id.longValue()));

        return IdosoMapper.toDTO(idoso);
    }

    public IdosoDTO buscarPorCpf(String cpf) {
        Idoso idoso = buscarEntidadePorCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso nao encontrado com CPF informado"));

        return IdosoMapper.toDTO(idoso);
    }

    public IdosoDTO criar(IdosoDTO dto) {
        return criar(dto, null);
    }

    public IdosoDTO criar(IdosoDTO dto, Integer cuidadorId) {
        String cpfLimpo = limparDocumento(dto.getCpf());
        validarCpf(cpfLimpo);
        if (cuidadorRepository.existsByCpf(cpfLimpo)) {
            throw new DuplicateResourceException("CPF ja esta em uso");
        }

        Optional<Idoso> idosoExistente = buscarEntidadePorCpf(cpfLimpo);

        if (idosoExistente.isPresent() && idosoExistente.get().getStatus() == Status.ATIVO) {
            throw new DuplicateResourceException("Ja existe um idoso ativo com esse CPF");
        }

        Contato contato = resolverContato(dto);
        Idoso salvo;

        if (idosoExistente.isPresent()) {
            Idoso idoso = idosoExistente.get();
            IdosoMapper.atualizarIdoso(idoso, dto);
            idoso.setContato(contato);
            idoso.setStatus(Status.ATIVO);
            idoso.setData_atualizacao(LocalDateTime.now());
            salvo = repository.save(idoso);
        } else {
            Idoso idoso = IdosoMapper.toEntity(dto);
            idoso.setContato(contato);
            salvo = repository.save(idoso);
        }

        criarVinculoComCuidadorSeNecessario(salvo, cuidadorId);
        return IdosoMapper.toDTO(salvo);
    }

    public IdosoDTO atualizar(Integer id, IdosoDTO dto) {
        Idoso idoso = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", id.longValue()));

        String cpfLimpo = limparDocumento(dto.getCpf());
        validarCpf(cpfLimpo);
        if (!Objects.equals(idoso.getCpf(), cpfLimpo)) {
            if (repository.existsByCpf(cpfLimpo) || cuidadorRepository.existsByCpf(cpfLimpo)) {
                throw new DuplicateResourceException("CPF ja esta em uso");
            }
        }

        Contato contato = resolverContatoParaAtualizacao(dto);
        IdosoMapper.atualizarIdoso(idoso, dto);
        if (contato != null) {
            idoso.setContato(contato);
        }
        idoso.setData_atualizacao(LocalDateTime.now());

        return IdosoMapper.toDTO(repository.save(idoso));
    }

    public void inativar(Integer id) {
        Idoso idoso = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", id.longValue()));

        idoso.setStatus(Status.INATIVO);
        idoso.setData_atualizacao(LocalDateTime.now());
        repository.save(idoso);
    }

    private Contato resolverContato(IdosoDTO dto) {
        ContatoDTO contatoDTO = dto.getContato();

        if (contatoDTO != null) {
            if (contatoDTO.getDdd() == null || contatoDTO.getTelefone() == null) {
                throw new InvalidRequestException("Dados de contato incompletos");
            }
            Contato contato = ContatoMapper.toEntity(contatoDTO, null, java.util.List.of());
            return contatoRepository.save(contato);
        }

        if (dto.getContatoId() != null) {
            return contatoRepository.findById(dto.getContatoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contato", dto.getContatoId().longValue()));
        }

        throw new InvalidRequestException("Contato e obrigatorio");
    }

    private Contato resolverContatoParaAtualizacao(IdosoDTO dto) {
        ContatoDTO contatoDTO = dto.getContato();

        if (contatoDTO != null) {
            if (contatoDTO.getId() != null) {
                Contato contato = contatoRepository.findById(contatoDTO.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contato", contatoDTO.getId().longValue()));
                ContatoMapper.atualizarContato(contato, contatoDTO, null, null);
                return contatoRepository.save(contato);
            }

            if (contatoDTO.getDdd() == null || contatoDTO.getTelefone() == null) {
                throw new InvalidRequestException("Dados de contato incompletos");
            }
            return contatoRepository.save(ContatoMapper.toEntity(contatoDTO, null, java.util.List.of()));
        }

        if (dto.getContatoId() != null) {
            return contatoRepository.findById(dto.getContatoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contato", dto.getContatoId().longValue()));
        }

        return null;
    }

    private Optional<Idoso> buscarEntidadePorCpf(String cpf) {
        String cpfLimpo = limparDocumento(cpf);
        return cpfLimpo == null ? Optional.empty() : repository.findByCpf(cpfLimpo);
    }

    private void criarVinculoComCuidadorSeNecessario(Idoso idoso, Integer cuidadorId) {
        if (cuidadorId == null) {
            return;
        }

        Cuidador cuidador = cuidadorRepository.findById(cuidadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador", cuidadorId.longValue()));

        if (vinculoRepository.existsByIdosoIdAndCuidadorId(idoso.getId(), cuidador.getId())) {
            return;
        }

        Vinculo vinculo = new Vinculo();
        vinculo.setIdoso(idoso);
        vinculo.setCuidador(cuidador);
        vinculo.setDataCriacao(LocalDate.now());
        vinculo.setTipoVinculo(
                vinculoRepository.existsByIdosoId(idoso.getId()) ? TipoVinculo.PADRAO : TipoVinculo.EMERGENCIA);
        vinculoRepository.save(vinculo);
    }

    private String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }

    private void validarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new InvalidRequestException("CPF e obrigatorio");
        }
    }
}
