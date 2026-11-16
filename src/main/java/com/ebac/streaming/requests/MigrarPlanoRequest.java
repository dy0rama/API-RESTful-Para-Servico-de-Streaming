package com.ebac.streaming.requests;

import com.ebac.streaming.enums.Plano;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MigrarPlanoRequest(
        @Schema(description = "Plano de destino. Precisa ser diferente do plano atual", example = "PREMIUM")
        @NotNull Plano novoPlano
) {}