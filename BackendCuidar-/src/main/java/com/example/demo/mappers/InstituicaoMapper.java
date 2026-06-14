package com.example.demo.mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dtos.InstituicaoDTO;
import com.example.demo.entity.Instituicao;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.utils.TextoUtils;

public class InstituicaoMapper {

    private InstituicaoMapper() {
    }

    public static InstituicaoDTO toDTO(Instituicao instituicao) {
        if (instituicao == null) {
            return null;
        }

        InstituicaoDTO dto = new InstituicaoDTO();

        dto.setId(instituicao.getId());
        dto.setNome(TextoUtils.paraExibicao(instituicao.getNome()));
        dto.setCnpj(instituicao.getCnpj());
        dto.setEmail(instituicao.getEmail());
        dto.setRua(TextoUtils.paraExibicao(instituicao.getRua()));
        dto.setBairro(TextoUtils.paraExibicao(instituicao.getBairro()));
        dto.setUf(TextoUtils.paraUf(instituicao.getUf()));
        dto.setNumero(instituicao.getNumero());
        dto.setCep(instituicao.getCep());
        dto.setStatus(instituicao.getStatus());

        return dto;
    }

    public static Instituicao toEntity(InstituicaoDTO dto) {
        if (dto == null) {
            return null;
        }

        Instituicao instituicao = new Instituicao();
        
        instituicao.setNome(TextoUtils.paraBanco(dto.getNome()));
        instituicao.setCnpj(limparDocumento(dto.getCnpj()));
        instituicao.setEmail(dto.getEmail());
        instituicao.setSenha(dto.getSenha());
        instituicao.setRua(TextoUtils.paraBanco(dto.getRua()));
        instituicao.setBairro(TextoUtils.paraBanco(dto.getBairro()));
        instituicao.setUf(TextoUtils.paraBanco(dto.getUf()));
        instituicao.setNumero(dto.getNumero());
        instituicao.setCep(limparDocumento(dto.getCep()));

        instituicao.setData_criacao(LocalDateTime.now());
        instituicao.setPerfil(Perfil.INSTITUICAO);
        instituicao.setStatus(Status.ATIVO);

        return instituicao;
    }

    public static List<InstituicaoDTO> toDTOList(List<Instituicao> instituicoes) {
        List<InstituicaoDTO> lista = new ArrayList<>();

        if (instituicoes == null) {
            return lista;
        }

        for (Instituicao instituicao : instituicoes) {
            lista.add(toDTO(instituicao));
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
