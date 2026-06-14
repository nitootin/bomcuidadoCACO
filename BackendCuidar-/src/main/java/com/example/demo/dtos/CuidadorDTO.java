package com.example.demo.dtos;

import com.example.demo.enums.Status;

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
public class CuidadorDTO {

    private Integer id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private Status status;
    private Integer instituicaoId;
    private ContatoDTO contato;
}
