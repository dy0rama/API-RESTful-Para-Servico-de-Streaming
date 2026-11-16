package com.ebac.streaming.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Erro de validação do payload, com a mensagem de cada campo inválido")
public record ErroValidacaoResponse(
        @Schema(example = "{\"idade\": \"Assinante deve ter 18 anos ou mais\"}") Map<String, String> erros
) {}
