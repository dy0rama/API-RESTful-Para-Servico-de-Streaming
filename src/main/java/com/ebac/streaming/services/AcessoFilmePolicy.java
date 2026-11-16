package com.ebac.streaming.services;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;

import java.util.Optional;

public final class AcessoFilmePolicy {
    private AcessoFilmePolicy() {}

    public static boolean podeAssistir(Plano planoDaConta, Perfil perfil,
                                       boolean permissaoAdulto, Filme filme) {
        return motivoNegado(planoDaConta, perfil, permissaoAdulto, filme).isEmpty();
    }

    public static Optional<String> motivoNegado(Plano planoDaConta, Perfil perfil,
                                                boolean permissaoAdulto, Filme filme) {
        if (!planoDaConta.permiteAcessoA(filme.getPlanoMinimo())) {
            return Optional.of("O plano %s da conta não dá acesso a este filme, que exige o plano %s."
                    .formatted(planoDaConta, filme.getPlanoMinimo()));
        }
        if (!perfil.permite(filme.getClassificacao(), permissaoAdulto)) {
            if (perfil == Perfil.ADOLESCENTE && filme.getClassificacao() == ClassificacaoEtaria.DEZOITO) {
                return Optional.of("Este filme é de classificação 18 anos e o assinante da conta "
                        + "não liberou filmes adultos para o seu perfil.");
            }
            return Optional.of("Este filme tem %s e não é permitido para o perfil %s."
                    .formatted(rotulo(filme.getClassificacao()), perfil));
        }
        return Optional.empty();
    }

    private static String rotulo(ClassificacaoEtaria c) {
        return c == ClassificacaoEtaria.LIVRE ? "classificação livre"
                : "classificação de " + c.anos() + " anos";
    }
}
