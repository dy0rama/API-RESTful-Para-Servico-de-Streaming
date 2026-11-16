package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.exceptions.RegraNegocioException;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.requests.CriarAssinanteRequest;
import com.ebac.streaming.requests.CriarVinculadoRequest;
import com.ebac.streaming.services.FilmeService;
import com.ebac.streaming.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional               // desfaz tudo ao final de cada teste
class MigracaoPlanoIntegracaoTest {

    @Autowired
    UsuarioService service;
    @Autowired
    UsuarioRepository repo;
    @Autowired
    FilmeService filmeService;

    private Usuario assinantePremiumComVinculados(int quantidade) {
        Usuario a = service.criarAssinante(
                new CriarAssinanteRequest("Carla", "carla@x.com", 40, Plano.PREMIUM, "senha123"));
        for (int i = 0; i < quantidade; i++) {
            service.criarVinculado(a.getId(),
                    new CriarVinculadoRequest("V" + i, "v" + i + "@x.com", 20, false, "senha123"));
        }
        return a;
    }

    @Test
    void downgradePremiumParaStandardLiberaDoisNovosVinculados() {
        Usuario a = assinantePremiumComVinculados(4);

        service.migrarPlano(a.getId(), Plano.STANDARD);
        assertEquals(0, repo.countByAssinanteId(a.getId()));

        service.criarVinculado(a.getId(),
                new CriarVinculadoRequest("N1", "n1@x.com", 20, false, "senha123"));
        service.criarVinculado(a.getId(),
                new CriarVinculadoRequest("N2", "n2@x.com", 20, false, "senha123"));
        assertThrows(RegraNegocioException.class, () ->
                service.criarVinculado(a.getId(),
                        new CriarVinculadoRequest("N3", "n3@x.com", 20, false, "senha123")));
    }

    @Test void upgradeMantemVinculadosEEles_herdamOPlanoNovo() {
        Usuario a = service.criarAssinante(
                new CriarAssinanteRequest("Davi", "davi@x.com", 40, Plano.STANDARD, "senha123"));
        Usuario v = service.criarVinculado(a.getId(),
                new CriarVinculadoRequest("Eva", "eva@x.com", 20, false, "senha123"));

        int antes = filmeService.listarParaUsuario(v.getId()).size();
        service.migrarPlano(a.getId(), Plano.PREMIUM);

        assertEquals(1, repo.countByAssinanteId(a.getId()));
        assertTrue(filmeService.listarParaUsuario(v.getId()).size() > antes);
    }
}
