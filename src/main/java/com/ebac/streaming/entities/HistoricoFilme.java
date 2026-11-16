package com.ebac.streaming.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_filmes")
public class HistoricoFilme {
    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Getter
    @ManyToOne
    private Usuario usuario;

    @Setter
    @Getter
    @ManyToOne
    private Filme filme;

    @Setter
    @Getter
    private LocalDateTime assistidoEm;        // garante a ordem; cada exibição é uma linha

    @Setter
    @Getter
    @Column(nullable = false)
    private Integer sequencia;
}
