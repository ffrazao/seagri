-- V1__schema_inicial.sql
-- Script de Criação Inicial do MVP - SEAGRI (PostgreSQL)
-- Arquitetura: Multi-Tenant, ReBAC (Grafos), Imutabilidade e Auditoria Plena (em Português)

-- 1. organizacao
CREATE TABLE organizacao (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ATIVO',
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP
);
CREATE INDEX idx_org_criado_por ON organizacao(criado_por);

-- 2. unidade
-- Nota: Sem id_pai. A hierarquia é definida via tabela 'relacionamento' (Grafos) [6].
CREATE TABLE unidade (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    tipo_geometria VARCHAR(16) NOT NULL, -- RAIO | POLIGONO
    centro_geo_lat DOUBLE PRECISION,
    centro_geo_lng DOUBLE PRECISION,
    raio_geo_metros INTEGER,
    poligono_geo JSONB,
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP,
    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
);
CREATE INDEX idx_unidade_org ON unidade(organizacao_id);

-- 3. vinculo_usuario
CREATE TABLE vinculo_usuario (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    usuario_id VARCHAR(64) NOT NULL, -- Keycloak sub
    papel VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ATIVO',
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP,
    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
);
-- Índice único garante que não usemos chave primária composta
CREATE UNIQUE INDEX uq_vinculo_usuario_org ON vinculo_usuario(usuario_id, organizacao_id);

-- 4. convite
CREATE TABLE convite (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    codigo VARCHAR(32) NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP,
    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
);
CREATE UNIQUE INDEX uq_convite_codigo ON convite(codigo);

-- 5. perfil_biometrico
CREATE TABLE perfil_biometrico (
    id UUID PRIMARY KEY,
    usuario_id VARCHAR(64) NOT NULL,
    modelo_biometrico BYTEA NOT NULL,
    versao_modelo VARCHAR(32) NOT NULL,
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP
);

-- 6. registro_presenca (ESTRITAMENTE IMUTÁVEL)
-- Todas as colunas antifraude e índices da arquitetura base mantidos [2-5, 7].
CREATE TABLE registro_presenca (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    unidade_id UUID NOT NULL,
    usuario_id VARCHAR(64) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    precisao_gps DOUBLE PRECISION,
    referencia_biometrica UUID,
    pontuacao_biometrica DOUBLE PRECISION,
    biometria_valida BOOLEAN,
    dispositivo_id VARCHAR(128) NOT NULL,
    modo_registro VARCHAR(16) NOT NULL, -- PROPRIO | COMPARTILHADO
    usuario_intermediario_id VARCHAR(64),
    pontuacao_risco DOUBLE PRECISION,
    indicadores_risco JSONB,
    status_tecnico VARCHAR(32) NOT NULL DEFAULT 'RECEBIDO',
    status_administrativo VARCHAR(32) NOT NULL DEFAULT 'PENDENTE',
    capturado_em TIMESTAMP NOT NULL, -- Timestamp do client (Dispositivo)
    recebido_no_servidor_em TIMESTAMP NOT NULL, -- Timestamp real de chegada no backend
    criado_por VARCHAR(64) NOT NULL, -- Sistema/Servidor que efetuou a ação
    criado_em TIMESTAMP NOT NULL, -- Momento da persistência no banco
    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id),
    FOREIGN KEY (unidade_id) REFERENCES unidade(id)
);
CREATE INDEX idx_presenca_org_usuario ON registro_presenca(organizacao_id, usuario_id);
CREATE INDEX idx_presenca_org_tempo ON registro_presenca(organizacao_id, recebido_no_servidor_em);
CREATE INDEX idx_presenca_unidade_tempo ON registro_presenca(unidade_id, recebido_no_servidor_em);

-- 7. dispositivo
CREATE TABLE dispositivo (
    id UUID PRIMARY KEY,
    identificador_dispositivo VARCHAR(128) NOT NULL,
    usuario_id VARCHAR(64),
    nivel_confianca VARCHAR(16) NOT NULL,
    plataforma VARCHAR(32),
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP,
    UNIQUE(identificador_dispositivo)
);

-- 8. log_auditoria (ESTRITAMENTE IMUTÁVEL)
CREATE TABLE log_auditoria (
    id UUID PRIMARY KEY,
    organizacao_id UUID,
    acao VARCHAR(128) NOT NULL,
    tipo_entidade VARCHAR(64),
    entidade_id UUID,
    decisao VARCHAR(16),
    metadados JSONB,
    criado_por VARCHAR(64) NOT NULL, 
    criado_em TIMESTAMP NOT NULL
);
CREATE INDEX idx_auditoria_org_tempo ON log_auditoria(organizacao_id, criado_em);

-- 9. consentimento
CREATE TABLE consentimento (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    usuario_id VARCHAR(64) NOT NULL,
    tipo_consentimento VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ATIVO',
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP,
    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
);

-- 10. relacionamento (O Coração do Grafo ReBAC) [6, 8]
CREATE TABLE relacionamento (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    sujeito_id VARCHAR(64) NOT NULL, -- Ex: ID do Gabinete ou ID do Usuário
    objeto_id VARCHAR(64) NOT NULL, -- Ex: ID do Conselho ou da Unidade
    tipo_relacionamento VARCHAR(64) NOT NULL, -- Ex: SUBORDINADO_A, MEMBRO_DE
    inicio_validade TIMESTAMP,
    fim_validade TIMESTAMP,
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP,
    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
);
CREATE INDEX idx_relacionamento_org_sujeito ON relacionamento(organizacao_id, sujeito_id);
CREATE INDEX idx_relacionamento_org_objeto ON relacionamento(organizacao_id, objeto_id);

-- 11. excecao_permissao_usuario
CREATE TABLE excecao_permissao_usuario (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    usuario_id VARCHAR(64) NOT NULL,
    recurso VARCHAR(128) NOT NULL,
    escopo VARCHAR(64) NOT NULL,
    condicao_json JSONB,
    criado_por VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_por VARCHAR(64),
    atualizado_em TIMESTAMP,
    FOREIGN KEY (organizacao_id) REFERENCES organizacao(id)
);
CREATE INDEX idx_permissao_org_usuario ON excecao_permissao_usuario(organizacao_id, usuario_id);