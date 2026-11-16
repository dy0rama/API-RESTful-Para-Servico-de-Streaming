package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.repository.FilmeRepository;
import com.ebac.streaming.repository.HistoricoFilmeRepository;
import com.ebac.streaming.requests.CriarAssinanteRequest;
import com.ebac.streaming.requests.CriarVinculadoRequest;
import com.ebac.streaming.responses.HistoricoItemResponse;
import com.ebac.streaming.services.AssistirFilmeService;
import com.ebac.streaming.services.HistoricoService;
import com.ebac.streaming.services.UsuarioService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class HistoricoIntegracaoTest {
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    AssistirFilmeService assistirService;
    @Autowired
    HistoricoService historicoService;
    @Autowired
    FilmeRepository filmeRepo;
    @Autowired
    HistoricoFilmeRepository historicoRepo;
    @Autowired
    EntityManager em;

    private Filme filmeRapido(String titulo) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        f.setGenero("teste");
        f.setClassificacao(ClassificacaoEtaria.LIVRE);
        f.setPlanoMinimo(Plano.BASIC);
        f.setDuracaoSegundos(0);
        return filmeRepo.save(f);
    }

    @Test
    void filmeAssistidoVariasVezesApareceVariasVezesNaOrdem() {
        Usuario a = usuarioService.criarAssinante(
                new CriarAssinanteRequest("Rita", "rita@x.com", 30, Plano.BASIC, "senha123"));
        Filme x = filmeRapido("X");
        Filme y = filmeRapido("Y");

        assistirService.assistir(a.getId(), x.getId());
        assistirService.assistir(a.getId(), y.getId());
        assistirService.assistir(a.getId(), x.getId());
        assistirService.assistir(a.getId(), x.getId());

        var historico = historicoService.listar(a.getId());

        assertEquals(List.of("X", "Y", "X", "X"),
                historico.stream().map(HistoricoItemResponse::titulo).toList());
        assertEquals(List.of(1, 2, 3, 4),
                historico.stream().map(HistoricoItemResponse::sequencia).toList());
    }

    @Test void cadaUsuarioTemOSeuProprioHistorico() {
        Usuario a = usuarioService.criarAssinante(
                new CriarAssinanteRequest("Rita", "rita2@x.com", 30, Plano.STANDARD, "senha123"));
        Usuario v = usuarioService.criarVinculado(a.getId(),
                new CriarVinculadoRequest("Davi", "davi2@x.com", 20, false, "senha123"));
        Filme x = filmeRapido("X");

        assistirService.assistir(a.getId(), x.getId());
        assistirService.assistir(v.getId(), x.getId());
        assistirService.assistir(v.getId(), x.getId());

        assertEquals(1, historicoService.listar(a.getId()).size());
        assertEquals(2, historicoService.listar(v.getId()).size());
        assertEquals(1, historicoService.listar(v.getId()).getFirst().sequencia());
    }

    @Test void excluirContaApagaOHistoricoDoAssinanteEDosVinculados() {
        Usuario a = usuarioService.criarAssinante(
                new CriarAssinanteRequest("Rita", "rita3@x.com", 30, Plano.STANDARD, "senha123"));
        Usuario v = usuarioService.criarVinculado(a.getId(),
                new CriarVinculadoRequest("Davi", "davi3@x.com", 20, false, "senha123"));
        Filme x = filmeRapido("X");
        assistirService.assistir(a.getId(), x.getId());
        assistirService.assistir(v.getId(), x.getId());
        em.flush();
        em.clear();   // força a cascata a recarregar as listas do banco

        UUID idA = a.getId();
        UUID idV1 = v.getId();

        usuarioService.excluirConta(a.getId());
        em.flush();

        assertFalse(historicoRepo.existsById(idA));
        assertFalse(historicoRepo.existsById(idV1));
    }
}
