package com.ebac.streaming.controller;

import com.ebac.streaming.configurations.UsuarioAutenticado;
import com.ebac.streaming.responses.TokenResponse;
import com.ebac.streaming.services.TokenService;
import com.ebac.streaming.services.UsuarioDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenController {
    private final UsuarioDetailsService detailsService;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    @Operation(hidden = true)
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> token(@RequestParam("grant_type") String grantType,
                                   @RequestParam String username,
                                   @RequestParam String password) {
        if (!"password".equals(grantType)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "unsupported_grant_type",
                    "error_description", "Somente o grant_type password é suportado"));
        }

        UserDetails usuario;
        try {
            usuario = detailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return credenciaisInvalidas();
        }
        if (!encoder.matches(password, usuario.getPassword())) {
            return credenciaisInvalidas();
        }

        String jwt = tokenService.gerar((UsuarioAutenticado) usuario);
        return ResponseEntity.ok(new TokenResponse(jwt, "Bearer", tokenService.expiraEmSegundos()));
    }

    private ResponseEntity<Map<String, String>> credenciaisInvalidas() {
        return ResponseEntity.status(401).body(Map.of(
                "error", "invalid_grant",
                "error_description", "E-mail ou senha inválidos"));
    }
}
