// InstituicaoService.java
package com.example.demo.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.InstituicaoDTO;
import com.example.demo.entity.Instituicao;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mappers.InstituicaoMapper;
import com.example.demo.repository.InstituicaoRepository;
import com.example.demo.utils.TextoUtils;

@Service
public class InstituicaoService {

    private final InstituicaoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SenhaService senhaService;

    public InstituicaoService(InstituicaoRepository repository, PasswordEncoder passwordEncoder, SenhaService senhaService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.senhaService = senhaService;
    }

    public Page<InstituicaoDTO> listarTodas(Pageable pageable) {
    return repository.findAll(pageable)
            .map(InstituicaoMapper::toDTO);
}

    public InstituicaoDTO buscarPorId(Integer id) {
        Instituicao instituicao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", id.longValue()));
        return InstituicaoMapper.toDTO(instituicao);
    }

    public InstituicaoDTO criar(InstituicaoDTO dto) {
        String cnpjLimpo = limparDocumento(dto.getCnpj());
        if (repository.existsByCnpj(cnpjLimpo)) {
            throw new DuplicateResourceException("Já existe uma instituição com esse CNPJ");
        }
        Instituicao instituicao = InstituicaoMapper.toEntity(dto);
        senhaService.validar(dto.getSenha());
        instituicao.setSenha(passwordEncoder.encode(dto.getSenha()));
        return InstituicaoMapper.toDTO(repository.save(instituicao));
    }

    public InstituicaoDTO atualizar(Integer id, InstituicaoDTO dto) {
        Instituicao instituicao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", id.longValue()));

        String cnpjLimpo = limparDocumento(dto.getCnpj());
        if (!instituicao.getCnpj().equals(cnpjLimpo) && repository.existsByCnpj(cnpjLimpo)) {
            throw new DuplicateResourceException("CNPJ já está em uso");
        }

        instituicao.setNome(TextoUtils.paraBanco(dto.getNome()));
        instituicao.setCnpj(cnpjLimpo);
        instituicao.setEmail(dto.getEmail());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            senhaService.validar(dto.getSenha());
            instituicao.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        instituicao.setBairro(TextoUtils.paraBanco(dto.getBairro()));
        instituicao.setUf(TextoUtils.paraBanco(dto.getUf()));
        instituicao.setNumero(dto.getNumero());
        instituicao.setCep(limparDocumento(dto.getCep()));
        instituicao.setData_atualizacao(LocalDateTime.now());

        return InstituicaoMapper.toDTO(repository.save(instituicao));
    }

    public void inativar(Integer id) {
        Instituicao instituicao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", id.longValue()));
        instituicao.setStatus(Status.INATIVO);
        instituicao.setData_atualizacao(LocalDateTime.now());
        repository.save(instituicao);
    }

    public void ativar(Integer id) {
    Instituicao instituicao = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Instituição", id.longValue()));

    instituicao.setStatus(Status.ATIVO);
    instituicao.setData_atualizacao(LocalDateTime.now());

    repository.save(instituicao);
}

    private String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }
}
