# Trocas

Microsserviço de **trocas de cartas** (S03 – Arquitetura de Software, Inatel). Um jogador anuncia uma troca oferecendo uma carta, outros jogadores enviam propostas e o dono aceita uma delas.

Diagramas de caso de uso e de classes: [`documentacao/`](documentacao/).

**Stack:** Java 21 · Spring Boot 3.3 · Spring Data JPA · Flyway · MySQL (produção) / H2 (desenvolvimento e testes) · Docker

## Rodando localmente

Todos os comandos abaixo rodam dentro da pasta `api/`.

### Opção 1: direto, com H2 em memória (mais rápido para desenvolver)

```bash
./mvnw spring-boot:run
```

- API em `http://localhost:8080`
- Banco H2 em memória (zera a cada reinício). Console em `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:trocas`, usuário `sa`, senha vazia.

### Opção 2: com Docker + MySQL (igual à produção)

```bash
docker compose up --build
```

Sobe a API com o perfil `prod` e um MySQL 8 local, configurado como a Aiven (exige chave primária em toda tabela) e com o mesmo limite de memória do Render (512 MB). Use isso para testar antes de fazer deploy.

`docker compose down` para tudo. Acrescente `-v` para apagar também os dados do MySQL.

Se a porta 8080 já estiver em uso (por exemplo, pela API rodando na IDE), escolha outra: `API_PORT=8081 docker compose up --build`.

### Testes

```bash
./mvnw test
```

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/trocas` | Cria uma troca (201 + header `Location`) |
| `GET` | `/trocas?status=&idJogador=&page=&perPage=&sort=` | Lista trocas com filtros e paginação |
| `GET` | `/trocas/{id}` | Busca uma troca |
| `GET` | `/trocas/{id}/propostas?page=&perPage=&sort=` | Lista as propostas de uma troca |
| `POST` | `/trocas/{id}/cancelar` | Cancela uma troca aberta (e suas propostas pendentes) |
| `POST` | `/propostas` | Cria uma proposta (201 + header `Location`) |
| `POST` | `/propostas/consultar?page=&perPage=&sort=` | Consulta propostas de uma troca com filtros opcionais; os critérios vão no **corpo** |
| `GET` | `/propostas/{id}` | Busca uma proposta |
| `POST` | `/propostas/{id}/aceitar` | Aceita a proposta, conclui a troca e recusa as demais |
| `POST` | `/propostas/{id}/cancelar` | Cancela uma proposta pendente |
| `GET` | `/actuator/health` | Health check |

- **Listagens:** respondem `{ "data": [...], "pagination": { "total", "page", "perPage", "lastPage" } }`. A primeira página é a 1.
- **Erros:** seguem a RFC 9457 (`application/problem+json`), com um campo extra `code` (ex.: `NOT_FOUND`, `BUSINESS_RULE_VIOLATION`).

### Trocas

```bash
curl -i -X POST http://localhost:8080/trocas -H "Content-Type: application/json" \
  -d '{"idJogador":"jogador1","idCartaOfertada":"c1","idCartaDesejada":"c2","limitePropostas":3}'
```

`idCartaDesejada` é opcional. Sem ela, o jogador aceita propostas com qualquer carta.

### Propostas

Outros jogadores enviam propostas para uma troca aberta; o dono escolhe uma delas.

**Criar** (`POST /propostas`):

```bash
curl -i -X POST http://localhost:8080/propostas -H "Content-Type: application/json" \
  -d '{"idTroca":"<id da troca>","idJogador":"jogador2","idCartaOfertada":"c2"}'
```

Resposta `201`, com o header `Location: /propostas/{id}`:

```json
{
  "id": "…", "idTroca": "…", "idJogador": "jogador2", "idCartaOfertada": "c2",
  "status": "PENDENTE", "criadaEm": "2026-11-18T23:10:00Z"
}
```

**Consultar propostas** (`POST /propostas/consultar`): os critérios vão no corpo; só `idTroca` é obrigatório. É um `POST` porque o corpo de um `GET` não é enviado por todos os clientes; a consulta não altera nada.

```bash
curl -X POST "http://localhost:8080/propostas/consultar?page=1&perPage=20" -H "Content-Type: application/json" \
  -d '{"idTroca":"<id da troca>","status":"PENDENTE","idJogador":"jogador2"}'
```

| Campo do corpo | Obrigatório | Descrição |
|---|---|---|
| `idTroca` | sim | Troca cujas propostas serão consultadas |
| `status` | não | `PENDENTE`, `ACEITA`, `RECUSADA` ou `CANCELADA` |
| `idJogador` | não | Só as propostas desse jogador |

O resultado é paginado, das mais antigas para as mais novas. Para listar **todas** as propostas de uma troca, sem filtro, também existe `GET /trocas/{id}/propostas`:

```bash
curl "http://localhost:8080/trocas/<id da troca>/propostas"
```

**Status da proposta:**

| Status | Quando |
|---|---|
| `PENDENTE` | Criada e aguardando o dono da troca |
| `ACEITA` | O dono escolheu esta proposta (a troca vira `CONCLUIDA`) |
| `RECUSADA` | Outra proposta da mesma troca foi aceita |
| `CANCELADA` | O jogador cancelou a proposta ou a troca foi cancelada |

Só propostas `PENDENTE` podem ser aceitas ou canceladas.

**Regras ao criar uma proposta** (violá-las devolve `422 BUSINESS_RULE_VIOLATION`):

- a troca precisa estar `ABERTA`;
- o dono da troca não pode propor para a própria troca;
- se a troca tem carta desejada, a proposta precisa oferecer exatamente essa carta (sem carta desejada, qualquer uma serve);
- o jogador precisa possuir a carta (consulta ao `Inventario`);
- só as propostas `PENDENTE` contam para o `limitePropostas`, então cancelar uma proposta libera a vaga.

**Aceitar** (`POST /propostas/{id}/aceitar`) aceita a proposta, conclui a troca e recusa as demais propostas pendentes, tudo na mesma transação. **Cancelar uma troca** também cancela as propostas pendentes dela.

**Códigos de erro mais comuns:**

| Situação | HTTP | `code` |
|---|---|---|
| Troca ou proposta inexistente | 404 | `NOT_FOUND` |
| Campo obrigatório ausente (ex.: `GET /propostas` sem `idTroca` no corpo) | 400 | `VALIDATION_ERROR` |
| `sort` por campo não permitido (permitidos: `criadaEm`, `idJogador`, `status`) | 400 | `INVALID_PARAMETER` |
| Regra de negócio violada | 422 | `BUSINESS_RULE_VIOLATION` |

## Banco de dados e migrações

O esquema do banco é controlado pelo **Flyway**: os scripts ficam em `api/src/main/resources/db/migration` e rodam automaticamente quando a API sobe. O Hibernate só **valida** se as entidades batem com as tabelas.

Para mudar o banco:

1. Crie um novo arquivo `V<próximo número>__descricao.sql` (ex.: `V2__completa_tabela_proposta.sql`).
2. **Nunca altere uma migração que já rodou em produção.**
3. O SQL precisa funcionar no MySQL e no H2. O H2 roda em modo MySQL, então use a sintaxe do MySQL.
4. Toda tabela precisa de chave primária: a Aiven recusa tabelas sem PK.

## Deploy (Render + Aiven)

A API roda no **Render** (via Docker) e o banco é um **MySQL na Aiven**.

### 1. Banco na Aiven

1. Escolha o banco (*database*):
   - **Serviço MySQL só deste microsserviço:** use o `defaultdb`, que a Aiven já cria.
   - **Serviço compartilhado com outros microsserviços:** crie um banco próprio (`CREATE DATABASE trocas;`). Cada microsserviço é dono do seu banco.
2. Na aba *Overview* do serviço, anote **host**, **porta**, **usuário** e **senha**. A Aiven usa uma porta própria (não a 3306) e **exige SSL**.
3. Em *Allowed IP addresses*, mantenha `0.0.0.0/0`. O plano gratuito do Render não tem IP fixo.

### 2. API no Render

1. *New* → *Web Service* → conecte o repositório `S03-2026-2/trocas`. Por ser de uma organização, um admin talvez precise autorizar o app do Render no GitHub.
2. Configuração:

   | Campo | Valor |
   |---|---|
   | Branch | `main` |
   | Root Directory | `api` |
   | Runtime / Language | `Docker` |
   | Health Check Path | `/actuator/health` |

3. Em *Environment*, cadastre as variáveis (modelo em [`api/.env.example`](api/.env.example)):

   | Variável | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | `jdbc:mysql://HOST:PORTA/BANCO?sslMode=REQUIRED` (ex.: `BANCO` = `defaultdb`) |
   | `DB_USERNAME` | usuário da Aiven |
   | `DB_PASSWORD` | senha da Aiven |
   | `CORS_ALLOWED_ORIGINS` | URLs dos front-ends que chamam a API pelo navegador, separadas por vírgula (opcional) |

   > ⚠️ **Não copie a *Service URI* da Aiven** (`mysql://usuario:senha@host:porta/defaultdb?ssl-mode=REQUIRED`) para a `DB_URL`: ela não está no formato JDBC. Monte a URL como na tabela, começando com `jdbc:mysql://`, sem usuário e senha, e com `sslMode` (sem hífen).

4. Faça o deploy. Na primeira subida o Flyway cria as tabelas no banco. Para testar: `https://SEU-SERVICO.onrender.com/actuator/health` deve responder `{"status":"UP"}`.

A cada push na `main`, o Render faz um novo deploy automaticamente.

### Limitações do plano gratuito

- **O Render "dorme" após ~15 min sem acesso.** A primeira requisição depois disso pode levar perto de um minuto. Antes de apresentar, acesse a API para acordá-la.
- **512 MB de RAM:** a JVM já está ajustada para isso no `Dockerfile`.

## Principais Design Patterns

Os padrões abaixo aparecem no código do serviço. Caminhos relativos a `api/src/main/java/com/inatel/s03/trocas/`.

| Padrão | Categoria | Onde está |
|---|---|---|
| [Repository](#repository) | Arquitetural (Fowler / DDD, fora do GoF) | `repositories/PropostaRepository.java`, `repositories/TrocaRepository.java` |
| [Strategy](#strategy) | Comportamental | `repositories/TrocaSpecs.java`, `repositories/PropostaSpecs.java` |
| [Facade](#facade) | Estrutural | `services/PropostaService.java` |
| [Singleton](#singleton) | Criacional | beans do Spring (`@Service`, `@Component`, `@RestController`) |

### Repository

**Repository Pattern**

Os Services não precisam escrever comandos SQL nem gerenciar conexões diretamente, eles acessam o banco por meio dos Repositories, que disponibilizam métodos para realizar operações de persistência, como `save()`, `findById()`, `findAll()` e consultas específicas, como `countByIdTrocaAndStatus()`. Dessa forma, o Repository isola a camada de acesso a dados da camada de serviços, deixando os Services responsáveis pelas regras de negócio. O projeto possui um Repository para cada entidade principal (`Troca` e `Proposta`).

[`repositories/PropostaRepository.java`](api/src/main/java/com/inatel/s03/trocas/repositories/PropostaRepository.java#L11-L17):

```java
@Repository
public interface PropostaRepository extends JpaRepository<Proposta, String>, JpaSpecificationExecutor<Proposta> {

    List<Proposta> findByIdTrocaAndStatus(String idTroca, StatusProposta status);

    long countByIdTrocaAndStatus(String idTroca, StatusProposta status);
}
```

Uso em [`services/PropostaService.java`](api/src/main/java/com/inatel/s03/trocas/services/PropostaService.java#L50-L60): a regra de negócio pergunta ao repositório sem saber que existe SQL.

```java
long pendentes = propostaRepository.countByIdTrocaAndStatus(idTroca, StatusProposta.PENDENTE);
if (pendentes >= troca.getLimitePropostas()) {
    throw new RegraNegocioException(...);
}
...
return propostaRepository.save(proposta);
```

### Strategy

Cada filtro da listagem (por status, por jogador) é um objeto `Specification`. Todos têm a mesma interface, e o repositório executa o que receber em `findAll(spec, paginacao)` sem saber qual regra é. Filtro não informado vira `null` e é ignorado. Um filtro novo é só uma nova estratégia, sem mexer no repositório nem no service.

[`repositories/TrocaSpecs.java`](api/src/main/java/com/inatel/s03/trocas/repositories/TrocaSpecs.java#L17-L33):

```java
public static Specification<Troca> comFiltro(FiltroTrocas filtro) {
    if (filtro == null) {
        return Specification.where(null);
    }
    return Specification.where(comStatus(filtro.status()))
            .and(doJogador(filtro.idJogador()));
}

private static Specification<Troca> comStatus(StatusTroca status) {
    return status == null ? null
            : (root, query, cb) -> cb.equal(root.get("status"), status);
}
```

O contexto, em [`services/TrocaService.java`](api/src/main/java/com/inatel/s03/trocas/services/TrocaService.java#L59), não sabe qual regra está executando:

```java
return trocaRepository.findAll(TrocaSpecs.comFiltro(filtro), comDesempate);
```

A mesma ideia aparece em [`dtos/PaginaResponse.java`](api/src/main/java/com/inatel/s03/trocas/dtos/PaginaResponse.java#L16-L21), que recebe a estratégia de conversão (`TrocaResponse::de` ou `PropostaResponse::de`) como parâmetro:

```java
public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> conversor) {
    return new PaginaResponse<>(
            pagina.getContent().stream().map(conversor).toList(),
            Paginacao.de(pagina)
    );
}
```

> A `Specification` (Evans e Fowler) é uma especialização de Strategy: a interface é a `Specification<T>` do Spring Data, e as lambdas são as implementações concretas.

### Facade

O controller chama uma linha só. Por trás dessa fachada, o service busca a proposta, busca a troca em outro service, aceita a proposta, conclui a troca e recusa as demais propostas pendentes, tudo numa única transação. O controller não conhece repositório, regra de negócio nem transação.

Cliente da fachada, em [`controllers/PropostaController.java`](api/src/main/java/com/inatel/s03/trocas/controllers/PropostaController.java#L59-L62):

```java
@PostMapping("/{id}/aceitar")
public PropostaResponse aceitar(@PathVariable String id) {
    return PropostaResponse.de(propostaService.aceitarProposta(id));
}
```

A fachada, em [`services/PropostaService.java`](api/src/main/java/com/inatel/s03/trocas/services/PropostaService.java#L89-L101):

```java
@Transactional
public Proposta aceitarProposta(String idProposta) {
    Proposta aceita = buscarProposta(idProposta);
    Troca troca = trocaService.buscarTroca(aceita.getIdTroca());

    aceita.aceitar();
    troca.concluir();

    propostaRepository.findByIdTrocaAndStatus(troca.getId(), StatusProposta.PENDENTE)
            .forEach(Proposta::recusar);

    return aceita;
}
```

> A camada de serviço (Service Layer, do Fowler) é uma aplicação de Facade: uma interface simples na frente de um subsistema com várias classes (repositórios, entidades, outro service, inventário). O [`cancelarTroca`](api/src/main/java/com/inatel/s03/trocas/services/TrocaService.java#L68-L76) segue o mesmo modelo: cancela a troca e as propostas pendentes dela.

### Singleton

Classes anotadas com `@Service`, `@Component` e `@RestController` viram beans com escopo *singleton*: o Spring cria uma única instância e entrega essa mesma instância a quem precisar. O `TrocaService` usado pelo `TrocaController` é o mesmo que o `PropostaService` recebe. Ninguém faz `new TrocaService()`: o construtor gerado pelo `@RequiredArgsConstructor` recebe as dependências prontas (Injeção de Dependência).

[`services/PropostaService.java`](api/src/main/java/com/inatel/s03/trocas/services/PropostaService.java#L23-L30):

```java
@Service
@RequiredArgsConstructor
public class PropostaService {
    private final PropostaRepository propostaRepository;
    private final TrocaService trocaService;   // a mesma instância usada pelo TrocaController
    private final Inventario inventario;       // o mesmo DistribuicaoCartas usado pelo TrocaService
```

[`controllers/TrocaController.java`](api/src/main/java/com/inatel/s03/trocas/controllers/TrocaController.java#L22-L28):

```java
@RestController
@RequestMapping("/trocas")
@RequiredArgsConstructor
public class TrocaController {

    private final TrocaService trocaService;
    private final PropostaService propostaService;
```

> Não há `getInstance()` nem construtor privado: é o Singleton do contêiner (IoC). Quem garante a instância única é o Spring, e a classe continua testável, porque nos testes dá para injetar um dublê no lugar dela.

## Pendências

- Propostas (criar, consultar, aceitar e cancelar).
- Integração real com o serviço de Distribuição de Cartas (hoje simulada: sempre confirma que o jogador possui a carta).
- Publicação do evento `TrocaRealizadaEvento` no broker.
- Autenticação.
