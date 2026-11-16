package com.ebac.streaming.services;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.repository.FilmeRepository;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.responses.FilmeListagemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilmeService {

    private final UsuarioRepository usuarioRepo;
    private final FilmeRepository filmeRepo;

    @Transactional(readOnly = true)
    public List<FilmeListagemResponse> listarParaUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        Plano planoDaConta = usuario.getPlanoDaConta();
        boolean exibirPlanoMinimo = planoDaConta != Plano.PREMIUM;

        return filmeRepo.findAll().stream()
                .filter(f -> AcessoFilmePolicy.podeAssistir(
                        planoDaConta, usuario.getPerfil(), usuario.isPermissaoFilmesAdultos(), f))
                .map(f -> new FilmeListagemResponse(
                        f.getId(), f.getTitulo(), exibirPlanoMinimo ? f.getPlanoMinimo() : null)).toList();
    }
}
