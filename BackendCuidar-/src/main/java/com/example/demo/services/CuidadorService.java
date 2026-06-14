package com.example.demo.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.CuidadorDTO;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Cuidador;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mappers.CuidadorMapper;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.utils.TextoUtils;

@Service
public class CuidadorService {

    private final CuidadorRepository repository;
    private final IdosoRepository idosoRepository;
    private final PasswordEncoder passwordEncoder;
    private final SenhaService senhaService;

    public CuidadorService(
            CuidadorRepository repository,
            IdosoRepository idosoRepository,
            PasswordEncoder passwordEncoder,
            SenhaService senhaService) {
        this.repository = repository;
        this.idosoRepository = idosoRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaService = senhaService;
    }

    public Page<CuidadorDTO> listarAtivos(Pageable pageable) {
        return repository.findByStatus(Status.ATIVO, pageable).map(CuidadorMapper::toDTO);
    }

    public CuidadorDTO buscarPorId(Integer id) {
        Cuidador cuidador = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador", id.longValue()));

        return CuidadorMapper.toDTO(cuidador);
    }

    public CuidadorDTO criar(CuidadorDTO dto) {
        String cpfLimpo = limparDocumento(dto.getCpf());
        validarCpfDisponivel(cpfLimpo);

        if (dto.getContato() == null) {
            throw new InvalidRequestException("O contato do cuidador deve ser informado");
        }

        Cuidador cuidador = CuidadorMapper.toEntity(dto);
        senhaService.validar(dto.getSenha());
        cuidador.setSenha(passwordEncoder.encode(dto.getSenha()));

        return CuidadorMapper.toDTO(repository.save(cuidador));
    }

    public CuidadorDTO atualizar(Integer id, CuidadorDTO dto) {
        Cuidador cuidador = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador", id.longValue()));

        String cpfLimpo = limparDocumento(dto.getCpf());
        if (!cuidador.getCpf().equals(cpfLimpo)) {
            validarCpfDisponivel(cpfLimpo);
        }

        cuidador.setNome(TextoUtils.paraBanco(dto.getNome()));
        cuidador.setCpf(cpfLimpo);
        cuidador.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            senhaService.validar(dto.getSenha());
            cuidador.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        cuidador.setData_atualizacao(LocalDateTime.now());

        if (dto.getContato() != null) {
            aplicarContato(cuidador, dto);
        }

        return CuidadorMapper.toDTO(repository.save(cuidador));
    }

    public CuidadorDTO reativar(Integer id, CuidadorDTO dto) {
        Cuidador cuidador = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador", id.longValue()));

        aplicarCamposEnviados(cuidador, dto);
        cuidador.setStatus(Status.ATIVO);
        cuidador.setData_atualizacao(LocalDateTime.now());

        return CuidadorMapper.toDTO(repository.save(cuidador));
    }

    public void inativar(Integer id) {
        Cuidador cuidador = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador", id.longValue()));

        cuidador.setStatus(Status.INATIVO);
        cuidador.setData_atualizacao(LocalDateTime.now());
        repository.save(cuidador);
    }

    private void aplicarCamposEnviados(Cuidador cuidador, CuidadorDTO dto) {
        if (dto == null) {
            return;
        }

        if (dto.getNome() != null) {
            cuidador.setNome(TextoUtils.paraBanco(dto.getNome()));
        }

        String cpfLimpo = limparDocumento(dto.getCpf());
        if (cpfLimpo != null && !cpfLimpo.equals(cuidador.getCpf())) {
            validarCpfDisponivel(cpfLimpo);
            cuidador.setCpf(cpfLimpo);
        }

        if (dto.getEmail() != null) {
            cuidador.setEmail(dto.getEmail());
        }

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            senhaService.validar(dto.getSenha());
            cuidador.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        if (dto.getContato() != null) {
            aplicarContato(cuidador, dto);
        }
    }

    private void aplicarContato(Cuidador cuidador, CuidadorDTO dto) {
        Contato contato = cuidador.getContato();
        if (contato == null) {
            contato = new Contato();
            contato.setCuidador(cuidador);
            cuidador.setContato(contato);
        }

        if (dto.getContato().getDdd() != null) {
            contato.setDdd(TextoUtils.limparNumero(dto.getContato().getDdd()));
        }
        if (dto.getContato().getTelefone() != null) {
            contato.setTelefone(TextoUtils.limparNumero(dto.getContato().getTelefone()));
        }
    }

    private String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }

    private void validarCpfDisponivel(String cpf) {
        if (repository.existsByCpf(cpf) || idosoRepository.existsByCpf(cpf)) {
            throw new DuplicateResourceException("CPF ja esta em uso");
        }
    }
}
