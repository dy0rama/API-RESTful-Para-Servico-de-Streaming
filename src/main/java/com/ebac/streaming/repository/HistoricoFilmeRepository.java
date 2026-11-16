package com.ebac.streaming.repository;

import com.ebac.streaming.entities.HistoricoFilme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoFilmeRepository extends JpaRepository<HistoricoFilme, UUID> {
    long countByUsuarioId(UUID usuarioId);
    List<HistoricoFilme> findByUsuarioIdOrderBySequenciaAsc(UUID usuarioId);
}
