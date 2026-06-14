package com.example.demo.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.services.AuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> dados) {
        return ResponseEntity.ok(authService.login(dados));
    }

    @PostMapping("/idoso/login")
    public ResponseEntity<?> loginIdoso(@RequestBody Map<String, String> dados) {
        return ResponseEntity.ok(authService.loginIdoso(dados));
    }

    @PostMapping("/verificar-2fa")
    public ResponseEntity<?> verificar2fa(@RequestBody Map<String, String> dados) {
        String identificador = primeiroValor(dados, "identificador", "cpfCnpj", "cpf", "cnpj");
        String codigo = dados.get("codigo");
        String perfil = dados.get("perfil");

        if (identificador == null || codigo == null || perfil == null) {
            throw new InvalidRequestException("Identificador, perfil e codigo sao obrigatorios");
        }

        return ResponseEntity.ok(authService.verificar2fa(identificador, codigo, perfil));
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody Map<String, String> dados) {
        String identificador = dados.get("identificador");
        return ResponseEntity.ok(authService.recuperarSenha(identificador));
    }

    @PostMapping("/verificar-recuperacao")
    public ResponseEntity<?> verificarRecuperacao(@RequestBody Map<String, String> dados) {
        String email = dados.get("email");
        String codigo = dados.get("codigo");

        if (email == null || codigo == null) {
            throw new InvalidRequestException("Email e codigo sao obrigatorios");
        }

        return ResponseEntity.ok(authService.verificarRecuperacao(email, codigo));
    }

    @PostMapping("/nova-senha")
    public ResponseEntity<?> novaSenha(@RequestBody Map<String, String> dados) {
        String email = dados.get("email");
        String novaSenha = dados.get("novaSenha");

        if (email == null || novaSenha == null) {
            throw new InvalidRequestException("Email e nova senha sao obrigatorios");
        }

        authService.novaSenha(email, novaSenha);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
    }

    private String primeiroValor(Map<String, String> dados, String... chaves) {
        for (String chave : chaves) {
            String valor = dados.get(chave);
            if (valor != null && !valor.isBlank()) return valor;
        }
        return null;
    }
    
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<?> reenviarCodigo(@RequestBody Map<String, String> dados) {
        String identificador = primeiroValor(dados, "identificador", "cpfCnpj", "cpf", "cnpj");
        String perfil = dados.get("perfil");

        if (identificador == null || perfil == null) {
            throw new InvalidRequestException("Identificador e perfil são obrigatórios");
        }

        return ResponseEntity.ok(authService.reenviarCodigo(identificador, perfil));
    }
}
