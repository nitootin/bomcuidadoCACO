package com.example.demo.services;

import static com.example.demo.support.TestDataFactory.administrador;
import static com.example.demo.support.TestDataFactory.cuidador;
import static com.example.demo.support.TestDataFactory.dadosLogin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Administrador;
import com.example.demo.entity.Cuidador;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.exceptions.UnsupportedProfileException;
import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private CuidadorRepository cuidadorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private SenhaService senhaService;

    @InjectMocks
    private AuthService service;

    @Test
    void deveLogarAdministradorSemMfa() {
        Administrador administrador = administrador();

        when(administradorRepository.findByCpf("12345678901")).thenReturn(Optional.of(administrador));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);
        when(jwtService.gerarToken(administrador)).thenReturn("token-admin");

        Map<String, Object> resposta = service.login(dadosLogin("123.456.789-01", "senha", "ADMINISTRADOR"));

        assertEquals(1, resposta.get("id"));
        assertEquals("Admin", resposta.get("nome"));
        assertEquals(administrador.getPerfil(), resposta.get("perfil"));
        assertEquals("token-admin", resposta.get("token"));
        assertEquals("Bearer", resposta.get("tipo"));
        assertEquals(true, resposta.get("autenticado"));
    }

    @Test
    void deveExigirMfaParaCuidador() {
        Cuidador cuidador = cuidador();

        when(cuidadorRepository.findByCpf("12345678901")).thenReturn(Optional.of(cuidador));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);

        Map<String, Object> resposta = service.login(dadosLogin("12345678901", "senha", "CUIDADOR"));

        assertEquals(true, resposta.get("requer2fa"));
        assertEquals("cu***@email.com", resposta.get("email"));
        verify(twoFactorService).enviarCodigo("cuidador@email.com");
    }

    @Test
    void deveRejeitarPerfilIdosoNoLogin() {
        assertThrows(UnsupportedProfileException.class,
                () -> service.login(dadosLogin("12345678901", "senha", "IDOSO")));
    }

    @Test
    void deveFalharLoginComSenhaIncorreta() {
        Administrador administrador = administrador();

        when(administradorRepository.findByCpf("12345678901")).thenReturn(Optional.of(administrador));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> service.login(dadosLogin("12345678901", "errada", "ADMINISTRADOR")));
    }

    @Test
    void deveFalharLoginComUsuarioInativo() {
        Administrador administrador = administrador();
        administrador.setStatus(Status.INATIVO);

        when(administradorRepository.findByCpf("12345678901")).thenReturn(Optional.of(administrador));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);

        assertThrows(UnauthorizedException.class,
                () -> service.login(dadosLogin("12345678901", "senha", "ADMINISTRADOR")));
    }

    @Test
    void deveFalharLoginSemPerfil() {
        Map<String, String> dados = dadosLogin("12345678901", "senha", "");

        assertThrows(InvalidRequestException.class, () -> service.login(dados));
    }

    @Test
    void deveFalharLoginComPerfilInvalido() {
        Map<String, String> dados = dadosLogin("12345678901", "senha", "INVALIDO");

        assertThrows(InvalidRequestException.class, () -> service.login(dados));
    }

    @Test
    void deveVerificarMfaDeCuidadorEGerarToken() {
        Cuidador cuidador = cuidador();

        when(cuidadorRepository.findByCpf("12345678901")).thenReturn(Optional.of(cuidador));
        when(jwtService.gerarToken(cuidador)).thenReturn("token-cuidador");

        Map<String, Object> resposta = service.verificar2fa("123.456.789-01", "123456", "CUIDADOR");

        verify(twoFactorService).validarCodigo("cuidador@email.com", "123456");
        assertEquals("token-cuidador", resposta.get("token"));
        assertEquals(true, resposta.get("autenticado"));
    }

    @Test
    void deveIniciarRecuperacaoDeSenhaParaCuidador() {
        Cuidador cuidador = cuidador();

        when(cuidadorRepository.findByCpf("12345678901")).thenReturn(Optional.of(cuidador));

        Map<String, Object> resposta = service.recuperarSenha("123.456.789-01");

        assertEquals("cu***@email.com", resposta.get("email"));
        verify(twoFactorService).enviarCodigo("cuidador@email.com");
    }

    @Test
    void deveTrocarSenhaDeCuidador() {
        Cuidador cuidador = cuidador();

        when(cuidadorRepository.findByEmail("cuidador@email.com")).thenReturn(Optional.of(cuidador));
        when(passwordEncoder.encode("Nova@123")).thenReturn("nova-hash");

        service.novaSenha("cuidador@email.com", "Nova@123");

        verify(senhaService).validar("Nova@123");
        verify(cuidadorRepository).save(cuidador);
        assertEquals("nova-hash", cuidador.getSenha());
    }

    @Test
    void deveVerificarCodigoDeRecuperacao() {
        Map<String, Object> resposta = service.verificarRecuperacao("cuidador@email.com", "123456");

        verify(twoFactorService).validarCodigo("cuidador@email.com", "123456");
        assertTrue((Boolean) resposta.get("valido"));
        assertEquals("cuidador@email.com", resposta.get("email"));
    }
}
