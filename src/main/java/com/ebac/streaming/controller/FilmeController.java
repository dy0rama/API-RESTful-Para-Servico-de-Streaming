package com.ebac.streaming.controller;

import com.ebac.streaming.configurations.UsuarioAutenticado;
import com.ebac.streaming.responses.AssistirFilmeResponse;
import com.ebac.streaming.responses.ErroResponse;
import com.ebac.streaming.responses.FilmeListagemResponse;
import com.ebac.streaming.services.AssistirFilmeService;
import com.ebac.streaming.services.FilmeService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.ebac.streaming.configurations.Exemplos.*;

@RestController
@RequestMapping(value = "/filmes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Filmes", description = "Catálogo e reprodução. Disponível para qualquer usuário autenticado.")
@SecurityRequirement(name = "oauth2")
public class FilmeController {

    private final FilmeService service;
    private final AssistirFilmeService assistirService;

    @Operation(summary = "Lista os filmes que o usuário pode assistir",
            description = "O filtro combina o plano da conta e o perfil por idade. Contas PREMIUM recebem "
                    + "somente o título; as demais recebem também o plano mínimo do filme.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filmes disponíveis (lista vazia se nenhum for permitido)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FilmeListagemResponse.class)),
                            examples = {
                                    @ExampleObject(name = "Conta STANDARD", value = FILMES_STANDARD),
                                    @ExampleObject(name = "Conta PREMIUM (sem planoMinimo)", value = FILMES_PREMIUM)}))
    })
    @GetMapping
    public List<FilmeListagemResponse> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.listarParaUsuario(logado.getId());
    }

    @Operation(summary = "Assiste a um filme",
            description = "Valida o plano da conta e o perfil por idade. Se permitido, simula a reprodução "
                    + "pelos segundos de duração do filme (0 a 10, então a resposta pode demorar), responde que "
                    + "foi assistido e registra no histórico. Se negado, devolve uma mensagem explicando o motivo "
                    + "e nada é registrado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme assistido e registrado no histórico",
                    content = @Content(schema = @Schema(implementation = AssistirFilmeResponse.class),
                            examples = @ExampleObject(name = "Assistido", value = ASSISTIU))),
            @ApiResponse(responseCode = "403", description = "Plano ou faixa etária não permitem o acesso",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Plano insuficiente", value = NEGADO_PLANO),
                            @ExampleObject(name = "Faixa etária", value = NEGADO_IDADE),
                            @ExampleObject(name = "Adolescente sem liberação", value = NEGADO_ADOLESCENTE)})),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(name = "Inexistente", value = FILME_NAO_ENCONTRADO)))
    })
    @PostMapping("/{filmeId}/assistir")
    public AssistirFilmeResponse assistir(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado,
            @Parameter(description = "Id do filme, obtido em GET /filmes") @PathVariable UUID filmeId) {
        return assistirService.assistir(logado.getId(), filmeId);
    }
}
