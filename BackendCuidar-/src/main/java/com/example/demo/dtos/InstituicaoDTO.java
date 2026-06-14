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
public class InstituicaoDTO {
    
    private Integer id;
    private String nome;
    private String cnpj;
    private String email;
    private String senha;
    private String rua;
    private String bairro;
    private String uf;
    private Integer numero;
    private String cep;
    private Status status;

}
