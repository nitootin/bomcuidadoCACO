package com.example.demo.support;

import java.time.LocalDateTime;
import java.util.Map;

import com.example.demo.dtos.ContatoDTO;
import com.example.demo.dtos.AlertasDTO;
import com.example.demo.dtos.IdosoDTO;
import com.example.demo.dtos.PrescricaoDTO;
import com.example.demo.dtos.RemedioDTO;
import com.example.demo.entity.Administrador;
import com.example.demo.entity.Alertas;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Instituicao;
import com.example.demo.entity.Prescricao;
import com.example.demo.entity.Remedio;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.enums.StatusAlertas;
import com.example.demo.enums.TipoAlerta;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static RemedioDTO remedioDTO(String nome, String observacao, Status status) {
        RemedioDTO dto = new RemedioDTO();
        dto.setNome(nome);
        dto.setObservacao(observacao);
        dto.setStatus(status);
        return dto;
    }

    public static Remedio remedio(int id, String nome, String observacao, Status status) {
        Remedio remedio = new Remedio();
        remedio.setId(id);
        remedio.setNome(nome);
        remedio.setObservacao(observacao);
        remedio.setStatus(status);
        return remedio;
    }

    public static PrescricaoDTO prescricaoDTO() {
        PrescricaoDTO dto = new PrescricaoDTO();
        dto.setRemedioId(10);
        dto.setIdosoId(20);
        dto.setDosagem("1 comprimido");
        dto.setIntervalo(8.0);
        dto.setNecessarioJejum(false);
        dto.setInstrucao("Tomar com agua");
        dto.setDataFim(LocalDateTime.now().plusDays(7));
        return dto;
    }

    public static Prescricao prescricao(int id, Remedio remedio, Idoso idoso, Status status) {
        Prescricao prescricao = new Prescricao();
        prescricao.setId(id);
        prescricao.setRemedio(remedio);
        prescricao.setIdoso(idoso);
        prescricao.setData_criacao(LocalDateTime.now());
        prescricao.setData_fim(LocalDateTime.now().plusDays(7));
        prescricao.setStatus(status);
        prescricao.setNecessario_jejum(false);
        prescricao.setInstrucao("Tomar com agua");
        prescricao.setIntervalo(8.0);
        prescricao.setDosagem("1 comprimido");
        return prescricao;
    }

    public static AlertasDTO alertaDTO() {
        AlertasDTO dto = new AlertasDTO();
        dto.setIdosoId(20);
        dto.setTipoAlerta(TipoAlerta.REMEDIO);
        dto.setDataAgendada(LocalDateTime.now().plusHours(2));
        return dto;
    }

    public static Alertas alerta(int id, Idoso idoso, StatusAlertas statusAlertas) {
        Alertas alerta = new Alertas();
        alerta.setId(id);
        alerta.setIdoso(idoso);
        alerta.setTipoAlerta(TipoAlerta.REMEDIO);
        alerta.setStatusAlertas(statusAlertas);
        alerta.setData_criacao(LocalDateTime.now());
        alerta.setData_agendade(LocalDateTime.now().plusHours(2));
        return alerta;
    }

    public static Remedio remedio() {
        return remedio(10, "Dipirona", null, Status.ATIVO);
    }

    public static IdosoDTO idosoDTO() {
        IdosoDTO dto = new IdosoDTO();
        dto.setNome("Maria");
        dto.setCpf("12345678901");
        dto.setObservacoes("Alergia a dipirona");
        dto.setInstituicaoId(10);
        dto.setContato(contatoDTO());
        return dto;
    }

    public static ContatoDTO contatoDTO() {
        ContatoDTO dto = new ContatoDTO();
        dto.setDdd("11");
        dto.setTelefone("999999999");
        return dto;
    }

    public static Idoso idoso() {
        Idoso idoso = new Idoso();
        idoso.setId(20);
        idoso.setNome("Maria");
        idoso.setStatus(Status.ATIVO);
        return idoso;
    }

    public static Idoso idoso(int id, String nome, String cpf, Status status) {
        Idoso idoso = idoso();
        idoso.setId(id);
        idoso.setNome(nome);
        idoso.setCpf(cpf);
        idoso.setObservacoes("Alergia a dipirona");
        idoso.setInstituicao(instituicao());
        idoso.setContato(contato(5, "11", "999999999"));
        idoso.setData_criacao(LocalDateTime.now());
        idoso.setPerfil(Perfil.IDOSO);
        idoso.setStatus(status);
        return idoso;
    }

    public static Instituicao instituicao() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(10);
        instituicao.setNome("Instituicao Bom Cuidado");
        instituicao.setStatus(Status.ATIVO);
        return instituicao;
    }

    public static Contato contato(Integer id, String ddd, String telefone) {
        Contato contato = new Contato();
        contato.setId(id);
        contato.setDdd(ddd);
        contato.setTelefone(telefone);
        return contato;
    }

    public static Map<String, String> dadosLogin(String identificador, String senha, String perfil) {
        return Map.of(
                "identificador", identificador,
                "senha", senha,
                "perfil", perfil);
    }

    public static Administrador administrador() {
        Administrador administrador = new Administrador();
        administrador.setId(1);
        administrador.setNome("Admin");
        administrador.setCpf("12345678901");
        administrador.setEmail("admin@email.com");
        administrador.setSenha("hash");
        administrador.setPerfil(Perfil.ADMINISTRADOR);
        administrador.setStatus(Status.ATIVO);
        administrador.setData_criacao(LocalDateTime.now());
        return administrador;
    }

    public static Cuidador cuidador() {
        Cuidador cuidador = new Cuidador();
        cuidador.setId(2);
        cuidador.setNome("Cuidador");
        cuidador.setCpf("12345678901");
        cuidador.setEmail("cuidador@email.com");
        cuidador.setSenha("hash");
        cuidador.setPerfil(Perfil.CUIDADOR);
        cuidador.setStatus(Status.ATIVO);
        cuidador.setData_criacao(LocalDateTime.now());
        return cuidador;
    }

    public static Instituicao instituicaoAuth() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(3);
        instituicao.setNome("Instituicao");
        instituicao.setCnpj("12345678000199");
        instituicao.setEmail("instituicao@email.com");
        instituicao.setSenha("hash");
        instituicao.setPerfil(Perfil.INSTITUICAO);
        instituicao.setStatus(Status.ATIVO);
        instituicao.setData_criacao(LocalDateTime.now());
        return instituicao;
    }
}
