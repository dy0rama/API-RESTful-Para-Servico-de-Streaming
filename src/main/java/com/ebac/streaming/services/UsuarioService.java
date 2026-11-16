package com.ebac.streaming.services;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import com.ebac.streaming.exceptions.RecursoNaoEncontradoException;
import com.ebac.streaming.exceptions.RegraNegocioException;
import com.ebac.streaming.repository.UsuarioRepository;
import com.ebac.streaming.requests.CriarAssinanteRequest;
import com.ebac.streaming.requests.CriarVinculadoRequest;
import com.ebac.streaming.responses.ContaResponse;
import com.ebac.streaming.responses.MigracaoPlanoResponse;
import com.ebac.streaming.responses.SessaoResponse;
import com.ebac.streaming.responses.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public Usuario criarAssinante(CriarAssinanteRequest req) {
        validarEmailLivre(req.email());

        Usuario u = new Usuario();
        u.setNome(req.nome());
        u.setEmail(req.email());
        u.setIdade(req.idade());
        u.setPerfil(Perfil.deIdade(req.idade()));
        u.setPlano(req.plano());
        u.setSenha(encoder.encode(req.senha()));
        return repo.save(u);
    }

    @Transactional
    public Usuario criarVinculado(UUID assinanteId, CriarVinculadoRequest req) {
        Usuario assinante = buscarAssinante(assinanteId);

        if (assinante.getAssinante() != null) {
            throw new RegraNegocioException("Somente um usuário assinante pode adicionar usuários");
        }

        Plano plano = assinante.getPlano();
        if (plano.limiteVinculados() == 0) {
            throw new RegraNegocioException("O plano " + plano + " não permite compartilhar a conta");
        }
        if (repo.countByAssinanteId(assinanteId) >= plano.limiteVinculados()) {
            throw new RegraNegocioException("Limite de " + plano.limiteVinculados()
                    + " usuários vinculados do plano " + plano + " atingido");
        }

        validarEmailLivre(req.email());

        Perfil perfil = Perfil.deIdade(req.idade());

        Usuario u = new Usuario();
        u.setNome(req.nome());
        u.setEmail(req.email());
        u.setIdade(req.idade());
        u.setPerfil(perfil);
        u.setAssinante(assinante);
        u.setPermissaoFilmesAdultos(perfil == Perfil.ADOLESCENTE && req.permissaoFilmesAdultos());
        u.setSenha(encoder.encode(req.senha()));
        return repo.save(u);
    }

    private void validarEmailLivre(String email) {
        if (repo.existsByEmail(email)) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }
    }

    private Usuario buscarAssinante(UUID id) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        if (u.getAssinante() != null) {
            throw new RegraNegocioException("Somente um usuário assinante pode realizar esta operação");
        }
        return u;
    }

// em criarVinculado, troque as primeiras linhas por:
// Usuario assinante = buscarAssinante(assinanteId);

    @Transactional
    public void excluirConta(UUID assinanteId) {
        Usuario assinante = buscarAssinante(assinanteId);
        repo.delete(assinante);   // cascata remove vinculados e históricos
    }

    @Transactional
    public MigracaoPlanoResponse migrarPlano(UUID assinanteId, Plano novoPlano) {
        Usuario assinante = buscarAssinante(assinanteId);
        Plano anterior = assinante.getPlano();

        if (anterior == novoPlano) {
            throw new RegraNegocioException("A conta já está no plano " + novoPlano);
        }

        int removidos = 0;
        if (anterior.ehDowngradePara(novoPlano)) {
            List<Usuario> vinculados = repo.findByAssinanteId(assinanteId);
            repo.deleteAll(vinculados);      // cada exclusão leva junto o histórico do usuário
            removidos = vinculados.size();
        }

        assinante.setPlano(novoPlano);
        repo.save(assinante);
        return new MigracaoPlanoResponse(assinanteId, anterior, novoPlano, removidos);
    }

    @Transactional(readOnly = true)
    public SessaoResponse sessaoDe(UUID usuarioId) {
        Usuario u = repo.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        String papel = u.getAssinante() == null ? "ASSINANTE" : "USUARIO";
        return new SessaoResponse(u.getId(), u.getEmail(), papel, u.getPlanoDaConta());
    }

    @Transactional(readOnly = true)
    public Usuario buscarVinculado(UUID assinanteId, UUID vinculadoId) {
        buscarAssinante(assinanteId);

        Usuario vinculado = repo.findById(vinculadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (vinculado.getAssinante() == null
                || !vinculado.getAssinante().getId().equals(assinanteId)) {
            throw new RegraNegocioException("O usuário não pertence a esta conta");
        }
        return vinculado;
    }

    @Transactional
    public void removerVinculado(UUID assinanteId, UUID vinculadoId) {
        repo.delete(buscarVinculado(assinanteId, vinculadoId));
    }

    @Transactional(readOnly = true)
    public ContaResponse detalharConta(UUID assinanteId) {
        Usuario assinante = buscarAssinante(assinanteId);

        List<UsuarioResponse> vinculados = repo.findByAssinanteIdOrderByNomeAsc(assinanteId).stream()
                .map(UsuarioResponse::de)
                .toList();

        int limite = assinante.getPlano().limiteVinculados();
        int vagas = Math.max(0, limite - vinculados.size());

        return new ContaResponse(UsuarioResponse.de(assinante), assinante.getPlano(), limite, vagas, vinculados);
    }
}
