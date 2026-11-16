package com.ebac.streaming.controller;

import com.ebac.streaming.requests.CriarAssinanteRequest;
import com.ebac.streaming.requests.CriarVinculadoRequest;
import com.ebac.streaming.requests.MigrarPlanoRequest;
import com.ebac.streaming.responses.ErroResponse;
import com.ebac.streaming.responses.ErroValidacaoResponse;
import com.ebac.streaming.responses.MigracaoPlanoResponse;
import com.ebac.streaming.responses.UsuarioResponse;
import com.ebac.streaming.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.ebac.streaming.configurations.Exemplos.*;

@RestController
@RequestMapping(value = "/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Cadastro", description = "Cadastro público de assinantes. Não exige login.")
public class UsuarioController {

    private final UsuarioService service;

    @Operation(summary = "Cadastra um novo assinante",
            description = "Cria uma conta com o plano escolhido. O assinante precisa ter 18 anos ou mais "
                    + "e recebe o perfil ADULTO. O e-mail é único e a senha é guardada codificada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assinante criado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class),
                            examples = @ExampleObject(name = "Assinante criado", value = ASSINANTE))),
            @ApiResponse(responseCode = "400", description = "Payload inválido (ex.: idade negativa ou menor que 18)",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class),
                            examples = @ExampleObject(name = "Campos inválidos", value = VALIDACAO))),
            @ApiResponse(responseCode = "422", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(name = "E-mail duplicado", value = EMAIL_DUPLICADO)))
    })
    @PostMapping("/assinantes")
    public ResponseEntity<UsuarioResponse> criarAssinante(@Valid @RequestBody CriarAssinanteRequest req) {
        return ResponseEntity.status(201).body(UsuarioResponse.de(service.criarAssinante(req)));
    }
}