DROP TABLE proposta;

CREATE TABLE proposta (
                          id                VARCHAR(36)  NOT NULL,
                          id_troca          VARCHAR(36)  NOT NULL,
                          id_jogador        VARCHAR(255) NOT NULL,
                          id_carta_ofertada VARCHAR(255) NOT NULL,
                          status            VARCHAR(20)  NOT NULL,
                          criada_em         DATETIME(6)  NOT NULL,
                          CONSTRAINT pk_proposta PRIMARY KEY (id),
                          CONSTRAINT fk_proposta_troca FOREIGN KEY (id_troca) REFERENCES trocas (id)
);

CREATE INDEX idx_proposta_id_troca_status ON proposta (id_troca, status);