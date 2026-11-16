package com.ebac.streaming.configurations;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UsuarioAutenticado implements UserDetails {
    @Getter
    private final UUID id;
    private final String email;
    private final String senha;
    private final boolean assinante;

    public UsuarioAutenticado(UUID id, String email, String senha, boolean assinante) {
        this.id = id; this.email = email; this.senha = senha; this.assinante = assinante;
    }

    @NullMarked
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(assinante ? "ROLE_ASSINANTE" : "ROLE_USUARIO"));
    }
    @Override public String getPassword() { return senha; }
    @NullMarked
    @Override public String getUsername() { return email; }

}
