package com.ebac.streaming.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de assistir um filme")
public record AssistirFilmeResponse(
        @Schema(example = "Aventura na Floresta") String titulo,
        @Schema(description = "Segundos de duração contabilizados (0 a 10)", example = "4") int segundosAssistidos,
        @Schema(example = "Filme assistido: Aventura na Floresta") String mensagem
) {}
