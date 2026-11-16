package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.repository.FilmeRepository;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.responses.FilmeListagemResponse;
import com.ebac.streaming.services.FilmeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilmeServiceTest {
    @Mock
    UsuarioRepository usuarioRepo;

    @Mock
    FilmeRepository filmeRepo;

    @InjectMocks
    FilmeService service;

    private Filme filme(String titulo, ClassificacaoEtaria c, Plano p) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        f.setClassificacao(c);
        f.setPlanoMinimo(p);
        return f;
    }

    private Usuario assinante(Plano plano, Perfil perfil) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setPlano(plano);
        u.setPerfil(perfil);
        return u;
    }

    private Usuario vinculado(Usuario assinante, Perfil perfil, boolean permissaoAdulto) {
        Usuario v = new Usuario();
        v.setId(UUID.randomUUID());
        v.setAssinante(assinante);
        v.setPerfil(perfil);
        v.setPermissaoFilmesAdultos(permissaoAdulto);
        return v;
    }

    private final List<Filme> catalogo = List.of(
            filme("Desenho", ClassificacaoEtaria.LIVRE, Plano.BASIC),
            filme("Aventura", ClassificacaoEtaria.DOZE, Plano.STANDARD),
            filme("Suspense", ClassificacaoEtaria.DEZESSEIS, Plano.STANDARD),
            filme("Terror Exclusivo", ClassificacaoEtaria.DEZOITO, Plano.PREMIUM)
    );

    private List<String> titulos(List<FilmeListagemResponse> lista) {
        return lista.stream().map(FilmeListagemResponse::titulo).toList();
    }

    @Test
    void premiumAdultoVeTodosOsFilmesSoComTitulo() {
        Usuario u = assinante(Plano.PREMIUM, Perfil.ADULTO);
        when(usuarioRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(filmeRepo.findAll()).thenReturn(catalogo);

        var lista = service.listarParaUsuario(u.getId());

        assertEquals(4, lista.size());
        assertTrue(lista.stream().allMatch(f -> f.planoMinimo() == null));
    }

    @Test void standardNaoVeFilmeExclusivoPremiumEVeOPlanoMinimo() {
        Usuario u = assinante(Plano.STANDARD, Perfil.ADULTO);
        when(usuarioRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(filmeRepo.findAll()).thenReturn(catalogo);

        var lista = service.listarParaUsuario(u.getId());

        assertEquals(List.of("Desenho", "Aventura", "Suspense"), titulos(lista));
        assertTrue(lista.stream().allMatch(f -> f.planoMinimo() != null));
    }

    @Test void basicSoVeFilmesBasic() {
        Usuario u = assinante(Plano.BASIC, Perfil.ADULTO);
        when(usuarioRepo.findById(u.getId())).thenReturn(Optional.of(u));
        when(filmeRepo.findAll()).thenReturn(catalogo);

        assertEquals(List.of("Desenho"), titulos(service.listarParaUsuario(u.getId())));
    }

    @Test void vinculadoUsaOPlanoDaContaDoAssinante() {
        Usuario a = assinante(Plano.STANDARD, Perfil.ADULTO);
        Usuario v = vinculado(a, Perfil.ADULTO, false);
        when(usuarioRepo.findById(v.getId())).thenReturn(Optional.of(v));
        when(filmeRepo.findAll()).thenReturn(catalogo);

        var lista = service.listarParaUsuario(v.getId());

        assertEquals(List.of("Desenho", "Aventura", "Suspense"), titulos(lista));
    }

    @Test void infantilNaoVeFilmesDeDezesseisNemDezoito() {
        Usuario a = assinante(Plano.PREMIUM, Perfil.ADULTO);
        Usuario v = vinculado(a, Perfil.INFANTIL, false);
        when(usuarioRepo.findById(v.getId())).thenReturn(Optional.of(v));
        when(filmeRepo.findAll()).thenReturn(catalogo);

        assertEquals(List.of("Desenho", "Aventura"), titulos(service.listarParaUsuario(v.getId())));
    }

    @Test void adolescenteSemPermissaoNaoVeDezoito() {
        Usuario a = assinante(Plano.PREMIUM, Perfil.ADULTO);
        Usuario v = vinculado(a, Perfil.ADOLESCENTE, false);
        when(usuarioRepo.findById(v.getId())).thenReturn(Optional.of(v));
        when(filmeRepo.findAll()).thenReturn(catalogo);

        assertFalse(titulos(service.listarParaUsuario(v.getId())).contains("Terror Exclusivo"));
    }

    @Test void adolescenteComPermissaoVeDezoito() {
        Usuario a = assinante(Plano.PREMIUM, Perfil.ADULTO);
        Usuario v = vinculado(a, Perfil.ADOLESCENTE, true);
        when(usuarioRepo.findById(v.getId())).thenReturn(Optional.of(v));
        when(filmeRepo.findAll()).thenReturn(catalogo);

        assertTrue(titulos(service.listarParaUsuario(v.getId())).contains("Terror Exclusivo"));
    }

    @Test void usuarioInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(usuarioRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.listarParaUsuario(id));
    }
}
