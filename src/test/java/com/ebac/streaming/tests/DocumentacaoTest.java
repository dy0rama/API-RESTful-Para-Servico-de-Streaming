package com.ebac.streaming.tests;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentacaoTest {
    @Autowired
    MockMvc mvc;

    @Test
    void documentacaoOpenApiEPublica() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Plataforma de Streaming"));
    }

    @Test
    void swaggerUiEPublico() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void apiContinuaProtegida() throws Exception {
        mvc.perform(get("/filmes")).andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroDeAssinanteNaoExigeAutenticacaoNaDocumentacao() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/usuarios/assinantes'].post.security").doesNotExist());
    }

    @Test
    void rotasDeContaExigemAutenticacaoNaDocumentacao() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/conta/plano'].patch.security").exists())
                .andExpect(jsonPath("$.paths['/conta'].delete.security").exists());
    }

    @Test
    void usuarioLogadoNaoApareceComoParametroDeEntrada() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/filmes'].get.parameters").doesNotExist())
                .andExpect(jsonPath("$.paths['/historico'].get.parameters").doesNotExist());
    }

    @Test
    void validacoesDosDtosApareceNoSchema() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.schemas.CriarAssinanteRequest.properties.idade.minimum").value(18))
                .andExpect(jsonPath("$.components.schemas.CriarAssinanteRequest.properties.senha.minLength").value(6));
    }

    @Test void oauth2PasswordFlowEstaDeclaradoNaDocumentacao() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.securitySchemes.oauth2.type").value("oauth2"))
                .andExpect(jsonPath("$.components.securitySchemes.oauth2.flows.password.tokenUrl").value("/auth/token"));
    }

    @Test void endpointDeTokenNaoApareceNaDocumentacao() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/auth/token']").doesNotExist());
    }

    @Test void headerLocationEstaDocumentadoNoCriarVinculado() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/conta/usuarios'].post.responses['201'].headers.Location").exists());
    }

    @Test void consultaDeVinculadoEstaDocumentada() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/conta/usuarios/{vinculadoId}'].get.security[0].oauth2").exists());
    }

    @Test @SuppressWarnings("unchecked")
    void todaRespostaDocumentadaTemCorpoJsonExcetoOsDe204() throws Exception {
        String json = mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> operacoes = JsonPath.read(json, "$.paths.*.*");

        assertFalse(operacoes.isEmpty());
        for (Map<String, Object> op : operacoes) {
            Map<String, Object> respostas = (Map<String, Object>) op.get("responses");
            respostas.forEach((codigo, resposta) -> {
                Map<String, Object> conteudo = (Map<String, Object>) ((Map<String, Object>) resposta).get("content");
                String onde = op.get("operationId") + " / " + codigo;
                if (codigo.equals("204")) {
                    assertNull(conteudo, "204 não deve ter corpo: " + onde);
                } else {
                    assertNotNull(conteudo, "Resposta sem corpo documentado: " + onde);
                    assertTrue(conteudo.containsKey("application/json"), "Falta application/json: " + onde);
                }
            });
        }
    }

    @Test void naoSobraTipoDeConteudoGenerico() throws Exception {
        String json = mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
        assertFalse(json.contains("\"*/*\""));
    }

    @Test void respostas401De401DosEndpointsProtegidosTemExemplos() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/filmes'].get.responses['401'].content['application/json'].examples").exists())
                .andExpect(jsonPath("$.paths['/historico'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/auth/me'].get.responses['401']").exists());
    }

    @Test void rotasDeContaDocumentamO403DeQuemNaoEAssinante() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/conta/plano'].patch.responses['403'].content['application/json']").exists())
                .andExpect(jsonPath("$.paths['/conta'].delete.responses['403'].content['application/json']").exists());
    }

    @Test void cadastroPublicoNaoDocumentaOs401De401() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/usuarios/assinantes'].post.responses['401']").doesNotExist());
    }

    @Test void detalheDaContaEstaDocumentado() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/conta'].get.security[0].oauth2").exists())
                .andExpect(jsonPath("$.paths['/conta'].get.responses['200'].content['application/json'].examples").exists())
                .andExpect(jsonPath("$.paths['/conta'].get.responses['403'].content['application/json']").exists())
                .andExpect(jsonPath("$.components.schemas.ContaResponse.properties.vinculados").exists());
    }
}
