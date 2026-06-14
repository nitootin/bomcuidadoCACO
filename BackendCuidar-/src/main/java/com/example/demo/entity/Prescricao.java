package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.enums.Status;
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
@ToString(callSuper = true)
@Table(name="Prescricao")
public class Prescricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @NotNull(message = "Remédio obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_remedio", nullable = false, foreignKey = @ForeignKey(name = "fk_remedio_prescricao"))
    private Remedio remedio;

    @NotNull(message = "idoso obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_idoso", nullable = false, foreignKey = @ForeignKey(name = "fk_idoso_prescricao"))
    private Idoso idoso;

    @NotNull(message = "Inicio obrigatório")
    private LocalDateTime data_criacao;

    @NotNull(message = "campo status vazio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false, length = 20)
    private Status status;

    private LocalDateTime data_fim;

    @NotNull(message = "Campo vazio")
    private Boolean necessario_jejum;
    private String instrucao;

    @NotNull(message = "Campo intervalo vazio")
    private Double intervalo;

    @NotBlank(message = "Campo dosagem vazio")
    private String dosagem;

    
}
