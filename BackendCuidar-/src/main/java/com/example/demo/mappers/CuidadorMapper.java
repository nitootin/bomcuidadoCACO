package com.example.demo.mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dtos.ContatoDTO;
import com.example.demo.dtos.CuidadorDTO;
import com.example.demo.entity.Contato;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Instituicao;
import com.example.demo.enums.Perfil;
import com.example.demo.enums.Status;
import com.example.demo.utils.TextoUtils;

public class CuidadorMapper {

    private CuidadorMapper() {
    }

    public static CuidadorDTO toDTO(Cuidador cuidador) {
        if (cuidador == null) {
            return null;
        }

        CuidadorDTO dto = new CuidadorDTO();
        dto.setId(cuidador.getId());
        dto.setNome(TextoUtils.paraExibicao(cuidador.getNome()));
        dto.setCpf(cuidador.getCpf());
        dto.setEmail(cuidador.getEmail());
        dto.setStatus(cuidador.getStatus());

        if (cuidador.getInstituicao() != null) {
            dto.setInstituicaoId(cuidador.getInstituicao().getId());
        }

        if (cuidador.getContato() != null) {
            ContatoDTO contatoDTO = new ContatoDTO();
            contatoDTO.setId(cuidador.getContato().getId());
            contatoDTO.setDdd(cuidador.getContato().getDdd());
            contatoDTO.setTelefone(cuidador.getContato().getTelefone());
            contatoDTO.setCuidadorId(cuidador.getId());

            dto.setContato(contatoDTO);
        }

        return dto;
    }

    public static Cuidador toEntity(CuidadorDTO dto) {
        if (dto == null) {
            return null;
        }

        Cuidador cuidador = new Cuidador();

        cuidador.setNome(TextoUtils.paraBanco(dto.getNome()));
        cuidador.setCpf(limparDocumento(dto.getCpf()));
        cuidador.setEmail(dto.getEmail());
        cuidador.setSenha(dto.getSenha());

        if (dto.getInstituicaoId() != null) {
            Instituicao instituicao = new Instituicao();
            instituicao.setId(dto.getInstituicaoId());
            cuidador.setInstituicao(instituicao);
        }

        if (dto.getContato() != null) {
            Contato contato = new Contato();
            // setId removido — o id é gerado pelo banco
            contato.setDdd(TextoUtils.limparNumero(dto.getContato().getDdd()));
            contato.setTelefone(TextoUtils.limparNumero(dto.getContato().getTelefone()));
            contato.setCuidador(cuidador);
            cuidador.setContato(contato);
        }

        cuidador.setData_criacao(LocalDateTime.now());
        cuidador.setPerfil(Perfil.CUIDADOR);
        cuidador.setStatus(Status.ATIVO);

        return cuidador;
    }

    public static List<CuidadorDTO> toDTOList(List<Cuidador> cuidadores) {
        List<CuidadorDTO> lista = new ArrayList<>();

        if (cuidadores == null) {
            return lista;
        }

        for (Cuidador cuidador : cuidadores) {
            lista.add(toDTO(cuidador));
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
