package com.ebac.streaming.configurations;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Plataforma de Streaming",
        version = "1.0",
        description = """
        Simulação de uma plataforma de streaming.

        **Como testar:** cadastre um assinante em `POST /usuarios/assinantes`, clique em \
        **Authorize** e informe o e-mail (no campo *username*) e a senha. Se estiverem errados, \
        o próprio modal mostra o erro. Depois use os demais endpoints.

        **Perfis:** o *assinante* gerencia a conta (`/conta/**`). Os usuários *vinculados* só listam \
        e assistem filmes e consultam o próprio histórico.
        """))
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "/auth/token")))
public class OpenApiConfig {}
