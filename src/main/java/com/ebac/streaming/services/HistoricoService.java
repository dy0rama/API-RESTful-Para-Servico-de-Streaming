package com.ebac.streaming.services;

import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.repository.HistoricoFilmeRepository;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.responses.HistoricoItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final UsuarioRepository usuarioRepo;
    private final HistoricoFilmeRepository historicoRepo;

    @Transactional(readOnly = true)
    public List<HistoricoItemResponse> listar(UUID usuarioId) {
        if (!usuarioRepo.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }
        return historicoRepo.findByUsuarioIdOrderBySequenciaAsc(usuarioId).stream()
                .map(h -> new HistoricoItemResponse(
                        h.getSequencia(), h.getFilme().getTitulo(), h.getAssistidoEm()))
                .toList();
    }
}
