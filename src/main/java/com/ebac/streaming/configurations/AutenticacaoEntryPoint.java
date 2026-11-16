package com.ebac.streaming.configurations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@Component
public class AutenticacaoEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        String mensagem;
        if (ex instanceof BadCredentialsException) {
            // HTTP Basic com e-mail inexistente ou senha errada
            mensagem = "E-mail ou senha inválidos";
        } else if (ex instanceof OAuth2AuthenticationException) {
            // Bearer inválido, expirado, malformado ou de usuário que não existe mais
            mensagem = "Token inválido ou expirado";
        } else {
            mensagem = "Autenticação necessária: informe e-mail e senha";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
