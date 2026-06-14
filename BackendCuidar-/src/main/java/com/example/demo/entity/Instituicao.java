package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
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
@ToString(callSuper = true, exclude = "cuidadores")
@Table(name = "instituicao")
public class Instituicao extends Usuario {

    @NotBlank(message = "CNPJ deve ser preenchido")
    @Column(length = 14, nullable = false, unique = true)
    private String cnpj;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String email;

    @NotBlank
    @Column(length = 300, nullable = false)
    private String senha;

    @Column(length = 200)
    private String rua;

    @Column(length = 200)
    private String bairro;

    @NotBlank(message = "Uf deve ser preenchido")
    @Column(length = 2)
    private String uf;

    @NotBlank(message = "Número deve ser preenchido")
    private int numero;

    @NotBlank(message = "CEP deve ser preenchido")
    @Column(length = 8)
    private String cep;

    @OneToMany(mappedBy = "instituicao", fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Idoso> idosos = new ArrayList<>();

    @OneToMany(mappedBy = "instituicao", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cuidador> cuidadores = new ArrayList<>();
}
