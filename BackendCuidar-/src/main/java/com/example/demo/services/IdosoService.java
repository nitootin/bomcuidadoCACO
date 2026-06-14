package com.example.demo.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.ContatoDTO;
import com.example.demo.dtos.IdosoDTO;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Instituicao;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.ContatoMapper;
import com.example.demo.mappers.IdosoMapper;
import com.example.demo.repository.ContatoRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.InstituicaoRepository;
import com.example.demo.repository.VinculoRepository;

@Service
public class IdosoService {

    private final IdosoRepository repository;
    private final CuidadorRepository cuidadorRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final ContatoRepository contatoRepository;
    private final VinculoRepository vinculoRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKeySpec chaveCriptografia;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final String ALGORITMO_CRIPTOGRAFIA = "AES/GCM/NoPadding";
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG = 128;

    public IdosoService(
            IdosoRepository repository,
            CuidadorRepository cuidadorRepository,
            InstituicaoRepository instituicaoRepository,
            ContatoRepository contatoRepository,
            VinculoRepository vinculoRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.crypto.secret:${jwt.secret}}") String segredoCriptografia) {
        this.repository = repository;
        this.cuidadorRepository = cuidadorRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.contatoRepository = contatoRepository;
        this.vinculoRepository = vinculoRepository;
        this.passwordEncoder = passwordEncoder;
        this.chaveCriptografia = new SecretKeySpec(gerarChaveCriptografia(segredoCriptografia), "AES");
    }

    public Page<IdosoDTO> listarAtivos(Pageable pageable) {
        return repository.findByStatus(Status.ATIVO, pageable).map(IdosoMapper::toDTO);
    }

    public Page<IdosoDTO> listarAtivosPorInstituicao(Integer instituicaoId, Pageable pageable) {
        return repository.findByStatusAndInstituicaoId(Status.ATIVO, instituicaoId, pageable)
                .map(IdosoMapper::toDTO);
    }

    public IdosoDTO buscarPorId(Integer id) {
        Idoso idoso = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", id.longValue()));

        return IdosoMapper.toDTO(idoso);
    }

    public IdosoDTO buscarPorCpf(String cpf) {
        Idoso idoso = buscarEntidadePorCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso não encontrado com CPF informado"));

        return IdosoMapper.toDTO(idoso);
    }

    public IdosoDTO criar(IdosoDTO dto) {
        String cpfLimpo = limparDocumento(dto.getCpf());
        if (cuidadorRepository.existsByCpf(cpfLimpo)) {
            throw new DuplicateResourceException("CPF já está em uso");
        }

        Optional<Idoso> idosoExistente = buscarEntidadePorCpf(cpfLimpo);

        if (idosoExistente.isPresent() && idosoExistente.get().getStatus() == Status.ATIVO) {
            throw new DuplicateResourceException("Já existe um idoso ativo com esse CPF");
        }

        Instituicao instituicao = instituicaoRepository.findById(dto.getInstituicaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", dto.getInstituicaoId().longValue()));

        Contato contato = resolverContato(dto);

        if (idosoExistente.isPresent()) {
            Idoso idoso = idosoExistente.get();
            IdosoMapper.atualizarIdoso(idoso, dto, instituicao);
            idoso.setContato(contato);
            idoso.setStatus(Status.ATIVO);
            idoso.setData_atualizacao(LocalDateTime.now());
            return IdosoMapper.toDTO(repository.save(idoso));
        }

        Idoso idoso = IdosoMapper.toEntity(dto);
        idoso.setInstituicao(instituicao);
        idoso.setContato(contato);

        return IdosoMapper.toDTO(repository.save(idoso));
    }

    public IdosoDTO atualizar(Integer id, IdosoDTO dto) {
        Idoso idoso = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", id.longValue()));

        String cpfLimpo = limparDocumento(dto.getCpf());
        if (!idoso.getCpf().equals(cpfLimpo)) {
            if (repository.existsByCpf(cpfLimpo) || cuidadorRepository.existsByCpf(cpfLimpo)) {
                throw new DuplicateResourceException("CPF já está em uso");
            }
        }

        Instituicao instituicao = instituicaoRepository.findById(dto.getInstituicaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", dto.getInstituicaoId().longValue()));

        Contato contato = null;
        ContatoDTO contatoDTO = dto.getContato();

        if (contatoDTO != null) {
            if (contatoDTO.getId() != null) {
                contato = contatoRepository.findById(contatoDTO.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contato", contatoDTO.getId().longValue()));
                ContatoMapper.atualizarContato(contato, contatoDTO, null, null);
                contato = contatoRepository.save(contato);
            } else {
                if (contatoDTO.getDdd() == null || contatoDTO.getTelefone() == null) {
                    throw new InvalidRequestException("Dados de contato incompletos");
                }
                contato = ContatoMapper.toEntity(contatoDTO, null, java.util.List.of());
                contato = contatoRepository.save(contato);
            }
        } else if (dto.getContatoId() != null) {
            contato = contatoRepository.findById(dto.getContatoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contato", dto.getContatoId().longValue()));
        }

        IdosoMapper.atualizarIdoso(idoso, dto, instituicao);
        if (contato != null) idoso.setContato(contato);
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

    public Idoso autenticarPorSenhaAcesso(String senhaAcesso) {
        if (senhaAcesso == null || senhaAcesso.isBlank()) {
            throw new InvalidRequestException("Senha de acesso é obrigatória.");
        }

        List<Idoso> idosos = repository.findBySenhaAcessoCriptografadaIsNotNull();

        for (Idoso idoso : idosos) {
            String senhaSalva = idoso.getSenhaAcessoCriptografada();
            if (senhaSalva == null || senhaSalva.isBlank()) {
                continue;
            }

            String senhaDescriptografada = descriptografarSenhaAcesso(senhaSalva);
            if (senhaAcesso.trim().equals(senhaDescriptografada)) {
                if (idoso.getStatus() != Status.ATIVO) {
                    throw new UnauthorizedException("Senha de acesso inválida.");
                }

                return idoso;
            }
        }

        throw new UnauthorizedException("Senha de acesso inválida.");
    }

    public Map<String, Object> obterSenhaAcesso(Integer idosoId, Integer cuidadorId, String senhaCuidador) {
        if (senhaCuidador == null || senhaCuidador.isBlank()) {
            throw new InvalidRequestException("Informe a senha do cuidador");
        }

        Cuidador cuidador = cuidadorRepository.findById(cuidadorId)
                .orElseThrow(() -> new UnauthorizedException("Cuidador nao encontrado"));

        if (!passwordEncoder.matches(senhaCuidador, cuidador.getSenha())) {
            throw new UnauthorizedException("Senha do cuidador invalida");
        }

        Idoso idoso = repository.findById(idosoId)
                .orElseThrow(() -> new ResourceNotFoundException("Idoso", idosoId.longValue()));

        if (!vinculoRepository.existsByIdosoIdAndCuidadorId(idosoId, cuidadorId)) {
            throw new UnauthorizedException("Voce nao tem permissao para acessar este idoso");
        }

        boolean geradaAgora = false;
        if (idoso.getSenhaAcessoCriptografada() == null || idoso.getSenhaAcessoCriptografada().isBlank()) {
            String senha = gerarSenhaAcesso();
            idoso.setSenhaAcessoCriptografada(criptografarSenhaAcesso(senha));
            idoso.setData_atualizacao(LocalDateTime.now());
            repository.save(idoso);
            geradaAgora = true;

            return Map.of(
                    "idosoId", idoso.getId(),
                    "senha", senha,
                    "gerada", geradaAgora);
        }

        return Map.of(
                "idosoId", idoso.getId(),
                "senha", descriptografarSenhaAcesso(idoso.getSenhaAcessoCriptografada()),
                "gerada", geradaAgora);
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

        throw new InvalidRequestException("Contato é obrigatório");
    }

    private Optional<Idoso> buscarEntidadePorCpf(String cpf) {
        String cpfLimpo = limparDocumento(cpf);
        return cpfLimpo == null ? Optional.empty() : repository.findByCpf(cpfLimpo);
    }

    private String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }

    private String gerarSenhaAcesso() {
        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder senha = new StringBuilder("BC-");

        for (int i = 0; i < 8; i++) {
            senha.append(caracteres.charAt(secureRandom.nextInt(caracteres.length())));
        }

        return senha.toString();
    }

    private String criptografarSenhaAcesso(String senha) {
        try {
            byte[] iv = new byte[TAMANHO_IV];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO_CRIPTOGRAFIA);
            cipher.init(Cipher.ENCRYPT_MODE, chaveCriptografia, new GCMParameterSpec(TAMANHO_TAG, iv));
            byte[] criptografado = cipher.doFinal(senha.getBytes(StandardCharsets.UTF_8));

            byte[] resultado = new byte[iv.length + criptografado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(criptografado, 0, resultado, iv.length, criptografado.length);

            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception ex) {
            throw new InvalidRequestException("Nao foi possivel proteger a senha de acesso");
        }
    }

    private String descriptografarSenhaAcesso(String senhaCriptografada) {
        try {
            byte[] conteudo = Base64.getDecoder().decode(senhaCriptografada);
            byte[] iv = Arrays.copyOfRange(conteudo, 0, TAMANHO_IV);
            byte[] criptografado = Arrays.copyOfRange(conteudo, TAMANHO_IV, conteudo.length);

            Cipher cipher = Cipher.getInstance(ALGORITMO_CRIPTOGRAFIA);
            cipher.init(Cipher.DECRYPT_MODE, chaveCriptografia, new GCMParameterSpec(TAMANHO_TAG, iv));

            return new String(cipher.doFinal(criptografado), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new InvalidRequestException("Nao foi possivel recuperar a senha de acesso");
        }
    }

    private byte[] gerarChaveCriptografia(String segredo) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(segredo.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new InvalidRequestException("Nao foi possivel configurar a criptografia");
        }
    }
}
