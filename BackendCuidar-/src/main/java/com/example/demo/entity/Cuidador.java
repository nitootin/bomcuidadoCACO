package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@ToString(callSuper = true, exclude = { "contato", "vinculos" })
@Table(name = "cuidador")
public class Cuidador extends Usuario {

    @NotNull(message = "CPF deve ser preenchido")
    @Column(length = 11, nullable = false, unique = true)
    private String cpf;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String email;

    @NotBlank
    @Column(length = 300, nullable = false)
    private String senha;

    @OneToOne(mappedBy = "cuidador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Contato contato;

    @OneToMany(mappedBy = "cuidador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vinculo> vinculos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;
}
