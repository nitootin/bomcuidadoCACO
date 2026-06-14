package com.example.demo.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ContatoDTO {

    private Integer id;
    
    @NotBlank(message = "Um DDD deve ser inserido")
    @Pattern(regexp = "^\\d{2}$", message = "DDD deve conter exatamente 2 dígitos")
    private String ddd;
    
    @NotBlank(message = "Um número de contato deve ser preenchido")
    @Pattern(regexp = "^\\d{8,9}$", message = "Telefone deve conter 8 ou 9 dígitos")
    private String telefone;
    private Integer cuidadorId;
    private List<Integer> idosos;
}