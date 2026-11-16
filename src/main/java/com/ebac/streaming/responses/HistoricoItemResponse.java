package com.ebac.streaming.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Item do histórico de filmes assistidos")
public record HistoricoItemResponse(
        @Schema(description = "Posição na ordem em que foi assistido, começando em 1", example = "1") int sequencia,
        String titulo,
        LocalDateTime assistidoEm
) {}
