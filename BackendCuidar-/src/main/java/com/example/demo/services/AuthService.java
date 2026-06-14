package com.example.demo.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Administrador;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.exceptions.UnsupportedProfileException;
import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.security.JwtService;

@Service
public class AuthService {

    private final AdministradorRepository administradorRepository;
    private final CuidadorRepository cuidadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final SenhaService senhaService;

    public AuthService(
            AdministradorRepository administradorRepository,
            CuidadorRepository cuidadorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TwoFactorService twoFactorService,
            SenhaService senhaService) {
        this.administradorRepository = administradorRepository;
        this.cuidadorRepository = cuidadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.twoFactorService = twoFactorService;
        this.senhaService = senhaService;
    }

    public Map<String, Object> login(Map<String, String> dados) {
        if (dados == null) {
            throw new InvalidRequestException("Dados de login nao informados");
        }

        String identificador = primeiroValor(dados, "identificador", "cpfCnpj", "cpf");
        String senha = primeiroValor(dados, "senha", "password");
        Perfil perfil = parsePerfil(dados.get("perfil"));

        if (identificador == null || identificador.isBlank()) {
            throw new InvalidRequestException("Informe CPF");
        }

        if (senha == null || senha.isBlank()) {
            throw new InvalidRequestException("Informe a senha");
        }

        Usuario usuario = buscarUsuario(perfil, identificador);
        String senhaSalva = senhaDoUsuario(usuario);

        if (senhaSalva == null || !passwordEncoder.matches(senha, senhaSalva)) {
            throw new UnauthorizedException("Credenciais invalidas");
        }

        if (usuario.getStatus() != Status.ATIVO) {
            throw new UnauthorizedException("Usuario inativo");
        }

        if (perfil == Perfil.CUIDADOR) {
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

    public Map<String, Object> recuperarSenha(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new InvalidRequestException("Informe o CPF");
        }

        String documento = limparDocumento(identificador);

        Usuario usuario = cuidadorRepository.findByCpf(documento)
                .map(u -> (Usuario) u)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado com esse CPF"));

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

        Cuidador cuidador = cuidadorRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario nao encontrado"));
        cuidador.setSenha(passwordEncoder.encode(novaSenha));
        cuidadorRepository.save(cuidador);
    }

    public Map<String, Object> reenviarCodigo(String identificador, String perfil) {
        Usuario usuario = buscarUsuario(parsePerfil(perfil), identificador);
        String email = emailDoUsuario(usuario);
        twoFactorService.enviarCodigo(email);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("email", mascararEmail(email));
        resposta.put("mensagem", "Codigo reenviado com sucesso");
        return resposta;
    }

    private String emailDoUsuario(Usuario usuario) {
        if (usuario instanceof Cuidador cuidador) return cuidador.getEmail();
        throw new UnsupportedProfileException("Perfil nao suporta 2FA");
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

        return resposta;
    }

    private Usuario buscarUsuario(Perfil perfil, String identificador) {
        String documento = limparDocumento(identificador);

        return switch (perfil) {
            case ADMINISTRADOR -> administradorRepository.findByCpf(documento)
                    .orElseThrow(() -> new UnauthorizedException("Credenciais invalidas"));
            case CUIDADOR -> cuidadorRepository.findByCpf(documento)
                    .orElseThrow(() -> new UnauthorizedException("Credenciais invalidas"));
            case IDOSO -> throw new UnsupportedProfileException("Perfil nao permitido para login");
        };
    }

    private String senhaDoUsuario(Usuario usuario) {
        if (usuario instanceof Administrador administrador) return administrador.getSenha();
        if (usuario instanceof Cuidador cuidador) return cuidador.getSenha();
        throw new UnsupportedProfileException("Perfil nao permitido para login");
    }

    private Perfil parsePerfil(String perfil) {
        if (perfil == null || perfil.isBlank()) {
            throw new InvalidRequestException("Informe o perfil");
        }

        try {
            return Perfil.valueOf(perfil.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Perfil invalido");
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

    private String mascararEmail(String email) {
        int at = email.indexOf("@");
        if (at <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(at);
    }
}
