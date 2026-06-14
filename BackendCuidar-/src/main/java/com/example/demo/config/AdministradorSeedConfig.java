package com.example.demo.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Administrador;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.repository.AdministradorRepository;

@Configuration
public class AdministradorSeedConfig {

    public static final String ADMIN_CPF = "00000000000";
    public static final String ADMIN_EMAIL = "admin@bomcuidado.com";
    public static final String ADMIN_SENHA = "Admin@123";

    @Bean
    @Order(1)
    public CommandLineRunner criarAdministradorPadrao(
            AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (administradorRepository.existsByCpf(ADMIN_CPF)) {
                return;
            }

            Administrador administrador = new Administrador();
            administrador.setNome("Administrador Sistema");
            administrador.setCpf(ADMIN_CPF);
            administrador.setEmail(ADMIN_EMAIL);
            administrador.setSenha(passwordEncoder.encode(ADMIN_SENHA));
            administrador.setPerfil(Perfil.ADMINISTRADOR);
            administrador.setStatus(Status.ATIVO);
            administrador.setData_criacao(LocalDateTime.now());

            administradorRepository.save(administrador);
        };
    }
}
