package com.ebac.streaming.entities;

import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Setter
    @Getter
    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Getter
    private String nome;

    @Setter
    @Getter
    private String email;

    @Setter
    @Getter
    @Column(nullable = false)
    private String senha;   // guardada sempre codificada (BCrypt)

    @Setter
    @Getter
    private Integer idade;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    @Setter
    @Getter
    private boolean permissaoFilmesAdultos;   // só vale para ADOLESCENTE

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Plano plano;  // só preenchido no assinante

    @Setter
    @Getter
    @ManyToOne @JoinColumn(name = "assinante_id")
    private Usuario assinante;                // null = este usuário é o assinante

    @OneToMany(mappedBy = "assinante", cascade = CascadeType.REMOVE)
    private List<Usuario> vinculados = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE)
    private List<HistoricoFilme> historico = new ArrayList<>();

    public Plano getPlanoDaConta() {
        return assinante == null ? plano : assinante.getPlano();
    }
}
