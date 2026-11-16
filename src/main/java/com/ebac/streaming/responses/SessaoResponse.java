package com.ebac.streaming.responses;

import com.ebac.streaming.enums.Plano;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Usuário autenticado")
public record SessaoResponse(
        UUID id,
        @Schema(example = "ana@email.com") String email,
        @Schema(description = "ASSINANTE gerencia a conta; USUARIO é um vinculado", example = "ASSINANTE")
        String papel,
        @Schema(description = "Plano da conta. Para um vinculado, é o plano do assinante", example = "STANDARD")
        Plano plano
) {}
