package com.ebac.streaming.configurations;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RespostasPadraoConfig {

    @Bean
    OpenApiCustomizer respostasDeSeguranca() {
        return openApi -> openApi.getPaths().forEach((caminho, item) ->
                item.readOperations().forEach(op -> {
                    boolean protegido = op.getSecurity() != null && !op.getSecurity().isEmpty();
                    if (!protegido) return;

                    op.getResponses().addApiResponse("401", resposta(
                            "Não autenticado, ou token inválido ou expirado",
                            Map.of(
                                    "Sem credenciais", "Autenticação necessária: informe e-mail e senha",
                                    "Credenciais inválidas", "E-mail ou senha inválidos",
                                    "Token inválido ou expirado", "Token inválido ou expirado")));

                    if (caminho.startsWith("/conta")) {
                        op.getResponses().addApiResponse("403", resposta(
                                "Quem chamou não é assinante",
                                Map.of("Usuário vinculado", "Operação permitida somente para o usuário assinante")));
                    }
                }));
    }

    private ApiResponse resposta(String descricao, Map<String, String> exemplos) {
        MediaType tipo = new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErroResponse"));
        exemplos.forEach((nome, mensagem) ->
                tipo.addExamples(nome, new Example().value(Map.of("erro", mensagem))));
        return new ApiResponse().description(descricao)
                .content(new Content().addMediaType("application/json", tipo));
    }
}
