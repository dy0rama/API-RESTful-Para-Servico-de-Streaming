package com.ebac.streaming.tests;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.Plano;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TokenTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UsuarioService usuarioService;

    private Usuario novoAssinante(String email, Plano plano) {
        return usuarioService.criarAssinante(
                new CriarAssinanteRequest("Nome", email, 30, plano, "senha123"));
    }
    private Usuario novoVinculado(Usuario assinante, String email, int idade) {
        return usuarioService.criarVinculado(assinante.getId(),
                new CriarVinculadoRequest("Nome", email, idade, false, "senha123"));
    }
    private ResultActions pedirToken(String email, String senha) throws Exception {
        return mvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "password")
                .param("username", email)
                .param("password", senha));
    }
    private String obterToken(String email) throws Exception {
        String corpo = pedirToken(email, "senha123").andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.access_token");
    }

    @Test
    void credenciaisValidasRetornamAccessToken() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        pedirToken("ana@x.com", "senha123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600));
    }

    @Test void senhaErradaRetornaMensagemExplicativa() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        pedirToken("ana@x.com", "errada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_grant"))
                .andExpect(jsonPath("$.error_description").value("E-mail ou senha inválidos"));
    }

    @Test void emailInexistenteRetornaAMesmaMensagem() throws Exception {
        pedirToken("ninguem@x.com", "senha123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error_description").value("E-mail ou senha inválidos"));
    }

    @Test void grantTypeDiferenteDePasswordEhRejeitado() throws Exception {
        mvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials")
                        .param("username", "ana@x.com")
                        .param("password", "senha123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
    }

    // o Swagger UI manda "Authorization: Basic Og==" (client_id e secret vazios) junto do pedido
    @Test void cabecalhoBasicVazioDoSwaggerNaoImpedeOPedidoDeToken() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        mvc.perform(post("/auth/token")
                        .header("Authorization", "Basic Og==")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("username", "ana@x.com")
                        .param("password", "senha123"))
                .andExpect(status().isOk());
    }

    @Test void tokenDaAcessoAosEndpointsProtegidos() throws Exception {
        novoAssinante("ana@x.com", Plano.BASIC);
        String token = obterToken("ana@x.com");

        mvc.perform(get("/filmes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.email").value("ana@x.com"))
                .andExpect(jsonPath("$.papel").value("ASSINANTE"));
    }

    @Test void tokenDoAssinanteAcessaRotasDeConta() throws Exception {
        novoAssinante("ana@x.com", Plano.STANDARD);
        String token = obterToken("ana@x.com");

        mvc.perform(post("/conta/usuarios").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"nome":"Teo","email":"teo@x.com","idade":15,"permissaoFilmesAdultos":false,"senha":"senha123"}"""))
                .andExpect(status().isCreated());
    }

    @Test void tokenDeVinculadoNaoAcessaRotasDeConta() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        novoVinculado(a, "teo@x.com", 20);
        String token = obterToken("teo@x.com");

        mvc.perform(delete("/conta").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Operação permitida somente para o usuário assinante"));
    }

    @Test void tokenInvalidoRetorna401ComMensagem() throws Exception {
        mvc.perform(get("/filmes").header("Authorization", "Bearer token-que-nao-existe"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Token inválido ou expirado"));
    }

    @Test void tokenDeUsuarioRemovidoDeixaDeFuncionar() throws Exception {
        Usuario a = novoAssinante("ana@x.com", Plano.PREMIUM);
        Usuario v = novoVinculado(a, "teo@x.com", 20);
        String token = obterToken("teo@x.com");

        usuarioService.removerVinculado(a.getId(), v.getId());

        mvc.perform(get("/filmes").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
