package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "idoso")
public class Idoso extends Usuario {

    @NotNull(message = "CPF deve ser preenchido")
    @Column(length = 11, nullable = false, unique = true)
    private String cpf;

    @Column(length = 300)
    private String observacoes;

    @NotNull(message = "Contato é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contato_id", nullable = false, foreignKey = @ForeignKey(name = "fk_idoso_contato"))
    private Contato contato;

    @OneToMany(mappedBy = "idoso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vinculo> vinculos = new ArrayList<>();
}
