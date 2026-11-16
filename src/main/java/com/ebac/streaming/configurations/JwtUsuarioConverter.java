package com.ebac.streaming.configurations;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUsuarioConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UsuarioRepository repo;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Usuario u;
        try {
            u = repo.findById(UUID.fromString(jwt.getSubject()))
                    .orElseThrow(() -> new InvalidBearerTokenException("Usuário não existe mais"));
        } catch (IllegalArgumentException e) {
            throw new InvalidBearerTokenException("Token inválido");
        }
        UsuarioAutenticado principal =
                new UsuarioAutenticado(u.getId(), u.getEmail(), u.getSenha(), u.getAssinante() == null);
        return UsernamePasswordAuthenticationToken.authenticated(principal, jwt, principal.getAuthorities());
    }
}
