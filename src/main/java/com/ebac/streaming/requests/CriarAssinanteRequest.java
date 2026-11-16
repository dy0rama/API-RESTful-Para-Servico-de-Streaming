package com.ebac.streaming.requests;

import com.ebac.streaming.enums.Plano;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CriarAssinanteRequest(
        @Schema(description = "Nome do assinante", example = "Ana Souza")
        @NotBlank String nome,

        @Schema(description = "E-mail, único no sistema e usado como login", example = "ana@email.com")
        @NotBlank @Email String email,

        @Schema(description = "Idade. O assinante precisa ter 18 anos ou mais", example = "30")
        @NotNull @Min(value = 18, message = "Assinante deve ter 18 anos ou mais") @Max(120) Integer idade,

        @Schema(description = "Plano da conta", example = "STANDARD")
        @NotNull Plano plano,

        @Schema(description = "Senha de 6 a 72 caracteres", example = "senha123")
        @NotBlank @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres") String senha
) {}
