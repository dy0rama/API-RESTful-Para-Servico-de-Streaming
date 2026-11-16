package com.ebac.streaming.services;

import com.ebac.streaming.configurations.UsuarioAutenticado;
import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repo;

    @NullMarked
    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
        return new UsuarioAutenticado(u.getId(), u.getEmail(), u.getSenha(), u.getAssinante() == null);
    }
}
