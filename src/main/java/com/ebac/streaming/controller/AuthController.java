package com.ebac.streaming.controller;

import com.ebac.streaming.configurations.UsuarioAutenticado;
import com.ebac.streaming.responses.ErroResponse;
import com.ebac.streaming.responses.SessaoResponse;
import com.ebac.streaming.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ebac.streaming.configurations.Exemplos.SESSAO;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Verificação das credenciais informadas no Authorize.")
@SecurityRequirement(name = "oauth2")
public class AuthController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Verifica o login",
            description = "Use logo depois de clicar em Authorize: 200 confirma quem está autenticado, "
                    + "o papel dele e o plano da conta; 401 indica que o e-mail ou a senha estão incorretos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais válidas",
                    content = @Content(schema = @Schema(implementation = SessaoResponse.class),
                            examples = @ExampleObject(name = "Assinante", value = SESSAO)))
    })
    @GetMapping("/me")
    public SessaoResponse me(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado logado) {
        return usuarioService.sessaoDe(logado.getId());
    }
}
