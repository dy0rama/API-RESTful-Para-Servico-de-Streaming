package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExclusaoEmCascataTest {
    @Autowired
    UsuarioRepository repo;
    @Autowired
    TestEntityManager em;

    private Usuario novo(String email, Integer idade, Perfil perfil) {
        Usuario u = new Usuario();
        u.setNome("Teste");
        u.setEmail(email);
        u.setIdade(idade);
        u.setPerfil(perfil);
        u.setSenha("x");
        return u;
    }

    @Test
    void excluirAssinanteExcluiTodosOsVinculados() {
        Usuario a = novo("a@x.com", 30, Perfil.ADULTO);
        a.setPlano(Plano.PREMIUM);
        em.persist(a);

        Usuario v1 = novo("v1@x.com", 10, Perfil.INFANTIL);
        v1.setAssinante(a);
        Usuario v2 = novo("v2@x.com", 16, Perfil.ADOLESCENTE);
        v2.setAssinante(a);
        em.persist(v1);
        em.persist(v2);

        UUID idA = a.getId();
        UUID idV1 = v1.getId();
        UUID idV2 = v2.getId();

        em.flush();
        em.clear();   // simula uma nova requisição: a lista de vinculados é recarregada do banco

        repo.delete(repo.findById(a.getId()).orElseThrow());
        repo.flush();

        assertFalse(repo.existsById(idA));
        assertFalse(repo.existsById(idV1));
        assertFalse(repo.existsById(idV2));
    }
}
