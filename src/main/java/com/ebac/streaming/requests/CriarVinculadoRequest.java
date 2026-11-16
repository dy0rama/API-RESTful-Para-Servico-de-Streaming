package com.ebac.streaming.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CriarVinculadoRequest(
        @Schema(description = "Nome do usuário", example = "Teo Souza")
        @NotBlank String nome,

        @Schema(description = "E-mail, único no sistema e usado como login", example = "teo@email.com")
        @NotBlank @Email String email,

        @Schema(description = "Idade. Define o perfil: menos de 12 INFANTIL, 12 a 17 ADOLESCENTE, 18 ou mais ADULTO",
                example = "15")
        @NotNull @Min(value = 0, message = "Idade inválida") @Max(120) Integer idade,

        @Schema(description = "Libera filmes de 18 anos. Só tem efeito para o perfil ADOLESCENTE", example = "false")
        boolean permissaoFilmesAdultos,

        @Schema(description = "Senha de 6 a 72 caracteres", example = "senha123")
        @NotBlank @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres") String senha
) {}
