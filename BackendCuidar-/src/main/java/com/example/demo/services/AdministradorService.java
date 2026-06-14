package com.example.demo.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.AdministradorDTO;
import com.example.demo.entity.Administrador;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mappers.AdministradorMapper;
import com.example.demo.repository.AdministradorRepository;

@Service
public class AdministradorService {

    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SenhaService senhaService;

    public AdministradorService(
            AdministradorRepository repository,
            PasswordEncoder passwordEncoder,
            SenhaService senhaService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.senhaService = senhaService;
    }

    public Page<AdministradorDTO> listarAtivos(Pageable pageable) {
        return repository.findByStatus(Status.ATIVO, pageable)
                .map(AdministradorMapper::toDTO);
    }

    public AdministradorDTO buscarPorId(Integer id) {
        Administrador administrador = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador", id.longValue()));

        return AdministradorMapper.toDTO(administrador);
    }

    public AdministradorDTO criar(AdministradorDTO dto) {
        String cpfLimpo = limparDocumento(dto.getCpf());
        if (repository.existsByCpf(cpfLimpo)) {
            throw new DuplicateResourceException("Já existe um administrador com esse CPF");
        }

        Administrador administrador = AdministradorMapper.toEntity(dto);
        senhaService.validar(dto.getSenha());
        administrador.setSenha(passwordEncoder.encode(dto.getSenha()));
        Administrador salvo = repository.save(administrador);

        return AdministradorMapper.toDTO(salvo);
    }

    public AdministradorDTO atualizar(Integer id, AdministradorDTO dto) {
        Administrador administrador = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador", id.longValue()));

        String cpfLimpo = limparDocumento(dto.getCpf());
        if (!administrador.getCpf().equals(cpfLimpo)
                && repository.existsByCpf(cpfLimpo)) {
            throw new DuplicateResourceException("CPF já está em uso");
        }

        AdministradorMapper.updateEntity(administrador, dto);
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            senhaService.validar(dto.getSenha());
            administrador.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        Administrador atualizado = repository.save(administrador);
        return AdministradorMapper.toDTO(atualizado);
    }

    public void inativar(Integer id) {
        Administrador administrador = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrador", id.longValue()));

        AdministradorMapper.inativarEntity(administrador);
        repository.save(administrador);
    }

    private String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }
}
