package com.example.demo.dtos;

import java.time.LocalDateTime;

import com.example.demo.enums.StatusAlertas;
import com.example.demo.enums.TipoAlerta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlertasDTO {
    private Integer id;
    private Integer idosoId;
    private String idosoNome;
    private Integer prescricaoId;
    private String remedioNome;
    private TipoAlerta tipoAlerta;
    private StatusAlertas statusAlertas;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataAgendada;
}
