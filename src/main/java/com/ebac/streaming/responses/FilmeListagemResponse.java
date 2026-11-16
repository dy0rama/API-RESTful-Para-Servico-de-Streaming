package com.ebac.streaming.responses;

import com.ebac.streaming.enums.Plano;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Filme disponível para o usuário")
public record FilmeListagemResponse(
        @Schema(description = "Usado em POST /filmes/{filmeId}/assistir") UUID id,
        String titulo,
        @Schema(description = "Plano mínimo do filme. Não é exibido para contas PREMIUM") Plano planoMinimo
) {}
