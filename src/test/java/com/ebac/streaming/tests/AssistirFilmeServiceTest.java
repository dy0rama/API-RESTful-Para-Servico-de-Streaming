package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.entities.HistoricoFilme;
import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.exceptions.AcessoNegadoException;
import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.repository.FilmeRepository;
import com.ebac.streaming.repository.HistoricoFilmeRepository;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.responses.AssistirFilmeResponse;
import com.ebac.streaming.services.AssistirFilmeService;
import com.ebac.streaming.services.SimuladorReproducao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssistirFilmeServiceTest {

    @Mock
    UsuarioRepository usuarioRepo;
    @Mock
    FilmeRepository filmeRepo;
    @Mock
    HistoricoFilmeRepository historicoRepo;
    @Mock
    SimuladorReproducao simulador;
    @InjectMocks
    AssistirFilmeService service;

    private Usuario assinante(Plano plano, Perfil perfil) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setPlano(plano);
        u.setPerfil(perfil);
        return u;
    }
    private Filme filme(ClassificacaoEtaria c, Plano p, int duracao) {
        Filme f = new Filme();
        f.setId(UUID.randomUUID());
        f.setTitulo("Filme X");
        f.setClassificacao(c);
        f.setPlanoMinimo(p);
        f.setDuracaoSegundos(duracao);
        return f;
    }

    @Test
    void acessoPermitidoSimulaDuracaoERegistraNoHistorico() {
        Usuario u = assinante(Plano.PREMIUM, Perfil.ADULTO);
        Filme f = filme(ClassificacaoEtaria.DEZOITO, Plano.PREMIUM, 7);
        when(usuarioRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(filmeRepo.findById(f.getId())).thenReturn(Optional.of(f));
        when(historicoRepo.countByUsuarioId(u.getId())).thenReturn(0L);

        AssistirFilmeResponse resp = service.assistir(u.getId(), f.getId());

        verify(simulador).reproduzir(7);
        ArgumentCaptor<HistoricoFilme> captor = ArgumentCaptor.forClass(HistoricoFilme.class);
        verify(historicoRepo).save(captor.capture());
        assertEquals(u, captor.getValue().getUsuario());
        assertEquals(f, captor.getValue().getFilme());
        assertEquals(1, captor.getValue().getSequencia());
        assertNotNull(captor.getValue().getAssistidoEm());
        assertEquals(7, resp.segundosAssistidos());
        assertEquals("Filme X", resp.titulo());
    }

    @Test void sequenciaContinuaDaQuantidadeJaAssistida() {
        Usuario u = assinante(Plano.PREMIUM, Perfil.ADULTO);
        Filme f = filme(ClassificacaoEtaria.LIVRE, Plano.BASIC, 3);
        when(usuarioRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(filmeRepo.findById(f.getId())).thenReturn(Optional.of(f));
        when(historicoRepo.countByUsuarioId(u.getId())).thenReturn(3L);

        service.assistir(u.getId(), f.getId());

        ArgumentCaptor<HistoricoFilme> captor = ArgumentCaptor.forClass(HistoricoFilme.class);
        verify(historicoRepo).save(captor.capture());
        assertEquals(4, captor.getValue().getSequencia());
    }

    @Test void negadoPorIdadeNaoSimulaNemRegistra() {
        Usuario a = assinante(Plano.PREMIUM, Perfil.ADULTO);
        Usuario v = new Usuario();
        v.setId(UUID.randomUUID());
        v.setAssinante(a);
        v.setPerfil(Perfil.INFANTIL);
        Filme f = filme(ClassificacaoEtaria.DEZOITO, Plano.BASIC, 5);
        when(usuarioRepo.findById(v.getId())).thenReturn(Optional.of(v));
        when(filmeRepo.findById(f.getId())).thenReturn(Optional.of(f));

        var ex = assertThrows(AcessoNegadoException.class, () -> service.assistir(v.getId(), f.getId()));

        assertFalse(ex.getMessage().isBlank());
        verify(simulador, never()).reproduzir(anyInt());
        verify(historicoRepo, never()).save(any());
    }

    @Test void negadoPorPlanoNaoSimulaNemRegistra() {
        Usuario u = assinante(Plano.BASIC, Perfil.ADULTO);
        Filme f = filme(ClassificacaoEtaria.LIVRE, Plano.PREMIUM, 5);
        when(usuarioRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(filmeRepo.findById(f.getId())).thenReturn(Optional.of(f));

        assertThrows(AcessoNegadoException.class, () -> service.assistir(u.getId(), f.getId()));
        verify(simulador, never()).reproduzir(anyInt());
        verify(historicoRepo, never()).save(any());
    }

    @Test void vinculadoUsaOPlanoDaContaDoAssinante() {
        Usuario a = assinante(Plano.BASIC, Perfil.ADULTO);
        Usuario v = new Usuario();
        v.setId(UUID.randomUUID());
        v.setAssinante(a);
        v.setPerfil(Perfil.ADULTO);
        Filme f = filme(ClassificacaoEtaria.LIVRE, Plano.STANDARD, 2);
        when(usuarioRepo.findById(v.getId())).thenReturn(Optional.of(v));
        when(filmeRepo.findById(f.getId())).thenReturn(Optional.of(f));

        assertThrows(AcessoNegadoException.class, () -> service.assistir(v.getId(), f.getId()));
    }

    @Test void usuarioInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(usuarioRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.assistir(id, UUID.randomUUID()));
    }

    @Test void filmeInexistenteDeveLancarNaoEncontrado() {
        Usuario u = assinante(Plano.PREMIUM, Perfil.ADULTO);
        UUID filmeId = UUID.randomUUID();
        when(usuarioRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(filmeRepo.findById(filmeId)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.assistir(u.getId(), filmeId));
    }
}
