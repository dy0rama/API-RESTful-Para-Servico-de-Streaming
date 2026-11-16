package com.ebac.streaming.controller;

import com.ebac.streaming.configurations.UsuarioAutenticado;
import com.ebac.streaming.responses.HistoricoItemResponse;
import com.ebac.streaming.services.HistoricoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.ebac.streaming.configurations.Exemplos.HISTORICO;

@RestController
@RequestMapping(value = "/historico", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Histórico", description = "Histórico de filmes assistidos pelo usuário autenticado.")
@SecurityRequirement(name = "oauth2")
public class HistoricoController {

    private final HistoricoService service;

    @Operation(summary = "Lista o histórico do usuário autenticado",
            description = "Na ordem em que os filmes foram assistidos, com repetições: um filme assistido "
                    + "5 vezes aparece 5 vezes. Cada usuário tem o próprio histórico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico (lista vazia se nada foi assistido)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoricoItemResponse.class)),
                            examples = {
                                    @ExampleObject(name = "Com repetição", value = HISTORICO),
                                    @ExampleObject(name = "Vazio", value = "[]")}))
    })
    @GetMapping
    public List<HistoricoItemResponse> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.listar(logado.getId());
    }
}
