package com.ebac.streaming.responses;

import com.ebac.streaming.enums.Plano;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Dados da conta do assinante, com o plano e os usuários vinculados")
public record ContaResponse(
        @Schema(description = "Dados do assinante dono da conta") UsuarioResponse assinante,
        @Schema(description = "Plano atual da conta", example = "STANDARD") Plano plano,
        @Schema(description = "Quantidade máxima de usuários vinculados do plano "
                + "(BASIC 0, STANDARD 2, PREMIUM 4)", example = "2") int limiteVinculados,
        @Schema(description = "Quantos usuários ainda podem ser vinculados", example = "1") int vagasDisponiveis,
        @Schema(description = "Usuários vinculados, ordenados por nome") List<UsuarioResponse> vinculados
) {}
