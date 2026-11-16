package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.ClassificacaoEtaria;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.repository.FilmeRepository;
import com.ebac.streaming.requests.CriarAssinanteRequest;
import com.ebac.streaming.requests.CriarVinculadoRequest;
import com.ebac.streaming.services.UsuarioService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class SegurancaTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    FilmeRepository filmeRepo;

    private Usuario novoAssinante(String email, Plano plano) {
        return usuarioService.criarAssinante(
                new CriarAssinanteRequest("Nome", email, 30, plano, "senha123"));
    }
    private Usuario novoVinculado(Usuario assinante, String email, int idade) {
        return usuarioService.criarVinculado(assinante.getId(),
                new CriarVinculadoRequest("Nome", email, idade, false, "senha123"));
    }
    private Filme novoFilme(String titulo, ClassificacaoEtaria c) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        f.setGenero("teste");
        f.setClassificacao(c);
        f.setPlanoMinimo(Plano.BASIC);
        f.setDuracaoSegundos(0);   // sem espera nos testes
        return filmeRepo.save(f);
    }
    private RequestPostProcessor login(String email) {
        return httpBasic(email, "senha123");
    }

    // --- autenticação ---

    @Test
    void cadastroDeAssinanteEPublicoENaoDevolveASenha() throws Exception {
        mvc.perform(post("/usuarios/assinantes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nome":"Ana","email":"ana@x.com","idade":30,"plano":"BASIC","senha":"senha123"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test void semCredenciaisRetorna401() throws Exception {
        mvc.perform(get("/filmes")).andExpect(status().isUnauthorized());
    }

    @Test void senhaErradaRetorna401() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        mvc.perform(get("/filmes").with(httpBasic("ana@x.com", "errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test void usuarioAutenticadoListaFilmes() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        mvc.perform(get("/filmes").with(login("ana@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // --- roles: só o assinante gerencia a conta ---

    @Test void assinanteCriaVinculadoPelaApi() throws Exception {
        novoAssinante("ana@x.com", Plano.STANDARD);
        mvc.perform(post("/conta/usuarios").with(login("ana@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nome":"Teo","email":"teo@x.com","idade":15,"permissaoFilmesAdultos":false,"senha":"senha123"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("ADOLESCENTE"));
    }

    @Test void vinculadoNaoPodeCriarUsuarios() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);
        mvc.perform(post("/conta/usuarios").with(login("teo@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nome":"Novo","email":"novo@x.com","idade":20,"permissaoFilmesAdultos":false,"senha":"senha123"}"""))
                .andExpect(status().isForbidden());
    }

    @Test void vinculadoNaoPodeRemoverOutroUsuario() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        Usuario v1 = novoVinculado(a, "v1@x.com", 20);
        novoVinculado(a, "v2@x.com", 20);
        mvc.perform(delete("/conta/usuarios/{id}", v1.getId()).with(login("v2@x.com")))
                .andExpect(status().isForbidden());
    }

    @Test void vinculadoNaoPodeExcluirAConta() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);
        mvc.perform(delete("/conta").with(login("teo@x.com")))
                .andExpect(status().isForbidden());
    }

    @Test void vinculadoNaoPodeMigrarPlano() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);
        mvc.perform(patch("/conta/plano").with(login("teo@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoPlano\":\"BASIC\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void assinanteNaoRemoveVinculadoDeOutraConta() throws Exception {
        novoAssinante("ana@x.com", Plano.PREMIUM);
        Usuario outra = novoAssinante("bia@x.com", Plano.PREMIUM);
        Usuario deOutra = novoVinculado(outra, "v@x.com", 20);
        mvc.perform(delete("/conta/usuarios/{id}", deOutra.getId()).with(login("ana@x.com")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test void assinanteMigraOProprioPlano() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);
        mvc.perform(patch("/conta/plano").with(login("ana@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoPlano\":\"STANDARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vinculadosRemovidos").value(1));
    }

    // --- cada usuário só opera sobre o próprio perfil ---

    @Test void assistirRegistraNoHistoricoDoUsuarioAutenticado() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        Filme f = novoFilme("Curto", ClassificacaoEtaria.LIVRE);

        mvc.perform(post("/filmes/{id}/assistir", f.getId()).with(login("ana@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segundosAssistidos").value(0));

        mvc.perform(get("/historico").with(login("ana@x.com")))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Curto"));
    }

    @Test void historicoEhSempreODoUsuarioAutenticado() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.STANDARD);
        novoVinculado(a, "teo@x.com", 20);
        Filme f = novoFilme("Curto", ClassificacaoEtaria.LIVRE);

        mvc.perform(post("/filmes/{id}/assistir", f.getId()).with(login("ana@x.com")));
        mvc.perform(post("/filmes/{id}/assistir", f.getId()).with(login("ana@x.com")));
        mvc.perform(post("/filmes/{id}/assistir", f.getId()).with(login("teo@x.com")));

        mvc.perform(get("/historico").with(login("ana@x.com")))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/historico").with(login("teo@x.com")))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test void vinculadoInfantilNaoAssisteFilmeDezoitoEVeMensagem() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "kid@x.com", 8);
        Filme f = novoFilme("Adulto", ClassificacaoEtaria.DEZOITO);

        mvc.perform(post("/filmes/{id}/assistir", f.getId()).with(login("kid@x.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test void senhaErradaRetornaMensagemExplicativa() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        mvc.perform(get("/filmes").with(httpBasic("ana@x.com", "errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha inválidos"));
    }

    @Test void emailInexistenteRetornaAMesmaMensagem() throws Exception {
        mvc.perform(get("/filmes").with(httpBasic("ninguem@x.com", "senha123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha inválidos"));
    }

    @Test void semCredenciaisPedeAutenticacao() throws Exception {
        mvc.perform(get("/filmes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária: informe e-mail e senha"));
    }

    @Test void respostaNaoAbrePopupDeLoginDoNavegador() throws Exception {
        mvc.perform(get("/filmes").with(httpBasic("ana@x.com", "errada")))
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test void authMeIdentificaOVinculadoComoUsuario() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);
        mvc.perform(get("/auth/me").with(login("teo@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("USUARIO"));
    }

    @Test void authMeComSenhaErradaRetornaMensagem() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        mvc.perform(get("/auth/me").with(httpBasic("ana@x.com", "errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha inválidos"));
    }

    @Test void authMeComCredenciaisValidasIdentificaOAssinante() throws Exception {
        novoAssinante("ana@x.com", Plano.STANDARD);
        mvc.perform(get("/auth/me").with(login("ana@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@x.com"))
                .andExpect(jsonPath("$.papel").value("ASSINANTE"))
                .andExpect(jsonPath("$.plano").value("STANDARD"));
    }

    @Test void authMeIdentificaOVinculadoComOPlanoDaConta() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);
        mvc.perform(get("/auth/me").with(login("teo@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("USUARIO"))
                .andExpect(jsonPath("$.plano").value("PREMIUM"));
    }

    @Test void authMeRefleteOPlanoDepoisDaMigracao() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.BASIC);

        mvc.perform(patch("/conta/plano").with(login("ana@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoPlano\":\"PREMIUM\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/auth/me").with(login("ana@x.com")))
                .andExpect(jsonPath("$.plano").value("PREMIUM"));
    }

    @Test void criarVinculadoDevolveLocationDoRecursoCriado() throws Exception {
        novoAssinante("ana@x.com", Plano.STANDARD);

        MvcResult r = mvc.perform(post("/conta/usuarios").with(login("ana@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"nome":"Teo","email":"teo@x.com","idade":15,"permissaoFilmesAdultos":false,"senha":"senha123"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();

        String id = JsonPath.read(r.getResponse().getContentAsString(), "$.id");
        assertTrue(r.getResponse().getHeader("Location").endsWith("/conta/usuarios/" + id));
    }

    @Test void locationApontaParaUmaRotaQueFunciona() throws Exception {
        novoAssinante("ana@x.com", Plano.STANDARD);

        MvcResult r = mvc.perform(post("/conta/usuarios").with(login("ana@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"nome":"Teo","email":"teo@x.com","idade":15,"permissaoFilmesAdultos":false,"senha":"senha123"}"""))
                .andReturn();

        mvc.perform(get(r.getResponse().getHeader("Location")).with(login("ana@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("teo@x.com"))
                .andExpect(jsonPath("$.perfil").value("ADOLESCENTE"));
    }

    @Test void assinanteNaoConsultaVinculadoDeOutraConta() throws Exception {
        novoAssinante("ana@x.com", Plano.PREMIUM);
        Usuario outra = novoAssinante("bia@x.com", Plano.PREMIUM);
        Usuario deOutra = novoVinculado(outra, "v@x.com", 20);

        mvc.perform(get("/conta/usuarios/{id}", deOutra.getId()).with(login("ana@x.com")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test void vinculadoNaoConsultaUsuarios() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        Usuario v1 = novoVinculado(a, "v1@x.com", 20);
        novoVinculado(a, "v2@x.com", 20);

        mvc.perform(get("/conta/usuarios/{id}", v1.getId()).with(login("v2@x.com")))
                .andExpect(status().isForbidden());
    }

    @Test void cadastroDeAssinanteNaoTemLocation() throws Exception {
        mvc.perform(post("/usuarios/assinantes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"nome":"Ana","email":"ana@x.com","idade":30,"plano":"BASIC","senha":"senha123"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test void vinculadoRecebeMensagemExplicativaNo403() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);

        mvc.perform(delete("/conta").with(login("teo@x.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Operação permitida somente para o usuário assinante"));
    }

    @Test void assinanteVeAContaComPlanoEVinculados() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.STANDARD);
        novoVinculado(a, "teo@x.com", 15);

        mvc.perform(get("/conta").with(login("ana@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plano").value("STANDARD"))
                .andExpect(jsonPath("$.limiteVinculados").value(2))
                .andExpect(jsonPath("$.vagasDisponiveis").value(1))
                .andExpect(jsonPath("$.assinante.email").value("ana@x.com"))
                .andExpect(jsonPath("$.vinculados.length()").value(1))
                .andExpect(jsonPath("$.vinculados[0].email").value("teo@x.com"))
                .andExpect(jsonPath("$.vinculados[0].perfil").value("ADOLESCENTE"));
    }

    @Test void vinculadosVemOrdenadosPorNome() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        usuarioService.criarVinculado(a.getId(), new CriarVinculadoRequest("Zeca", "zeca@x.com", 20, false, "senha123"));
        usuarioService.criarVinculado(a.getId(), new CriarVinculadoRequest("Alice", "alice@x.com", 20, false, "senha123"));

        mvc.perform(get("/conta").with(login("ana@x.com")))
                .andExpect(jsonPath("$.vinculados[0].nome").value("Alice"))
                .andExpect(jsonPath("$.vinculados[1].nome").value("Zeca"));
    }

    @Test void contaNaoExpoeSenhasNemDoAssinanteNemDosVinculados() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);

        mvc.perform(get("/conta").with(login("ana@x.com")))
                .andExpect(jsonPath("$.assinante.senha").doesNotExist())
                .andExpect(jsonPath("$.vinculados[0].senha").doesNotExist());
    }

    @Test void contaRefleteADowngradeSemVinculados() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);

        mvc.perform(patch("/conta/plano").with(login("ana@x.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoPlano\":\"STANDARD\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/conta").with(login("ana@x.com")))
                .andExpect(jsonPath("$.plano").value("STANDARD"))
                .andExpect(jsonPath("$.vagasDisponiveis").value(2))
                .andExpect(jsonPath("$.vinculados.length()").value(0));
    }

    @Test void vinculadoNaoVeADadosDaConta() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);

        mvc.perform(get("/conta").with(login("teo@x.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Operação permitida somente para o usuário assinante"));
    }

    @Test void contaExigeAutenticacao() throws Exception {
        mvc.perform(get("/conta")).andExpect(status().isUnauthorized());
    }
}
