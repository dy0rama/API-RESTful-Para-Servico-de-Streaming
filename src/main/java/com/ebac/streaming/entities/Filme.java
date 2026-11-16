package com.ebac.streaming.entities;

import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Plano;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "filmes")
public class Filme {
    @Setter
    @Getter
    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Getter
    private String titulo;

    @Setter
    @Getter
    private String genero;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private ClassificacaoEtaria classificacao;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Plano planoMinimo;

    @Setter
    @Getter
    @Min(0) @Max(10)
    private Integer duracaoSegundos;          // 0 a 10

}
