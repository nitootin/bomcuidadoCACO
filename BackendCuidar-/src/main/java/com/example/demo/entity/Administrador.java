package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Table(name = "administrador")
public class Administrador extends Usuario {

    @NotBlank
    @Column(length = 11, nullable = false, unique = true)
    private String cpf;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String email;

    @NotBlank
    @Column(length = 300, nullable = false)
    private String senha;

}
