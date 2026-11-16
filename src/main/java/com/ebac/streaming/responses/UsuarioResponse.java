package com.ebac.streaming.responses;

import com.ebac.streaming.entities.Usuario;
import com.ebac.streaming.enums.Perfil;
import com.ebac.streaming.enums.Plano;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados do usuário. A senha nunca é devolvida")
public record UsuarioResponse(
        UUID id, String nome, String email, Integer idade,
        @Schema(description = "Perfil calculado pela idade") Perfil perfil,
        @Schema(description = "Plano da conta. O vinculado herda o plano do assinante") Plano plano,
        @Schema(description = "Id do assinante da conta. Nulo quando o próprio usuário é o assinante")
        UUID assinanteId,
        boolean permissaoFilmesAdultos
) {
    public static UsuarioResponse de(Usuario u) {
        Plano plano = u.getAssinante() == null ? u.getPlano() : u.getAssinante().getPlano();
        UUID assinanteId = u.getAssinante() == null ? null : u.getAssinante().getId();
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getIdade(),
                u.getPerfil(), plano, assinanteId, u.isPermissaoFilmesAdultos());
    }
}
