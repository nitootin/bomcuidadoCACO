package com.example.demo.dtos;

import java.time.LocalDate;

import com.example.demo.enums.TipoVinculo;

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
public class VinculoDTO {

    private Integer id;
    private LocalDate dataCriacao;
    private Integer idosoId;
    private Integer cuidadorId;
    private String nomeIdoso;
    private String nomeCuidador;
    private TipoVinculo tipoVinculo;
}