package com.example.demo.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Administrador;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Instituicao;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.exceptions.UnsupportedProfileException;
import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.InstituicaoRepository;
import com.example.demo.security.JwtService;

@Service
public class AuthService {

    private final AdministradorRepository administradorRepository;
    private final CuidadorRepository cuidadorRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final IdosoService idosoService;

    private final TwoFactorService twoFactorService;

    private final SenhaService senhaService;

    public AuthService(
            AdministradorRepository administradorRepository,
            CuidadorRepository cuidadorRepository,
            InstituicaoRepository instituicaoRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TwoFactorService twoFactorService,
            SenhaService senhaService,
            IdosoService idosoService) {
        this.administradorRepository = administradorRepository;
        this.cuidadorRepository = cuidadorRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.twoFactorService = twoFactorService;
        this.senhaService = senhaService;
        this.idosoService = idosoService;
    }

    public Map<String, Object> login(Map<String, String> dados) {
        if (dados == null) {
            throw new InvalidRequestException("Dados de login não informados");
        }

        String identificador = primeiroValor(dados, "identificador", "cpfCnpj", "cpf", "cnpj");
        String senha = primeiroValor(dados, "senha", "password");
        Perfil perfil = parsePerfil(dados.get("perfil"));

        if (identificador == null || identificador.isBlank()) {
            throw new InvalidRequestException("Informe CPF ou CNPJ");
        }

        if (senha == null || senha.isBlank()) {
            throw new InvalidRequestException("Informe a senha");
        }

        Usuario usuario = buscarUsuario(perfil, identificador);
        String senhaSalva = senhaDoUsuario(usuario);

        if (senhaSalva == null || !passwordEncoder.matches(senha, senhaSalva)) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        if (usuario.getStatus() != Status.ATIVO) {
            throw new UnauthorizedException("Usuário inativo");
        }

        if (perfil == Perfil.CUIDADOR || perfil == Perfil.INSTITUICAO) {
            String email = emailDoUsuario(usuario);
            twoFactorService.enviarCodigo(email);

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("requer2fa", true);
            resposta.put("email", mascararEmail(email));
            return resposta;
        }

        return gerarRespostaLogin(usuario);
    }

    public Map<String, Object> verificar2fa(String identificador, String codigo, String perfil) {
        Usuario usuario = buscarUsuario(parsePerfil(perfil), identificador);
        String email = emailDoUsuario(usuario);
        twoFactorService.validarCodigo(email, codigo);
        return gerarRespostaLogin(usuario);
    }

    public Map<String, Object> loginIdoso(Map<String, String> dados) {
        if (dados == null) {
            throw new InvalidRequestException("Dados de login nao informados");
        }

        String senhaAcesso = dados.get("senhaAcesso");
        if (senhaAcesso == null || senhaAcesso.isBlank()) {
            throw new InvalidRequestException("Senha de acesso é obrigatória.");
        }

        return gerarRespostaLogin(idosoService.autenticarPorSenhaAcesso(senhaAcesso));
    }

    private String emailDoUsuario(Usuario usuario) {
        if (usuario instanceof Cuidador cuidador) return cuidador.getEmail();
        if (usuario instanceof Instituicao instituicao) return instituicao.getEmail();
        throw new UnsupportedProfileException("Perfil nao suporta 2FA");
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return cuidadorRepository.findByEmail(email)
                .map(u -> (Usuario) u)
                .or(() -> instituicaoRepository.findByEmail(email).map(u -> (Usuario) u))
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    }

    private Usuario buscarUsuarioPorEmail(String email, Perfil perfil) {
        return switch (perfil) {
            case CUIDADOR -> cuidadorRepository.findByEmail(email)
                    .map(u -> (Usuario) u)
                    .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
            case INSTITUICAO -> instituicaoRepository.findByEmail(email)
                    .map(u -> (Usuario) u)
                    .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
            default -> throw new UnsupportedProfileException("Perfil nao suporta 2FA");
        };
    }

    private Map<String, Object> gerarRespostaLogin(Usuario usuario) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("id", usuario.getId());
        resposta.put("nome", usuario.getNome());
        resposta.put("perfil", usuario.getPerfil());
        resposta.put("token", jwtService.gerarToken(usuario));
        resposta.put("tipo", "Bearer");
        resposta.put("autenticado", true);

        if (usuario instanceof Cuidador c) resposta.put("email", c.getEmail());
        else if (usuario instanceof Instituicao i) resposta.put("email", i.getEmail());

        return resposta;
    }

    private String mascararEmail(String email) {
        int at = email.indexOf("@");
        if (at <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    private Usuario buscarUsuario(Perfil perfil, String identificador) {
        String documento = limparDocumento(identificador);

        return switch (perfil) {
            case ADMINISTRADOR -> administradorRepository.findByCpf(documento)
                    .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
            case CUIDADOR -> cuidadorRepository.findByCpf(documento)
                    .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
            case INSTITUICAO -> instituicaoRepository.findByCnpj(documento)
                    .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
            default -> throw new UnsupportedProfileException("Perfil nao permitido para login");
        };
    }

    private String senhaDoUsuario(Usuario usuario) {
        if (usuario instanceof Administrador administrador) return administrador.getSenha();
        if (usuario instanceof Cuidador cuidador) return cuidador.getSenha();
        if (usuario instanceof Instituicao instituicao) return instituicao.getSenha();

        throw new UnsupportedProfileException("Perfil nao permitido para login");
    }

    private Perfil parsePerfil(String perfil) {
        if (perfil == null || perfil.isBlank()) {
            throw new InvalidRequestException("Informe o perfil");
        }

        try {
            return Perfil.valueOf(perfil.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Perfil inválido");
        }
    }

    private String primeiroValor(Map<String, String> dados, String... chaves) {
        for (String chave : chaves) {
            String valor = dados.get(chave);
            if (valor != null && !valor.isBlank()) return valor;
        }
        return null;
    }

    private String limparDocumento(String valor) {
        return valor.replaceAll("\\D", "");
    }

    public Map<String, Object> recuperarSenha(String identificador) {
    if (identificador == null || identificador.isBlank()) {
        throw new InvalidRequestException("Informe o CPF ou CNPJ");
    }

    String documento = limparDocumento(identificador);

    Usuario usuario = cuidadorRepository.findByCpf(documento)
            .map(u -> (Usuario) u)
            .or(() -> instituicaoRepository.findByCnpj(documento).map(u -> (Usuario) u))
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com esse CPF ou CNPJ"));

    String email = emailDoUsuario(usuario);
    twoFactorService.enviarCodigo(email);

    Map<String, Object> resposta = new HashMap<>();
    resposta.put("email", mascararEmail(email));
    return resposta;
}

public Map<String, Object> verificarRecuperacao(String email, String codigo) {
    twoFactorService.validarCodigo(email, codigo);

    Map<String, Object> resposta = new HashMap<>();
    resposta.put("valido", true);
    resposta.put("email", email);
    return resposta;
}

public void novaSenha(String email, String novaSenha) {
    senhaService.validar(novaSenha);

    Usuario usuario = buscarUsuarioPorEmail(email);

    if (usuario instanceof Cuidador cuidador) {
        cuidador.setSenha(passwordEncoder.encode(novaSenha));
        cuidadorRepository.save(cuidador);
        return;
    }

    if (usuario instanceof Instituicao instituicao) {
        instituicao.setSenha(passwordEncoder.encode(novaSenha));
        instituicaoRepository.save(instituicao);
        return;
    }

    throw new UnsupportedProfileException("Perfil nao suporta recuperacao de senha");
}

public Map<String, Object> reenviarCodigo(String identificador, String perfil) {
    Usuario usuario = buscarUsuario(parsePerfil(perfil), identificador);
    String email = emailDoUsuario(usuario);
    twoFactorService.enviarCodigo(email);

    Map<String, Object> resposta = new HashMap<>();
    resposta.put("email", mascararEmail(email));
    resposta.put("mensagem", "Código reenviado com sucesso");
    return resposta;
}
}
