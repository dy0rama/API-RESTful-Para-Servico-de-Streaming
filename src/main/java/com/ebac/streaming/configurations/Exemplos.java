package com.ebac.streaming.configurations;

public final class Exemplos {
    private Exemplos() {}

    public static final String USUARIO = """
        {"id":"3f2b8c1e-5a7d-4e0a-9b6c-1d2e3f4a5b6c","nome":"Teo Souza","email":"teo@email.com",\
        "idade":15,"perfil":"ADOLESCENTE","plano":"STANDARD",\
        "assinanteId":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b","permissaoFilmesAdultos":false}""";

    public static final String ASSINANTE = """
        {"id":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b","nome":"Ana Souza","email":"ana@email.com",\
        "idade":30,"perfil":"ADULTO","plano":"STANDARD","assinanteId":null,"permissaoFilmesAdultos":false}""";

    public static final String VALIDACAO = """
        {"erros":{"idade":"Assinante deve ter 18 anos ou mais","senha":"A senha deve ter entre 6 e 72 caracteres"}}""";

    public static final String EMAIL_DUPLICADO = """
        {"erro":"E-mail já cadastrado"}""";

    public static final String LIMITE_PLANO = """
        {"erro":"Limite de 2 usuários vinculados do plano STANDARD atingido"}""";

    public static final String PLANO_NAO_COMPARTILHA = """
        {"erro":"O plano BASIC não permite compartilhar a conta"}""";

    public static final String NAO_PERTENCE = """
        {"erro":"O usuário não pertence a esta conta"}""";

    public static final String USUARIO_NAO_ENCONTRADO = """
        {"erro":"Usuário não encontrado"}""";

    public static final String FILME_NAO_ENCONTRADO = """
        {"erro":"Filme não encontrado"}""";

    public static final String MIGRACAO_UPGRADE = """
        {"assinanteId":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b","planoAnterior":"STANDARD",\
        "planoNovo":"PREMIUM","vinculadosRemovidos":0}""";

    public static final String MIGRACAO_DOWNGRADE = """
        {"assinanteId":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b","planoAnterior":"PREMIUM",\
        "planoNovo":"STANDARD","vinculadosRemovidos":4}""";

    public static final String MESMO_PLANO = """
        {"erro":"A conta já está no plano PREMIUM"}""";

    public static final String FILMES_STANDARD = """
        [{"id":"c1a2b3d4-0001-4000-8000-000000000001","titulo":"Aventura na Floresta","planoMinimo":"BASIC"},\
        {"id":"c1a2b3d4-0002-4000-8000-000000000002","titulo":"Noite de Comédia","planoMinimo":"STANDARD"}]""";

    public static final String FILMES_PREMIUM = """
        [{"id":"c1a2b3d4-0001-4000-8000-000000000001","titulo":"Aventura na Floresta"},\
        {"id":"c1a2b3d4-0005-4000-8000-000000000005","titulo":"Galáxia Perdida"}]""";

    public static final String ASSISTIU = """
        {"titulo":"Aventura na Floresta","segundosAssistidos":4,"mensagem":"Filme assistido: Aventura na Floresta"}""";

    public static final String NEGADO_PLANO = """
        {"erro":"O plano STANDARD da conta não dá acesso a este filme, que exige o plano PREMIUM."}""";

    public static final String NEGADO_IDADE = """
        {"erro":"Este filme tem classificação de 18 anos e não é permitido para o perfil INFANTIL."}""";

    public static final String NEGADO_ADOLESCENTE = """
        {"erro":"Este filme é de classificação 18 anos e o assinante da conta não liberou filmes adultos para o seu perfil."}""";

    public static final String HISTORICO = """
        [{"sequencia":1,"titulo":"Aventura na Floresta","assistidoEm":"2026-11-15T20:41:07"},\
        {"sequencia":2,"titulo":"Piratas do Mar","assistidoEm":"2026-11-15T20:41:15"},\
        {"sequencia":3,"titulo":"Aventura na Floresta","assistidoEm":"2026-11-15T20:41:20"}]""";

    public static final String SESSAO = """
        {"id":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b","email":"ana@email.com","papel":"ASSINANTE","plano":"STANDARD"}""";

    public static final String CONTA = """
    {"assinante":{"id":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b","nome":"Ana Souza","email":"ana@email.com",\
    "idade":30,"perfil":"ADULTO","plano":"STANDARD","assinanteId":null,"permissaoFilmesAdultos":false},\
    "plano":"STANDARD","limiteVinculados":2,"vagasDisponiveis":1,\
    "vinculados":[{"id":"3f2b8c1e-5a7d-4e0a-9b6c-1d2e3f4a5b6c","nome":"Teo Souza","email":"teo@email.com",\
    "idade":15,"perfil":"ADOLESCENTE","plano":"STANDARD","assinanteId":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b",\
    "permissaoFilmesAdultos":false}]}""";

    public static final String CONTA_BASIC = """
    {"assinante":{"id":"9a1c7d20-4b3e-4f55-8c1a-2b6d7e8f9a0b","nome":"Ana Souza","email":"ana@email.com",\
    "idade":30,"perfil":"ADULTO","plano":"BASIC","assinanteId":null,"permissaoFilmesAdultos":false},\
    "plano":"BASIC","limiteVinculados":0,"vagasDisponiveis":0,"vinculados":[]}""";
}
