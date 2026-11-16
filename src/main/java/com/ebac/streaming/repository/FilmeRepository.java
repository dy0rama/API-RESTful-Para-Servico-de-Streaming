package com.ebac.streaming.repository;

import com.ebac.streaming.entities.Filme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FilmeRepository extends JpaRepository<Filme, UUID> {}
