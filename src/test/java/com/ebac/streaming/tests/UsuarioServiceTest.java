package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.exceptions.RegraNegocioException;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.requests.CriarAssinanteRequest;
import com.ebac.streaming.requests.CriarVinculadoRequest;
import com.ebac.streaming.responses.ContaResponse;
import com.ebac.streaming.responses.MigracaoPlanoResponse;
import com.ebac.streaming.responses.SessaoResponse;
import com.ebac.streaming.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock
    UsuarioRepository repo;
    @InjectMocks
    UsuarioService service;
    @Mock
    PasswordEncoder encoder;

    private Usuario assinante(Plano plano) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setPlano(plano);
        return u;
    }
    private CriarVinculadoRequest req(int idade) {
        return new CriarVinculadoRequest("Joao", "joao@x.com", idade, false,"senha123");
    }

    @Test
    void assinanteRecebePerfilAdulto() {
        when(repo.existsByEmail("ana@x.com")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario criado = service.criarAssinante(
                new CriarAssinanteRequest("Ana", "ana@x.com", 30, Plano.STANDARD, "senha123"));

        assertEquals(Perfil.ADULTO, criado.getPerfil());
        assertNull(criado.getAssinante());
        assertEquals(Plano.STANDARD, criado.getPlano());
    }

    @Test void emailDuplicadoDeveSerRejeitado() {
        when(repo.existsByEmail("ana@x.com")).thenReturn(true);
        assertThrows(RegraNegocioException.class, () -> service.criarAssinante(
                new CriarAssinanteRequest("Ana", "ana@x.com", 30, Plano.BASIC, "senha123")));
    }

    @Test void vinculadoRecebePerfilPelaIdade() {
        Usuario a = assinante(Plano.PREMIUM);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.countByAssinanteId(a.getId())).thenReturn(0L);
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario v = service.criarVinculado(a.getId(), req(8));

        assertEquals(Perfil.INFANTIL, v.getPerfil());
        assertEquals(a, v.getAssinante());
    }

    @Test void basicNaoPermiteVinculados() {
        Usuario a = assinante(Plano.BASIC);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));

        assertThrows(RegraNegocioException.class, () -> service.criarVinculado(a.getId(), req(20)));
        verify(repo, never()).save(any());
    }

    @Test void standardNaoPermiteTerceiroVinculado() {
        Usuario a = assinante(Plano.STANDARD);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.countByAssinanteId(a.getId())).thenReturn(2L);

        assertThrows(RegraNegocioException.class, () -> service.criarVinculado(a.getId(), req(20)));
    }

    @Test void premiumNaoPermiteQuintoVinculado() {
        Usuario a = assinante(Plano.PREMIUM);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.countByAssinanteId(a.getId())).thenReturn(4L);

        assertThrows(RegraNegocioException.class, () -> service.criarVinculado(a.getId(), req(20)));
    }

    @Test void assinanteInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.criarVinculado(id, req(20)));
    }

    @Test void naoPodeVincularAUmUsuarioQueNaoEAssinante() {
        Usuario a = assinante(Plano.PREMIUM);
        Usuario vinculado = new Usuario();
        vinculado.setId(UUID.randomUUID());
        vinculado.setAssinante(a);
        when(repo.findById(vinculado.getId())).thenReturn(Optional.of(vinculado));

        assertThrows(RegraNegocioException.class, () -> service.criarVinculado(vinculado.getId(), req(20)));
    }

    @Test void permissaoAdultoSoValeParaAdolescente() {
        Usuario a = assinante(Plano.PREMIUM);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.countByAssinanteId(a.getId())).thenReturn(0L);
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario crianca = service.criarVinculado(a.getId(),
                new CriarVinculadoRequest("Lia", "lia@x.com", 8, true, "senha123"));
        assertFalse(crianca.isPermissaoFilmesAdultos());

        Usuario adolescente = service.criarVinculado(a.getId(),
                new CriarVinculadoRequest("Teo", "teo@x.com", 15, true, "senha123"));
        assertTrue(adolescente.isPermissaoFilmesAdultos());
    }

    private Usuario vinculadoDe(Usuario assinante) {
        Usuario v = new Usuario();
        v.setId(UUID.randomUUID());
        v.setAssinante(assinante);
        return v;
    }

    @Test void assinanteRemoveSeuVinculado() {
        Usuario a = assinante(Plano.PREMIUM);
        Usuario v = vinculadoDe(a);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        service.removerVinculado(a.getId(), v.getId());

        verify(repo).delete(v);
    }

    @Test void naoPodeRemoverVinculadoDeOutraConta() {
        Usuario a1 = assinante(Plano.PREMIUM);
        Usuario a2 = assinante(Plano.PREMIUM);
        Usuario v = vinculadoDe(a2);
        when(repo.findById(a1.getId())).thenReturn(Optional.of(a1));
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        assertThrows(RegraNegocioException.class, () -> service.removerVinculado(a1.getId(), v.getId()));
        verify(repo, never()).delete(any());
    }

    @Test void naoAssinanteNaoPodeRemoverUsuarios() {
        Usuario a = assinante(Plano.PREMIUM);
        Usuario naoAssinante = vinculadoDe(a);
        when(repo.findById(naoAssinante.getId())).thenReturn(Optional.of(naoAssinante));

        assertThrows(RegraNegocioException.class,
                () -> service.removerVinculado(naoAssinante.getId(), UUID.randomUUID()));
        verify(repo, never()).delete(any());
    }

    @Test void assinanteExcluiPropriaConta() {
        Usuario a = assinante(Plano.STANDARD);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));

        service.excluirConta(a.getId());

        verify(repo).delete(a);
    }

    @Test void naoAssinanteNaoPodeExcluirConta() {
        Usuario a = assinante(Plano.STANDARD);
        Usuario v = vinculadoDe(a);
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        assertThrows(RegraNegocioException.class, () -> service.excluirConta(v.getId()));
        verify(repo, never()).delete(any());
    }

    @Test void excluirContaInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluirConta(id));
    }

    @ParameterizedTest
    @CsvSource({"BASIC,STANDARD", "BASIC,PREMIUM", "STANDARD,PREMIUM"})
    void upgradeMantemOsVinculados(Plano de, Plano para) {
        Usuario a = assinante(de);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));

        MigracaoPlanoResponse resp = service.migrarPlano(a.getId(), para);

        assertEquals(para, a.getPlano());
        assertEquals(0, resp.vinculadosRemovidos());
        verify(repo, never()).deleteAll(any());
    }

    @ParameterizedTest
    @CsvSource({"PREMIUM,STANDARD", "PREMIUM,BASIC", "STANDARD,BASIC"})
    void downgradeExcluiTodosOsVinculados(Plano de, Plano para) {
        Usuario a = assinante(de);
        List<Usuario> vinculados = List.of(vinculadoDe(a), vinculadoDe(a));
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.findByAssinanteId(a.getId())).thenReturn(vinculados);

        MigracaoPlanoResponse resp = service.migrarPlano(a.getId(), para);

        assertEquals(para, a.getPlano());
        assertEquals(2, resp.vinculadosRemovidos());
        verify(repo).deleteAll(vinculados);
    }

    @Test void migrarParaOMesmoPlanoDeveSerRejeitado() {
        Usuario a = assinante(Plano.STANDARD);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));

        assertThrows(RegraNegocioException.class,
                () -> service.migrarPlano(a.getId(), Plano.STANDARD));
    }

    @Test void naoAssinanteNaoPodeMigrarPlano() {
        Usuario a = assinante(Plano.PREMIUM);
        Usuario v = vinculadoDe(a);
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        assertThrows(RegraNegocioException.class,
                () -> service.migrarPlano(v.getId(), Plano.BASIC));
        assertEquals(Plano.PREMIUM, a.getPlano());
    }

    @Test void migrarPlanoDeAssinanteInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.migrarPlano(id, Plano.BASIC));
    }

    @Test void senhaDoAssinanteEhGuardadaCodificada() {
        when(repo.existsByEmail("ana@x.com")).thenReturn(false);
        when(encoder.encode("senha123")).thenReturn("HASH");
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario criado = service.criarAssinante(
                new CriarAssinanteRequest("Ana", "ana@x.com", 30, Plano.STANDARD, "senha123"));

        assertEquals("HASH", criado.getSenha());
    }

    @Test void senhaDoVinculadoEhGuardadaCodificada() {
        Usuario a = assinante(Plano.PREMIUM);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.countByAssinanteId(a.getId())).thenReturn(0L);
        when(encoder.encode("senha123")).thenReturn("HASH");
        when(repo.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario v = service.criarVinculado(a.getId(),
                new CriarVinculadoRequest("Teo", "teo@x.com", 15, false, "senha123"));

        assertEquals("HASH", v.getSenha());
    }

    @Test void sessaoDoAssinanteTrazOPlanoDele() {
        Usuario a = assinante(Plano.STANDARD);
        a.setEmail("ana@x.com");
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));

        SessaoResponse s = service.sessaoDe(a.getId());

        assertEquals(Plano.STANDARD, s.plano());
        assertEquals("ASSINANTE", s.papel());
    }

    @Test void sessaoDoVinculadoTrazOPlanoDoAssinante() {
        Usuario a = assinante(Plano.PREMIUM);
        Usuario v = vinculadoDe(a);
        v.setEmail("teo@x.com");
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        SessaoResponse s = service.sessaoDe(v.getId());

        assertEquals(Plano.PREMIUM, s.plano());
        assertEquals("USUARIO", s.papel());
    }

    @Test void sessaoDeUsuarioInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.sessaoDe(id));
    }

    @Test void assinanteBuscaSeuVinculado() {
        Usuario a = assinante(Plano.PREMIUM);
        Usuario v = vinculadoDe(a);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        assertEquals(v, service.buscarVinculado(a.getId(), v.getId()));
    }

    @Test void naoPodeBuscarVinculadoDeOutraConta() {
        Usuario a1 = assinante(Plano.PREMIUM);
        Usuario a2 = assinante(Plano.PREMIUM);
        Usuario v = vinculadoDe(a2);
        when(repo.findById(a1.getId())).thenReturn(Optional.of(a1));
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        assertThrows(RegraNegocioException.class, () -> service.buscarVinculado(a1.getId(), v.getId()));
    }

    @Test void buscarVinculadoInexistenteDeveLancarNaoEncontrado() {
        Usuario a = assinante(Plano.PREMIUM);
        UUID id = UUID.randomUUID();
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarVinculado(a.getId(), id));
    }

    @Test void detalhaAContaComPlanoLimiteEVinculados() {
        Usuario a = assinante(Plano.STANDARD);
        Usuario v1 = vinculadoDe(a);
        Usuario v2 = vinculadoDe(a);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.findByAssinanteIdOrderByNomeAsc(a.getId())).thenReturn(List.of(v1, v2));

        ContaResponse conta = service.detalharConta(a.getId());

        assertEquals(Plano.STANDARD, conta.plano());
        assertEquals(2, conta.limiteVinculados());
        assertEquals(0, conta.vagasDisponiveis());
        assertEquals(2, conta.vinculados().size());
        assertEquals(a.getId(), conta.assinante().id());
    }

    @Test void vagasDisponiveisSaoOLimiteMenosOsVinculados() {
        Usuario a = assinante(Plano.PREMIUM);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.findByAssinanteIdOrderByNomeAsc(a.getId())).thenReturn(List.of(vinculadoDe(a)));

        ContaResponse conta = service.detalharConta(a.getId());

        assertEquals(4, conta.limiteVinculados());
        assertEquals(3, conta.vagasDisponiveis());
    }

    @Test void contaBasicNaoTemVagasNemVinculados() {
        Usuario a = assinante(Plano.BASIC);
        when(repo.findById(a.getId())).thenReturn(Optional.of(a));
        when(repo.findByAssinanteIdOrderByNomeAsc(a.getId())).thenReturn(List.of());

        ContaResponse conta = service.detalharConta(a.getId());

        assertEquals(0, conta.limiteVinculados());
        assertEquals(0, conta.vagasDisponiveis());
        assertTrue(conta.vinculados().isEmpty());
    }

    @Test void naoAssinanteNaoDetalhaAConta() {
        Usuario a = assinante(Plano.PREMIUM);
        Usuario v = vinculadoDe(a);
        when(repo.findById(v.getId())).thenReturn(Optional.of(v));

        assertThrows(RegraNegocioException.class, () -> service.detalharConta(v.getId()));
    }

    @Test void detalharContaInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.detalharConta(id));
    }
}
