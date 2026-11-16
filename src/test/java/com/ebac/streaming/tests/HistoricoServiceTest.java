package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.entities.HistoricoFilme;
import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.repository.HistoricoFilmeRepository;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.responses.HistoricoItemResponse;
import com.ebac.streaming.services.HistoricoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {
    @Mock
    UsuarioRepository usuarioRepo;
    @Mock
    HistoricoFilmeRepository historicoRepo;
    @InjectMocks
    HistoricoService service;

    private HistoricoFilme item(Filme f, int seq) {
        HistoricoFilme h = new HistoricoFilme();
        h.setFilme(f);
        h.setSequencia(seq);
        h.setAssistidoEm(LocalDateTime.now());
        return h;
    }
    private Filme filme(String titulo) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        return f;
    }

    @Test
    void mantemAOrdemERepeteOsFilmes() {
        UUID id = UUID.randomUUID();
        Filme a = filme("A");
        Filme b = filme("B");
        when(usuarioRepo.existsById(id)).thenReturn(true);
        when(historicoRepo.findByUsuarioIdOrderBySequenciaAsc(id))
                .thenReturn(List.of(item(a, 1), item(b, 2), item(a, 3), item(a, 4), item(a, 5)));

        var lista = service.listar(id);

        assertEquals(List.of("A", "B", "A", "A", "A"),
                lista.stream().map(HistoricoItemResponse::titulo).toList());
        assertEquals(List.of(1, 2, 3, 4, 5),
                lista.stream().map(HistoricoItemResponse::sequencia).toList());
    }

    @Test void usuarioSemFilmesTemHistoricoVazio() {
        UUID id = UUID.randomUUID();
        when(usuarioRepo.existsById(id)).thenReturn(true);
        when(historicoRepo.findByUsuarioIdOrderBySequenciaAsc(id)).thenReturn(List.of());
        assertTrue(service.listar(id).isEmpty());
    }

    @Test void usuarioInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(usuarioRepo.existsById(id)).thenReturn(false);
        assertThrows(RecursoNaoEncontradoException.class, () -> service.listar(id));
    }
}
