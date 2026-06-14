package com.example.demo.mappers;

import java.time.LocalDate;

import com.example.demo.dtos.VinculoDTO;
import com.example.demo.entity.Cuidador;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Vinculo;
import com.example.demo.enums.TipoVinculo;
import com.example.demo.utils.TextoUtils;

public class VinculoMapper {

    private VinculoMapper() {}

    public static VinculoDTO toDTO(Vinculo vinculo) {
        if (vinculo == null) return null;

        VinculoDTO dto = new VinculoDTO();
        dto.setId(vinculo.getId());
        dto.setDataCriacao(vinculo.getDataCriacao());

        if (vinculo.getIdoso() != null) {
            dto.setIdosoId(vinculo.getIdoso().getId());
            dto.setNomeIdoso(TextoUtils.paraExibicao(vinculo.getIdoso().getNome()));
        }

        if (vinculo.getCuidador() != null) {
            dto.setCuidadorId(vinculo.getCuidador().getId());
            dto.setNomeCuidador(TextoUtils.paraExibicao(vinculo.getCuidador().getNome()));
        }

        dto.setTipoVinculo(vinculo.getTipoVinculo());

        return dto;
    }

    public static Vinculo toEntity(VinculoDTO dto, Idoso idoso, Cuidador cuidador) {
        if (dto == null) return null;

        Vinculo vinculo = new Vinculo();
        vinculo.setDataCriacao(dto.getDataCriacao() != null ? dto.getDataCriacao() : LocalDate.now());
        vinculo.setIdoso(idoso);
        vinculo.setCuidador(cuidador);
        vinculo.setTipoVinculo(dto.getTipoVinculo() != null ? dto.getTipoVinculo() : TipoVinculo.PADRAO);

        return vinculo;
    }
}
