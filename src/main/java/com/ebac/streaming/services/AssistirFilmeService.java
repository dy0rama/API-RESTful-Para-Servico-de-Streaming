package com.ebac.streaming.services;

import com.ebac.streaming.entities.Filme;
import com.ebac.streaming.entities.HistoricoFilme;
import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.exceptions.AcessoNegadoException;
import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.repository.FilmeRepository;
import com.ebac.streaming.repository.HistoricoFilmeRepository;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.responses.AssistirFilmeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssistirFilmeService {

    private final UsuarioRepository usuarioRepo;
    private final FilmeRepository filmeRepo;
    private final HistoricoFilmeRepository historicoRepo;
    private final SimuladorReproducao simulador;

    // Sem @Transactional de propósito: a simulação pode levar até 10 segundos
    // e não deve manter uma transação (e uma conexão do banco) aberta esse tempo todo.
    public AssistirFilmeResponse assistir(UUID usuarioId, UUID filmeId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        Filme filme = filmeRepo.findById(filmeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filme não encontrado"));

        AcessoFilmePolicy.motivoNegado(usuario.getPlanoDaConta(), usuario.getPerfil(),
                        usuario.isPermissaoFilmesAdultos(), filme)
                .ifPresent(motivo -> { throw new AcessoNegadoException(motivo); });

        int segundos = filme.getDuracaoSegundos();
        simulador.reproduzir(segundos);

        HistoricoFilme registro = new HistoricoFilme();
        registro.setUsuario(usuario);
        registro.setFilme(filme);
        registro.setAssistidoEm(LocalDateTime.now());
        registro.setSequencia((int) historicoRepo.countByUsuarioId(usuarioId) + 1);
        historicoRepo.save(registro);

        return new AssistirFilmeResponse(filme.getTitulo(), segundos,
                "Filme assistido: " + filme.getTitulo());
    }
}
