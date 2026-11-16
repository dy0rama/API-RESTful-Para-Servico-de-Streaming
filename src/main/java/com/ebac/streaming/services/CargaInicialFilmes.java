package com.ebac.streaming.services;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.repository.FilmeRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CargaInicialFilmes implements CommandLineRunner {
    private final FilmeRepository repo;

    @Override
    public void run(String @NonNull ... args) {
        if (repo.count() > 0) return;

        repo.saveAll(List.of(
                novo("Aventura na Floresta", "infantil", ClassificacaoEtaria.LIVRE, Plano.BASIC, 4),
                novo("Piratas do Mar", "aventura", ClassificacaoEtaria.DOZE, Plano.BASIC, 7),
                novo("Noite de Comédia", "comédia", ClassificacaoEtaria.DOZE, Plano.STANDARD, 5),
                novo("Amor em Paris", "romance", ClassificacaoEtaria.DEZESSEIS, Plano.STANDARD, 8),
                novo("Galáxia Perdida", "ficção", ClassificacaoEtaria.DEZESSEIS, Plano.PREMIUM, 10),
                novo("A Casa Sombria", "terror", ClassificacaoEtaria.DEZOITO, Plano.STANDARD, 6),
                novo("Sangue Frio", "ação", ClassificacaoEtaria.DEZOITO, Plano.PREMIUM, 9),
                novo("Um Sonho de Liberdade", "drama", ClassificacaoEtaria.DEZESSEIS, Plano.PREMIUM, 7),
                novo("O Poderoso Chefão", "drama/crime", ClassificacaoEtaria.DOZE, Plano.STANDARD, 4),
                novo("Batman: O Cavaleiro das Trevas", "ação", ClassificacaoEtaria.DOZE, Plano.STANDARD, 8),
                novo("O Poderoso Chefão: Parte II", "drama/crime", ClassificacaoEtaria.DOZE, Plano.PREMIUM, 7),
                novo("12 Homens e uma Sentença", "drama", ClassificacaoEtaria.LIVRE, Plano.BASIC, 7),
                novo("O Senhor dos Anéis: O Retorno do Rei", "aventura/fantasia", ClassificacaoEtaria.DOZE, Plano.BASIC, 10),
                novo("A Lista de Schindler", "drama", ClassificacaoEtaria.DEZOITO, Plano.PREMIUM, 9),
                novo("O Senhor dos Anéis: A Sociedade do Anel", "aventura/fantasia", ClassificacaoEtaria.DOZE, Plano.STANDARD, 7),
                novo("Pulp Fiction: Tempo de Violência", "drama/crime", ClassificacaoEtaria.DEZOITO, Plano.PREMIUM, 6),
                novo("Três Homens em Conflito", "aventura/faroeste", ClassificacaoEtaria.DEZESSEIS, Plano.STANDARD, 7),
                novo("O Senhor dos Anéis: As Duas Torres", "aventura/fantasia", ClassificacaoEtaria.DOZE, Plano.PREMIUM, 7),
                novo("Forrest Gump: O Contador de Histórias", "drama/romance", ClassificacaoEtaria.DOZE, Plano.BASIC, 5),
                novo("Clube da Luta", "drama/suspense", ClassificacaoEtaria.DEZOITO, Plano.BASIC, 5),
                novo("A Origem", "ação/aventura/ficção", ClassificacaoEtaria.DOZE, Plano.PREMIUM, 7),
                novo("Star Wars: Episódio V - O Império Contra-Ataca", "ação/aventura/fantasia", ClassificacaoEtaria.LIVRE, Plano.STANDARD, 6),
                novo("Matrix", "ação/ficção", ClassificacaoEtaria.DOZE, Plano.BASIC, 5),
                novo("Os Bons Companheiros", "biografia/crime/drama", ClassificacaoEtaria.DEZESSEIS, Plano.BASIC, 7),
                novo("Um Estranho no Ninho", "Drama", ClassificacaoEtaria.DEZESSEIS, Plano.STANDARD, 4),
                novo("Interestelar", "aventura/drama/ficção", ClassificacaoEtaria.DOZE, Plano.PREMIUM, 7),
                novo("Seven: Os Sete Crimes Capitais", "crime/drama/mistério", ClassificacaoEtaria.DEZESSEIS, Plano.PREMIUM, 8)
        ));
    }

    private Filme novo(String titulo, String genero, ClassificacaoEtaria c, Plano p, int duracao) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        f.setGenero(genero);
        f.setClassificacao(c);
        f.setPlanoMinimo(p);
        f.setDuracaoSegundos(duracao);
        return f;
    }
}
