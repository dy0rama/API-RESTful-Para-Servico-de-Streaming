package com.ebac.streaming.repository;

import com.ebac.streaming.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmail(String email);
    long countByAssinanteId(UUID assinanteId);
    List<Usuario> findByAssinanteId(UUID assinanteId);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByAssinanteIdOrderByNomeAsc(UUID assinanteId);
}
