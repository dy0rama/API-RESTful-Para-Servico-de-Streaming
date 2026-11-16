package com.ebac.streaming.controller;

import com.ebac.streaming.configurations.UsuarioAutenticado;
import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.requests.CriarVinculadoRequest;
import com.ebac.streaming.requests.MigrarPlanoRequest;
import com.ebac.streaming.responses.*;
import com.ebac.streaming.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.ebac.streaming.configurations.Exemplos.*;

@RestController
@RequestMapping(value = "/conta", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Conta", description = "Gerenciamento da conta. Somente o usuário assinante.")
@SecurityRequirement(name = "oauth2")
public class ContaController {

    private final UsuarioService service;

    @Operation(summary = "Cria um usuário vinculado à conta",
            description = "Limites por plano: BASIC não compartilha a conta, STANDARD aceita até 2 usuários "
                    + "e PREMIUM até 4. O perfil é calculado pela idade. A permissão de filmes 18+ só vale "
                    + "para o perfil ADOLESCENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário vinculado criado",
                    headers = @Header(name = "Location",
                            description = "URL para consultar o usuário criado (GET /conta/usuarios/{id})",
                            schema = @Schema(type = "string", format = "uri",
                                    example = "http://localhost:8080/conta/usuarios/3f2b8c1e-5a7d-4e0a-9b6c-1d2e3f4a5b6c")),
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class),
                            examples = @ExampleObject(name = "Vinculado criado", value = USUARIO))),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class),
                            examples = @ExampleObject(name = "Campos inválidos", value = VALIDACAO))),
            @ApiResponse(responseCode = "422", description = "Limite do plano atingido ou e-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Limite do plano", value = LIMITE_PLANO),
                            @ExampleObject(name = "Plano que não compartilha", value = PLANO_NAO_COMPARTILHA),
                            @ExampleObject(name = "E-mail duplicado", value = EMAIL_DUPLICADO)}))
    })
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> criarVinculado(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado,
            @Valid @RequestBody CriarVinculadoRequest req) {
        Usuario criado = service.criarVinculado(logado.getId(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(location).body(UsuarioResponse.de(criado));
    }

    @Operation(summary = "Consulta um usuário vinculado da própria conta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuário vinculado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class),
                            examples = @ExampleObject(name = "Vinculado", value = USUARIO))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(name = "Inexistente", value = USUARIO_NAO_ENCONTRADO))),
            @ApiResponse(responseCode = "422", description = "O usuário não pertence a esta conta",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(name = "Outra conta", value = NAO_PERTENCE)))
    })
    @GetMapping("/usuarios/{vinculadoId}")
    public UsuarioResponse buscarVinculado(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado,
            @Parameter(description = "Id do usuário vinculado") @PathVariable UUID vinculadoId) {
        return UsuarioResponse.de(service.buscarVinculado(logado.getId(), vinculadoId));
    }

    @Operation(summary = "Remove um usuário vinculado da própria conta",
            description = "O histórico do usuário removido é excluído junto.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido. Sem corpo na resposta."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(name = "Inexistente", value = USUARIO_NAO_ENCONTRADO))),
            @ApiResponse(responseCode = "422", description = "O usuário não pertence a esta conta",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(name = "Outra conta", value = NAO_PERTENCE)))
    })
    @DeleteMapping("/usuarios/{vinculadoId}")
    public ResponseEntity<Void> removerVinculado(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado,
            @Parameter(description = "Id do usuário vinculado") @PathVariable UUID vinculadoId) {
        service.removerVinculado(logado.getId(), vinculadoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Exclui a própria conta",
            description = "Exclui o assinante, todos os usuários vinculados e o histórico de todos eles.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta excluída. Sem corpo na resposta.")
    })
    @DeleteMapping
    public ResponseEntity<Void> excluirConta(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado) {
        service.excluirConta(logado.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Migra o plano da conta",
            description = """
            Upgrade (BASIC para STANDARD/PREMIUM, STANDARD para PREMIUM): os usuários vinculados são mantidos.

            Downgrade (PREMIUM para STANDARD/BASIC, STANDARD para BASIC): todos os vinculados são excluídos. \
            No downgrade de PREMIUM para STANDARD, o assinante pode cadastrar até 2 novos usuários.

            Os vinculados herdam os benefícios e as restrições do novo plano.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plano migrado",
                    content = @Content(schema = @Schema(implementation = MigracaoPlanoResponse.class), examples = {
                            @ExampleObject(name = "Upgrade", value = MIGRACAO_UPGRADE),
                            @ExampleObject(name = "Downgrade", value = MIGRACAO_DOWNGRADE)})),
            @ApiResponse(responseCode = "400", description = "Payload inválido",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoResponse.class),
                            examples = @ExampleObject(name = "Plano ausente", value = """
                    {"erros":{"novoPlano":"must not be null"}}"""))),
            @ApiResponse(responseCode = "422", description = "A conta já está no plano informado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class),
                            examples = @ExampleObject(name = "Mesmo plano", value = MESMO_PLANO)))
    })
    @PatchMapping("/plano")
    public MigracaoPlanoResponse migrarPlano(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado,
            @Valid @RequestBody MigrarPlanoRequest req) {
        return service.migrarPlano(logado.getId(), req.novoPlano());
    }

    @Operation(summary = "Mostra os dados da própria conta",
            description = "Devolve o assinante, o plano atual, o limite de usuários vinculados do plano, "
                    + "as vagas ainda disponíveis e a lista dos usuários vinculados, ordenada por nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados da conta",
                    content = @Content(schema = @Schema(implementation = ContaResponse.class), examples = {
                            @ExampleObject(name = "Conta STANDARD com um vinculado", value = CONTA),
                            @ExampleObject(name = "Conta BASIC, sem vinculados", value = CONTA_BASIC)}))
    })
    @GetMapping
    public ContaResponse detalhar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado) {
        return service.detalharConta(logado.getId());
    }
}