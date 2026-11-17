-- Esquema inicial do serviço de trocas.
--
-- Migrações do Flyway: cada arquivo V<n>__descricao.sql roda uma única vez, em ordem.
-- NUNCA altere um arquivo que já rodou em produção; crie um novo (V2__..., V3__...).
--
-- O SQL precisa funcionar no MySQL (produção, Aiven) e no H2 em modo MySQL (desenvolvimento e testes).
-- Toda tabela precisa de chave primária: a Aiven recusa tabelas sem PK (sql_require_primary_key=ON).

CREATE TABLE trocas (
    id                VARCHAR(36)  NOT NULL,
    id_jogador        VARCHAR(255) NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    limite_propostas  INT          NOT NULL,
    id_carta_ofertada VARCHAR(255) NOT NULL,
    id_carta_desejada VARCHAR(255),
    criada_em         DATETIME(6)  NOT NULL,
    versao            BIGINT,
    CONSTRAINT pk_trocas PRIMARY KEY (id)
);

-- Índices para os filtros da listagem (GET /trocas?status=&idJogador=) e a ordenação padrão
CREATE INDEX idx_trocas_status ON trocas (status);
CREATE INDEX idx_trocas_id_jogador ON trocas (id_jogador);
CREATE INDEX idx_trocas_criada_em ON trocas (criada_em);

-- Esqueleto da entidade Proposta (ainda não implementada).
-- A feature de propostas vai completar esta tabela numa nova migração.
CREATE TABLE proposta (
    id         VARCHAR(255) NOT NULL,
    id_jogador VARCHAR(255),
    CONSTRAINT pk_proposta PRIMARY KEY (id)
);
