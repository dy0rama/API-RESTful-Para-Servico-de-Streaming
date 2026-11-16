package com.ebac.streaming.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Erro de regra de negócio, de acesso ou recurso não encontrado")
public record ErroResponse(
        @Schema(example = "Limite de 2 usuários vinculados do plano STANDARD atingido") String erro
) {}
