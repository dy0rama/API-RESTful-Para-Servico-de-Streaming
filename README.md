🎬 Plataforma de Streaming — API REST

API REST que simula o back-end de uma plataforma de streaming: cadastro de assinantes, contas compartilhadas por plano, controle de acesso a filmes por plano e faixa etária, e histórico de reprodução. Autenticação com JWT (OAuth2) e documentação interativa com Swagger.

Show Image Show Image Show Image Show Image Show Image

📌 Sobre o projeto

O projeto foi desenvolvido seguindo TDD, começando pelas regras de negócio (perfis e planos) e evoluindo até a camada de segurança e documentação. A ideia é reproduzir regras reais de uma plataforma de streaming:

    Um assinante (maior de 18 anos) cria a conta e escolhe um plano.
    O assinante pode vincular outros usuários à conta, conforme o limite do plano.
    Cada usuário tem um perfil definido pela idade, que restringe o que ele pode assistir.
    Assistir a um filme é simulado pela duração do filme (0 a 10 segundos) e fica registrado no histórico.
    
✨ Funcionalidades
Área	O que a API faz

    Cadastro	Cria assinantes (público, sem login), e-mail único, senha armazenada com BCrypt
    Conta	Cria, consulta e remove usuários vinculados; detalha a conta (plano, limite, vagas); exclui a conta
    Planos	Migração de plano com regras de upgrade e downgrade
    Catálogo	Lista somente os filmes que o usuário pode assistir
    Reprodução	Valida plano e faixa etária, simula a exibição e registra no histórico
    Histórico	Lista os filmes assistidos, na ordem, com repetições, separado por usuário
    Segurança	Login via OAuth2 (password flow) com token JWT e controle de acesso por papel
    
📐 Regras de negócio

    Planos
    Plano	Usuários vinculados	Acesso
    BASIC	0 (não compartilha a conta)	Filmes BASIC
    STANDARD	até 2	Filmes BASIC e STANDARD
    PREMIUM	até 4	Todos os filmes
    Perfis por idade
    Idade	Perfil	Pode assistir
    menos de 12	INFANTIL	Até classificação 12 anos
    12 a 17	ADOLESCENTE	Até 16 anos; 18 anos só com liberação do assinante
    18 ou mais	ADULTO	Tudo
    Migração de plano
    Upgrade (ex.: BASIC → STANDARD): os usuários vinculados são mantidos.
    Downgrade (ex.: PREMIUM → BASIC): todos os vinculados são excluídos, junto com o histórico deles.
    Migrar para o plano que a conta já possui é rejeitado.
    Exclusão em cascata

Excluir o assinante remove seus vinculados e o histórico de todos. Remover um vinculado remove o histórico dele.

🛠️ Tecnologias

    Java 21
    Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security)
    Spring Security + OAuth2 Resource Server (JWT)
    MySQL + Hibernate
    springdoc-openapi (Swagger UI)
    Lombok
    JUnit 5, Mockito, Spring Boot Test e Spring Security Test
    Maven (com Maven Wrapper)
    
🧱 Arquitetura

Organização em camadas, dentro de com.ebac.streaming:

    src/main/java/com/ebac/streaming
    ├── configurations/   # Segurança, JWT, OpenAPI, exemplos da documentação
    ├── controller/       # Endpoints REST
    ├── services/         # Regras de negócio (UsuarioService, AssistirFilmeService, AcessoFilmePolicy...)
    ├── repository/       # Spring Data JPA
    ├── entities/         # Usuario, Filme, HistoricoFilme
    ├── enums/            # Plano, Perfil, ClassificacaoEtaria
    ├── requests/         # DTOs de entrada com validação
    ├── responses/        # DTOs de saída
    └── exceptions/       # Exceções de domínio e handlers globais

Decisões de projeto:

    UUID como identificador de todas as entidades.
    Regras de acesso isoladas em AcessoFilmePolicy, uma classe pura e fácil de testar.
    A reprodução usa uma interface (SimuladorReproducao), o que permite trocar a espera real por um mock nos testes.
    AssistirFilmeService não é transacional de propósito: a simulação pode levar até 10 segundos e não deve manter uma conexão do banco aberta.
    Tratamento centralizado de erros, com respostas padronizadas para validação (400), acesso negado (403), recurso não encontrado (404) e regra de negócio (422).
    Duas cadeias de segurança: uma exclusiva para /auth/token e outra para o restante da API, em modo stateless.
    🔐 Autenticação e autorização
    O assinante se cadastra em POST /usuarios/assinantes (público).
    Faz login com e-mail e senha em POST /auth/token (grant_type=password) e recebe um JWT (expira em 60 minutos por padrão).
    Envia o token no cabeçalho Authorization: Bearer <token>.
    Rota	Quem acessa
    POST /usuarios/assinantes, Swagger	Público
    /conta/**	Somente o assinante
    /filmes, /historico, /auth/me	Qualquer usuário autenticado
    
🔌 Endpoints

    Método	Rota	Descrição
    POST	/usuarios/assinantes	Cadastra um assinante
    POST	/auth/token	Gera o token JWT
    GET	/auth/me	Dados da sessão atual
    GET	/conta	Detalha a conta (plano, limite, vagas, vinculados)
    DELETE	/conta	Exclui a conta
    PATCH	/conta/plano	Migra o plano
    POST	/conta/usuarios	Cria usuário vinculado
    GET	/conta/usuarios/{id}	Consulta um vinculado
    DELETE	/conta/usuarios/{id}	Remove um vinculado
    GET	/filmes	Lista os filmes permitidos ao usuário
    POST	/filmes/{id}/assistir	Assiste a um filme
    GET	/historico	Histórico do usuário autenticado

A documentação completa, com exemplos de requisição e resposta, fica no Swagger UI.

🚀 Como executar
Pré-requisitos
JDK 21
MySQL em execução, com um banco criado (ex.: streaming)

    1. Clone o repositório
    bash
    git clone https://github.com/<seu-usuario>/<seu-repositorio>.git
    cd <seu-repositorio>
    2. Configure as variáveis de ambiente

A aplicação lê as credenciais do banco do ambiente, sem senhas no código:

    Variável	Descrição	Exemplo
    DB_URL	URL JDBC do MySQL	jdbc:mysql://localhost:3306/streaming
    DB_USERNAME	Usuário do banco	root
    DB_PASSWORD	Senha do banco	sua_senha
    JWT_SECRET	Chave de assinatura do JWT (mínimo 32 bytes)	(opcional em desenvolvimento)

⚠️ Em produção, defina sempre um JWT_SECRET próprio. O valor padrão existe só para facilitar o desenvolvimento.

Linux/macOS:

    bash
    export DB_URL="jdbc:mysql://localhost:3306/streaming"
    export DB_USERNAME="root"
    export DB_PASSWORD="sua_senha"

Windows (PowerShell):

    powershell
    $env:DB_URL="jdbc:mysql://localhost:3306/streaming"
    $env:DB_USERNAME="root"
    $env:DB_PASSWORD="sua_senha"
    3. Execute
    bash
    ./mvnw spring-boot:run

As tabelas são criadas automaticamente pelo Hibernate e, na primeira execução, o catálogo é populado com filmes de exemplo.

A API sobe em http://localhost:8080.

📖 Testando pelo Swagger
Acesse http://localhost:8080/swagger-ui.html.
Cadastre um assinante em POST /usuarios/assinantes:

    json
    {
      "nome": "Ana Souza",
      "email": "ana@email.com",
      "idade": 30,
      "plano": "STANDARD",
      "senha": "senha123"
    }

Clique em Authorize e informe o e-mail em username e a senha.
Use GET /auth/me para confirmar o login e depois explore os demais endpoints.
✅ Testes

O projeto tem uma suíte ampla de testes automatizados (JUnit 5, Mockito, @DataJpaTest e @SpringBootTest), cobrindo:

    Regras de domínio: perfis por idade, hierarquia de planos, política de acesso a filmes
    Serviços: criação de usuários, limites por plano, migração de plano, reprodução e histórico
    Validação dos dados de entrada
    Integração: histórico, migração de plano e exclusão em cascata
    Segurança: autenticação, autorização por papel e geração/validação de tokens
    Documentação: presença dos endpoints e exemplos no OpenAPI
    bash
    ./mvnw test
    
🗺️ Próximos passos
    [ ] Migrar de ddl-auto=update para migrations versionadas (Flyway)
    [ ] Paginação na listagem de filmes e no histórico
    [ ] Containerização com Docker Compose (aplicação + MySQL)
    [ ] Pipeline de CI com GitHub Actions
    
👤 Autor

Rodrigo Marques Viana
