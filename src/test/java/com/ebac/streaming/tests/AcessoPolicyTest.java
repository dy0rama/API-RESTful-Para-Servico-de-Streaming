package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.services.AcessoFilmePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcessoFilmePolicyTest {
    @Test
    void infantilAssisteLivreEDoze() {
        assertTrue(Perfil.INFANTIL.permite(ClassificacaoEtaria.LIVRE, false));
        assertTrue(Perfil.INFANTIL.permite(ClassificacaoEtaria.DOZE, false));
        assertFalse(Perfil.INFANTIL.permite(ClassificacaoEtaria.DEZESSEIS, false));
        assertFalse(Perfil.INFANTIL.permite(ClassificacaoEtaria.DEZOITO, true));
    }
    @Test
    void adolescenteSemPermissaoNaoAssisteDezoito() {
        assertTrue(Perfil.ADOLESCENTE.permite(ClassificacaoEtaria.DEZESSEIS, false));
        assertFalse(Perfil.ADOLESCENTE.permite(ClassificacaoEtaria.DEZOITO, false));
    }
    @Test
    void adolescenteComPermissaoAssisteDezoito() {
        assertTrue(Perfil.ADOLESCENTE.permite(ClassificacaoEtaria.DEZOITO, true));
    }
    @Test
    void adultoAssisteTudo() {
        for (ClassificacaoEtaria c : ClassificacaoEtaria.values()) {
            assertTrue(Perfil.ADULTO.permite(c, false));
        }
    }
    @Test
    void planoOkMasIdadeNegadaNaoPodeAssistir() {
        Filme f = new Filme();
        f.setPlanoMinimo(Plano.BASIC);
        f.setClassificacao(ClassificacaoEtaria.DEZOITO);
        assertFalse(AcessoFilmePolicy.podeAssistir(Plano.PREMIUM, Perfil.INFANTIL, false, f));
    }
    @Test
    void idadeOkMasPlanoNegadoNaoPodeAssistir() {
        Filme f = new Filme();
        f.setPlanoMinimo(Plano.PREMIUM);
        f.setClassificacao(ClassificacaoEtaria.LIVRE);
        assertFalse(AcessoFilmePolicy.podeAssistir(Plano.BASIC, Perfil.ADULTO, false, f));
    }

    private Filme filme(ClassificacaoEtaria c, Plano p) {
        Filme f = new Filme();
        f.setClassificacao(c);
        f.setPlanoMinimo(p);
        return f;
    }

    @Test void acessoPermitidoNaoTemMotivo() {
        var motivo = AcessoFilmePolicy.motivoNegado(
                Plano.STANDARD, Perfil.ADULTO, false, filme(ClassificacaoEtaria.DEZOITO, Plano.STANDARD));
        assertTrue(motivo.isEmpty());
    }

    @Test void motivoPorPlanoCitaOPlanoDaContaEOExigido() {
        var motivo = AcessoFilmePolicy.motivoNegado(
                Plano.BASIC, Perfil.ADULTO, false, filme(ClassificacaoEtaria.LIVRE, Plano.PREMIUM));
        assertTrue(motivo.orElseThrow().contains("BASIC"));
        assertTrue(motivo.orElseThrow().contains("PREMIUM"));
    }

    @Test void motivoPorIdadeCitaAClassificacaoEOPerfil() {
        var motivo = AcessoFilmePolicy.motivoNegado(
                Plano.PREMIUM, Perfil.INFANTIL, false, filme(ClassificacaoEtaria.DEZESSEIS, Plano.BASIC));
        assertTrue(motivo.orElseThrow().contains("16"));
        assertTrue(motivo.orElseThrow().contains("INFANTIL"));
    }

    @Test void adolescenteSemPermissaoRecebeMensagemSobreALiberacaoDoAssinante() {
        var motivo = AcessoFilmePolicy.motivoNegado(
                Plano.PREMIUM, Perfil.ADOLESCENTE, false, filme(ClassificacaoEtaria.DEZOITO, Plano.BASIC));
        assertTrue(motivo.orElseThrow().contains("assinante"));
    }

    @Test void quandoPlanoEIdadeFalhamAMensagemCitaOPlano() {
        var motivo = AcessoFilmePolicy.motivoNegado(
                Plano.BASIC, Perfil.INFANTIL, false, filme(ClassificacaoEtaria.DEZOITO, Plano.PREMIUM));
        assertTrue(motivo.orElseThrow().contains("plano"));
    }
}
