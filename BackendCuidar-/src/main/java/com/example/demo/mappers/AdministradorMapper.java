package com.example.demo.mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dtos.AdministradorDTO;
import com.example.demo.entity.Administrador;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.utils.TextoUtils;

public class AdministradorMapper {

    private AdministradorMapper() {
    }

    public static AdministradorDTO toDTO(Administrador administrador) {
        if (administrador == null) {
            return null;
        }

        AdministradorDTO dto = new AdministradorDTO();
        dto.setId(administrador.getId());
        dto.setNome(TextoUtils.paraExibicao(administrador.getNome()));
        dto.setCpf(administrador.getCpf());
        dto.setEmail(administrador.getEmail());

        return dto;
    }

    public static Administrador toEntity(AdministradorDTO dto) {
        if (dto == null) {
            return null;
        }

        Administrador administrador = new Administrador();

        administrador.setNome(TextoUtils.paraBanco(dto.getNome()));
        administrador.setCpf(limparDocumento(dto.getCpf()));
        administrador.setEmail(dto.getEmail());
        administrador.setSenha(dto.getSenha());

        administrador.setData_criacao(LocalDateTime.now());
        administrador.setPerfil(Perfil.ADMINISTRADOR);
        administrador.setStatus(Status.ATIVO);

        return administrador;
    }

    public static void updateEntity(Administrador administrador, AdministradorDTO dto) {
        if (administrador == null || dto == null) {
            return;
        }

        administrador.setNome(TextoUtils.paraBanco(dto.getNome()));
        administrador.setCpf(limparDocumento(dto.getCpf()));
        administrador.setEmail(dto.getEmail());
        administrador.setData_atualizacao(LocalDateTime.now());
    }

    public static void inativarEntity(Administrador administrador) {
        if (administrador == null) {
            return;
        }

        administrador.setStatus(Status.INATIVO);
        administrador.setData_atualizacao(LocalDateTime.now());
    }

    public static List<AdministradorDTO> toDTOList(List<Administrador> administradores) {
        List<AdministradorDTO> lista = new ArrayList<>();

        if (administradores == null) {
            return lista;
        }

        for (Administrador administrador : administradores) {
            lista.add(toDTO(administrador));
        }

        return lista;
    }

    private static String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }
}
