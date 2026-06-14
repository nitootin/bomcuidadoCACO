package com.example.demo.mappers;

import java.time.LocalDateTime;

import com.example.demo.dtos.AlertasDTO;
import com.example.demo.entity.Alertas;
import com.example.demo.entity.Idoso;
import com.example.demo.entity.Prescricao;
import com.example.demo.enums.StatusAlertas;
import com.example.demo.utils.TextoUtils;

public class AlertasMapper {

    public static AlertasDTO toDTO(Alertas alerta) {
        if (alerta == null) {
            return null;
        }

        AlertasDTO dto = new AlertasDTO();
        dto.setId(alerta.getId());
        dto.setIdosoId(alerta.getIdoso() != null ? alerta.getIdoso().getId() : null);
        dto.setIdosoNome(alerta.getIdoso() != null ? TextoUtils.paraExibicao(alerta.getIdoso().getNome()) : null);
        dto.setPrescricaoId(alerta.getPrescricao() != null ? alerta.getPrescricao().getId() : null);
        dto.setRemedioNome(
                alerta.getPrescricao() != null && alerta.getPrescricao().getRemedio() != null
                        ? TextoUtils.paraExibicao(alerta.getPrescricao().getRemedio().getNome())
                        : null);
        dto.setTipoAlerta(alerta.getTipoAlerta());
        dto.setStatusAlertas(alerta.getStatusAlertas());
        dto.setDataCriacao(alerta.getData_criacao());
        dto.setDataAtualizacao(alerta.getData_atualizacao());
        dto.setDataAgendada(alerta.getData_agendade());

        return dto;
    }

    public static Alertas toEntity(AlertasDTO dto, Idoso idoso) {
        return toEntity(dto, idoso, null);
    }

    public static Alertas toEntity(AlertasDTO dto, Idoso idoso, Prescricao prescricao) {
        if (dto == null) {
            return null;
        }

        Alertas alerta = new Alertas();
        alerta.setData_criacao(LocalDateTime.now());
        alerta.setIdoso(idoso);
        alerta.setPrescricao(prescricao);
        alerta.setTipoAlerta(dto.getTipoAlerta());
        alerta.setStatusAlertas(dto.getStatusAlertas() != null ? dto.getStatusAlertas() : StatusAlertas.AGENDADO);
        alerta.setData_agendade(dto.getDataAgendada());

        return alerta;
    }

    public static void updateEntity(Alertas alerta, AlertasDTO dto, Idoso idoso) {
        updateEntity(alerta, dto, idoso, null);
    }

    public static void updateEntity(Alertas alerta, AlertasDTO dto, Idoso idoso, Prescricao prescricao) {
        if (alerta == null || dto == null) {
            return;
        }

        alerta.setIdoso(idoso);
        alerta.setPrescricao(prescricao);
        alerta.setTipoAlerta(dto.getTipoAlerta());
        alerta.setData_agendade(dto.getDataAgendada());
        alerta.setData_atualizacao(LocalDateTime.now());

        if (dto.getStatusAlertas() != null) {
            alerta.setStatusAlertas(dto.getStatusAlertas());
        }
    }
}
