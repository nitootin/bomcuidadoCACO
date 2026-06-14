package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.example.demo.support.TestDataFactory.contato;
import static com.example.demo.support.TestDataFactory.idoso;
import static com.example.demo.support.TestDataFactory.idosoDTO;
import static com.example.demo.support.TestDataFactory.instituicao;
import static com.example.demo.support.TestDataFactory.cuidador;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.dtos.IdosoDTO;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Idoso;
import com.example.demo.enums.Status;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.InvalidRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repository.ContatoRepository;
import com.example.demo.repository.CuidadorRepository;
import com.example.demo.repository.IdosoRepository;
import com.example.demo.repository.InstituicaoRepository;
import com.example.demo.repository.VinculoRepository;

@ExtendWith(MockitoExtension.class)
class IdosoServiceTest {

    @Mock
    private IdosoRepository idosoRepository;

    @Mock
    private CuidadorRepository cuidadorRepository;

    @Mock
    private InstituicaoRepository instituicaoRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private VinculoRepository vinculoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private IdosoService service;

    @BeforeEach
    void setUp() {
        service = new IdosoService(
                idosoRepository,
                cuidadorRepository,
                instituicaoRepository,
                contatoRepository,
                vinculoRepository,
                passwordEncoder,
                "segredo-de-teste-para-criptografia");
    }

    @Test
    void deveListarIdososAtivosPorInstituicao() {
        Pageable pageable = PageRequest.of(0, 10);
        Idoso idosoDaInstituicao = idoso(1, "Maria", "12345678901", Status.ATIVO);

        when(idosoRepository.findByStatusAndInstituicaoId(Status.ATIVO, 10, pageable))
                .thenReturn(new PageImpl<>(List.of(idosoDaInstituicao)));

        var resultado = service.listarAtivosPorInstituicao(10, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Maria", resultado.getContent().get(0).getNome());
        assertEquals(10, resultado.getContent().get(0).getInstituicaoId());
        verify(idosoRepository).findByStatusAndInstituicaoId(Status.ATIVO, 10, pageable);
    }

    @Test
    void deveCriarIdosoComContatoNovo() {
        IdosoDTO dto = idosoDTO();
        Contato contatoSalvo = contato(5, "11", "999999999");

        when(idosoRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(instituicaoRepository.findById(10)).thenReturn(Optional.of(instituicao()));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoSalvo);
        when(idosoRepository.save(any(Idoso.class))).thenAnswer(invocation -> {
            Idoso idoso = invocation.getArgument(0);
            idoso.setId(1);
            return idoso;
        });

        IdosoDTO resultado = service.criar(dto);

        assertEquals(1, resultado.getId());
        assertEquals("Maria", resultado.getNome());
        assertEquals("12345678901", resultado.getCpf());
        assertEquals(10, resultado.getInstituicaoId());
        assertEquals(5, resultado.getContatoId());
        assertEquals(Status.ATIVO, resultado.getStatus());
    }

    @Test
    void deveBloquearCriacaoComCpfAtivoDuplicado() {
        Idoso existente = idoso(1, "Maria", "12345678901", Status.ATIVO);

        when(idosoRepository.findByCpf("12345678901")).thenReturn(Optional.of(existente));

        assertThrows(DuplicateResourceException.class, () -> service.criar(idosoDTO()));
    }


    @Test
    void deveBloquearCriacaoComCpfDeCuidador() {
        IdosoDTO dto = idosoDTO();

        when(cuidadorRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.criar(dto));
    }

    @Test
    void deveReativarIdosoInativoAoCriarComMesmoCpf() {
        IdosoDTO dto = idosoDTO();
        Idoso existente = idoso(1, "Maria Antiga", "12345678901", Status.INATIVO);
        Contato contatoSalvo = contato(5, "11", "999999999");

        when(idosoRepository.findByCpf("12345678901")).thenReturn(Optional.of(existente));
        when(instituicaoRepository.findById(10)).thenReturn(Optional.of(instituicao()));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoSalvo);
        when(idosoRepository.save(existente)).thenReturn(existente);

        IdosoDTO resultado = service.criar(dto);

        assertEquals(1, resultado.getId());
        assertEquals("Maria", resultado.getNome());
        assertEquals(Status.ATIVO, resultado.getStatus());
        assertEquals(5, resultado.getContatoId());
    }

    @Test
    void deveFalharAoCriarComInstituicaoInexistente() {
        when(idosoRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(instituicaoRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.criar(idosoDTO()));
    }

    @Test
    void deveFalharAoCriarComContatoIncompleto() {
        IdosoDTO dto = idosoDTO();
        dto.getContato().setTelefone(null);

        when(idosoRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(instituicaoRepository.findById(10)).thenReturn(Optional.of(instituicao()));

        assertThrows(InvalidRequestException.class, () -> service.criar(dto));
    }

    @Test
    void deveFalharAoCriarSemContato() {
        IdosoDTO dto = idosoDTO();
        dto.setContato(null);
        dto.setContatoId(null);

        when(idosoRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(instituicaoRepository.findById(10)).thenReturn(Optional.of(instituicao()));

        assertThrows(InvalidRequestException.class, () -> service.criar(dto));
    }

    @Test
    void deveAtualizarIdosoComContatoExistente() {
        IdosoDTO dto = idosoDTO();
        dto.setNome("Maria Atualizada");
        dto.setContato(null);
        dto.setContatoId(5);
        Idoso existente = idoso(1, "Maria", "12345678901", Status.ATIVO);

        when(idosoRepository.findById(1)).thenReturn(Optional.of(existente));
        when(instituicaoRepository.findById(10)).thenReturn(Optional.of(instituicao()));
        when(contatoRepository.findById(5)).thenReturn(Optional.of(contato(5, "11", "999999999")));
        when(idosoRepository.save(existente)).thenReturn(existente);

        IdosoDTO resultado = service.atualizar(1, dto);

        assertEquals("Maria Atualizada", resultado.getNome());
        assertEquals(5, resultado.getContatoId());
        verify(idosoRepository).save(existente);
    }

    @Test
    void deveBloquearAtualizacaoComCpfJaEmUso() {
        IdosoDTO dto = idosoDTO();
        dto.setCpf("10987654321");
        Idoso existente = idoso(1, "Maria", "12345678901", Status.ATIVO);

        when(idosoRepository.findById(1)).thenReturn(Optional.of(existente));
        when(idosoRepository.existsByCpf("10987654321")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.atualizar(1, dto));
    }


    @Test
    void deveBloquearAtualizacaoComCpfDeCuidador() {
        IdosoDTO dto = idosoDTO();
        dto.setCpf("10987654321");
        Idoso existente = idoso(1, "Maria", "12345678901", Status.ATIVO);

        when(idosoRepository.findById(1)).thenReturn(Optional.of(existente));
        when(idosoRepository.existsByCpf("10987654321")).thenReturn(false);
        when(cuidadorRepository.existsByCpf("10987654321")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.atualizar(1, dto));
    }

    @Test
    void deveInativarIdoso() {
        Idoso existente = idoso(1, "Maria", "12345678901", Status.ATIVO);

        when(idosoRepository.findById(1)).thenReturn(Optional.of(existente));

        service.inativar(1);

        ArgumentCaptor<Idoso> captor = ArgumentCaptor.forClass(Idoso.class);
        verify(idosoRepository).save(captor.capture());
        assertEquals(Status.INATIVO, captor.getValue().getStatus());
    }

    @Test
    void deveAutenticarPorSenhaAcessoSemCpf() {
        Idoso idoso = idoso(1, "Maria", "12345678901", Status.ATIVO);

        when(cuidadorRepository.findById(2)).thenReturn(Optional.of(cuidador()));
        when(passwordEncoder.matches("senhaCuidador", "hash")).thenReturn(true);
        when(idosoRepository.findById(1)).thenReturn(Optional.of(idoso));
        when(vinculoRepository.existsByIdosoIdAndCuidadorId(1, 2)).thenReturn(true);
        when(idosoRepository.save(idoso)).thenReturn(idoso);

        Map<String, Object> dadosSenha = service.obterSenhaAcesso(1, 2, "senhaCuidador");
        String senhaAcesso = (String) dadosSenha.get("senha");

        when(idosoRepository.findBySenhaAcessoCriptografadaIsNotNull()).thenReturn(List.of(idoso));

        Idoso autenticado = service.autenticarPorSenhaAcesso(senhaAcesso);

        assertEquals(1, autenticado.getId());
        verify(idosoRepository).findBySenhaAcessoCriptografadaIsNotNull();
    }

    @Test
    void deveFalharAutenticacaoPorSenhaAcessoInvalida() {
        when(idosoRepository.findBySenhaAcessoCriptografadaIsNotNull()).thenReturn(List.of());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> service.autenticarPorSenhaAcesso("senhaerrada"));

        assertEquals("Senha de acesso inválida.", ex.getMessage());
    }

    @Test
    void deveFalharAoBuscarIdosoInexistente() {
        when(idosoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99));
    }
}
