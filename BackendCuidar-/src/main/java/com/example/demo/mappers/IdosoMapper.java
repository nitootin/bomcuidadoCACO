package com.example.demo.mappers;

import java.time.LocalDateTime;

import com.example.demo.dtos.IdosoDTO;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Instituicao;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.utils.TextoUtils;

public class IdosoMapper {

    private IdosoMapper() {}

    public static IdosoDTO toDTO(Idoso idoso) {
        if (idoso == null) return null;

        IdosoDTO dto = new IdosoDTO();
        dto.setId(idoso.getId());
        dto.setNome(TextoUtils.paraExibicao(idoso.getNome()));
        dto.setCpf(idoso.getCpf());
        dto.setObservacoes(TextoUtils.textoLivre(idoso.getObservacoes()));
        dto.setSenhaAcessoGerada(idoso.getSenhaAcessoCriptografada() != null
                && !idoso.getSenhaAcessoCriptografada().isBlank());

        if (idoso.getInstituicao() != null) {
            dto.setInstituicaoId(idoso.getInstituicao().getId());
        }

        if (idoso.getContato() != null) {
            dto.setContatoId(idoso.getContato().getId());
            dto.setContato(ContatoMapper.toDTO(idoso.getContato()));
        }

        dto.setData_criacao(idoso.getData_criacao());
        dto.setData_atualizacao(idoso.getData_atualizacao());
        dto.setPerfil(idoso.getPerfil());
        dto.setStatus(idoso.getStatus());

        return dto;
    }

    public static Idoso toEntity(IdosoDTO dto) {
        if (dto == null) return null;

        Idoso idoso = new Idoso();
       
        idoso.setNome(TextoUtils.paraBanco(dto.getNome()));
        idoso.setCpf(limparDocumento(dto.getCpf()));
        idoso.setObservacoes(TextoUtils.textoLivre(dto.getObservacoes()));

        if (dto.getInstituicaoId() != null) {
            Instituicao instituicao = new Instituicao();
            instituicao.setId(dto.getInstituicaoId());
            idoso.setInstituicao(instituicao);
        }

        if (dto.getContatoId() != null) {
            Contato contato = new Contato();
            contato.setId(dto.getContatoId());
            idoso.setContato(contato);
        }

        if (dto.getData_criacao() != null) {
            idoso.setData_criacao(dto.getData_criacao());
        } else {
            idoso.setData_criacao(LocalDateTime.now());
        }

        if (dto.getPerfil() != null) {
            idoso.setPerfil(dto.getPerfil());
        } else {
            idoso.setPerfil(Perfil.IDOSO);
        }

        if (dto.getStatus() != null) {
            idoso.setStatus(dto.getStatus());
        } else {
            idoso.setStatus(Status.ATIVO);
        }

        return idoso;
    }

    public static void atualizarIdoso(Idoso idoso, IdosoDTO dto, Instituicao instituicao) {
        if (dto == null || idoso == null) return;

        idoso.setNome(TextoUtils.paraBanco(dto.getNome()));
        idoso.setCpf(limparDocumento(dto.getCpf()));
        idoso.setObservacoes(TextoUtils.textoLivre(dto.getObservacoes()));

        if (instituicao != null) {
            idoso.setInstituicao(instituicao);
        } else if (dto.getInstituicaoId() != null) {
            Instituicao stub = new Instituicao();
            stub.setId(dto.getInstituicaoId());
            idoso.setInstituicao(stub);
        }

    if (dto.getContatoId() != null) {
            Contato contato = new Contato();
            contato.setId(dto.getContatoId());
            idoso.setContato(contato);
        }
    }

    private static String limparDocumento(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }
}
