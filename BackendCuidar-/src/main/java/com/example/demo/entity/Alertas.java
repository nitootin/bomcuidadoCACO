package com.example.demo.entity;

import com.example.demo.enums.StatusAlertas;
import com.example.demo.enums.TipoAlerta;

import java.time.LocalDateTime;

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
@ToString
@Table(name = "alertas")
public class Alertas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotNull(message = "idoso obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_idoso", nullable = false, foreignKey = @ForeignKey(name = "fk_idoso_alerta"))
    private Idoso idoso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prescricao", foreignKey = @ForeignKey(name = "fk_prescricao_alerta"))
    private Prescricao prescricao;

    @NotNull(message = "tipo do alerta obrigatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoAlerta", nullable = false, length = 20)
    private TipoAlerta tipoAlerta;

    @NotNull(message = "Status do alerta é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "statusAlerta", nullable = false, length = 20)
    private StatusAlertas statusAlertas;

    @NotNull(message = "Data de criação é obrigatória")
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime data_criacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime data_atualizacao;

    @Column(name = "data_agendade")
    private LocalDateTime data_agendade;
}
