package com.ebac.streaming.responses;

import com.ebac.streaming.enums.Plano;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resultado da migração de plano")
public record MigracaoPlanoResponse(
        UUID assinanteId,
        @Schema(example = "PREMIUM") Plano planoAnterior,
        @Schema(example = "STANDARD") Plano planoNovo,
        @Schema(description = "Quantidade de usuários vinculados excluídos. Só ocorre em downgrade", example = "2")
        int vinculadosRemovidos
) {}