package com.example.demo.entity;

import java.time.LocalDate;

import com.example.demo.enums.TipoVinculo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@ToString(exclude = { "idoso", "cuidador" })
@Table(name = "vinculo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vinculo_idoso_cuidador", columnNames = { "idoso_id", "cuidador_id" })
})
public class Vinculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @NotNull(message = "Data de criação é obrigatória")
    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @NotNull(message = "Idoso é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idoso_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vinculo_idoso"))
    private Idoso idoso;

    @NotNull(message = "Cuidador é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuidador_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vinculo_cuidador"))
    private Cuidador cuidador;

    @NotNull(message = "Tipo de vínculo é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculo", nullable = false, length = 20)
    private TipoVinculo tipoVinculo;
}