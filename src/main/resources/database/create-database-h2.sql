-- =====================================================================================
-- SCRIPT COMPLETO DO BANCO DE DADOS SHIFTLY PARA H2
-- Sistema de Controle de Ponto Eletrônico com Reconhecimento Facial e Geolocalização
-- Compatível com H2 Database (In-Memory)
-- =====================================================================================

-- =====================================================================================
-- TABELA: USUARIOS
-- =====================================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    tipo_usuario VARCHAR(20) NOT NULL CHECK (tipo_usuario IN ('COLABORADOR', 'RH', 'ADMIN')),
    cargo VARCHAR(50),
    departamento VARCHAR(50),
    salario DECIMAL(10,2),
    data_admissao TIMESTAMP,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    face_encoding CLOB, -- JSON com dados do rosto
    telefone VARCHAR(20),
    endereco VARCHAR(200),
    cep VARCHAR(10),
    data_nascimento DATE,
    cpf VARCHAR(14),
    rg VARCHAR(20),
    foto_perfil VARCHAR(255) -- caminho para foto
);

-- Índices para usuarios
CREATE INDEX IF NOT EXISTS IX_usuarios_email ON usuarios (email);
CREATE INDEX IF NOT EXISTS IX_usuarios_tipo ON usuarios (tipo_usuario);
CREATE INDEX IF NOT EXISTS IX_usuarios_ativo ON usuarios (ativo);

-- =====================================================================================
-- TABELA: PONTOS
-- =====================================================================================
CREATE TABLE IF NOT EXISTS pontos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_ponto VARCHAR(10) NOT NULL CHECK (tipo_ponto IN ('ENTRADA', 'SAIDA')),
    
    -- Geolocalização
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    endereco VARCHAR(200),
    precisao DECIMAL(8, 2), -- em metros
    
    -- Reconhecimento facial
    face_match DECIMAL(5, 4), -- percentual de match (0.0000 a 1.0000)
    face_validada BOOLEAN DEFAULT FALSE,
    face_data CLOB, -- dados da foto capturada
    
    -- Controle e auditoria
    observacoes VARCHAR(500),
    manual BOOLEAN DEFAULT FALSE, -- se foi inserido manualmente
    corrigido_por_usuario_id BIGINT,
    data_correcao TIMESTAMP,
    motivo_correcao VARCHAR(500),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (corrigido_por_usuario_id) REFERENCES usuarios(id)
);

-- Índices para pontos
CREATE INDEX IF NOT EXISTS IX_pontos_usuario_data ON pontos (usuario_id, data_hora);
CREATE INDEX IF NOT EXISTS IX_pontos_data ON pontos (data_hora);
CREATE INDEX IF NOT EXISTS IX_pontos_tipo ON pontos (tipo_ponto);
CREATE INDEX IF NOT EXISTS IX_pontos_validacao ON pontos (face_validada);

-- =====================================================================================
-- TABELA: FERIAS
-- =====================================================================================
CREATE TABLE IF NOT EXISTS ferias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    dias_solicitados INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA', 'CANCELADA')),
    observacoes VARCHAR(1000),
    motivo_recusa VARCHAR(500),
    
    -- Auditoria
    aprovado_por_usuario_id BIGINT,
    data_aprovacao TIMESTAMP,
    data_solicitacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (aprovado_por_usuario_id) REFERENCES usuarios(id),
    
    -- Constraints
    CHECK (data_fim >= data_inicio),
    CHECK (dias_solicitados > 0)
);

-- Índices para ferias
CREATE INDEX IF NOT EXISTS IX_ferias_usuario ON ferias (usuario_id);
CREATE INDEX IF NOT EXISTS IX_ferias_status ON ferias (status);
CREATE INDEX IF NOT EXISTS IX_ferias_datas ON ferias (data_inicio, data_fim);

-- =====================================================================================
-- TABELA: HORAS_EXTRAS
-- =====================================================================================
CREATE TABLE IF NOT EXISTS horas_extras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    data DATE NOT NULL,
    horas DECIMAL(4,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA')),
    descricao VARCHAR(500) NOT NULL,
    justificativa VARCHAR(1000),
    motivo_recusa VARCHAR(500),
    
    -- Auditoria
    aprovado_por_usuario_id BIGINT,
    data_aprovacao TIMESTAMP,
    data_solicitacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (aprovado_por_usuario_id) REFERENCES usuarios(id),
    
    -- Constraints
    CHECK (horas > 0 AND horas <= 12) -- máximo 12h extras por dia
);

-- Índices para horas_extras
CREATE INDEX IF NOT EXISTS IX_horas_extras_usuario ON horas_extras (usuario_id);
CREATE INDEX IF NOT EXISTS IX_horas_extras_status ON horas_extras (status);
CREATE INDEX IF NOT EXISTS IX_horas_extras_data ON horas_extras (data);

-- =====================================================================================
-- TABELA: COMPROVANTES
-- =====================================================================================
CREATE TABLE IF NOT EXISTS comprovantes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo_comprovante VARCHAR(20) NOT NULL CHECK (tipo_comprovante IN ('SALARIO', 'FERIAS', 'RESCISAO', 'ADIANTAMENTO')),
    referencia VARCHAR(50) NOT NULL, -- ano/mês ou período
    data_emissao DATE NOT NULL DEFAULT CURRENT_DATE,
    
    -- Período de referência
    periodo_inicio DATE NOT NULL,
    periodo_fim DATE NOT NULL,
    
    -- Valores
    valor_bruto DECIMAL(10,2) NOT NULL,
    valor_descontos DECIMAL(10,2) DEFAULT 0,
    valor_liquido DECIMAL(10,2) NOT NULL,
    salario_base DECIMAL(10,2),
    horas_extras DECIMAL(10,2) DEFAULT 0,
    adicional_noturno DECIMAL(10,2) DEFAULT 0,
    vale_transporte DECIMAL(10,2) DEFAULT 0,
    vale_refeicao DECIMAL(10,2) DEFAULT 0,
    plano_saude DECIMAL(10,2) DEFAULT 0,
    inss DECIMAL(10,2) DEFAULT 0,
    irrf DECIMAL(10,2) DEFAULT 0,
    fgts DECIMAL(10,2) DEFAULT 0,
    
    -- Arquivo
    arquivo_nome VARCHAR(255),
    arquivo_caminho VARCHAR(500),
    arquivo_tamanho BIGINT,
    
    -- Auditoria
    gerado_por_usuario_id BIGINT,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (gerado_por_usuario_id) REFERENCES usuarios(id)
);

-- Índices para comprovantes
CREATE INDEX IF NOT EXISTS IX_comprovantes_usuario ON comprovantes (usuario_id);
CREATE INDEX IF NOT EXISTS IX_comprovantes_tipo ON comprovantes (tipo_comprovante);
CREATE INDEX IF NOT EXISTS IX_comprovantes_referencia ON comprovantes (referencia);
CREATE INDEX IF NOT EXISTS IX_comprovantes_emissao ON comprovantes (data_emissao);

-- =====================================================================================
-- TABELA: CONFIGURACOES_SISTEMA
-- =====================================================================================
CREATE TABLE IF NOT EXISTS configuracoes_sistema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chave VARCHAR(100) NOT NULL UNIQUE,
    valor VARCHAR(1000) NOT NULL,
    descricao VARCHAR(500),
    tipo VARCHAR(20) NOT NULL DEFAULT 'STRING' CHECK (tipo IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
    categoria VARCHAR(50) NOT NULL DEFAULT 'GERAL',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para configuracoes_sistema
CREATE INDEX IF NOT EXISTS IX_configuracoes_chave ON configuracoes_sistema (chave);
CREATE INDEX IF NOT EXISTS IX_configuracoes_categoria ON configuracoes_sistema (categoria);

-- =====================================================================================
-- TABELA: NOTIFICACOES
-- =====================================================================================
CREATE TABLE IF NOT EXISTS notificacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    mensagem VARCHAR(500) NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'INFO' CHECK (tipo IN ('INFO', 'SUCESSO', 'AVISO', 'ERRO')),
    lida BOOLEAN DEFAULT FALSE,
    url_acao VARCHAR(200), -- URL para ação relacionada
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_leitura TIMESTAMP,
    
    -- Relacionamentos
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices para notificacoes
CREATE INDEX IF NOT EXISTS IX_notificacoes_usuario ON notificacoes (usuario_id);
CREATE INDEX IF NOT EXISTS IX_notificacoes_lida ON notificacoes (lida);
CREATE INDEX IF NOT EXISTS IX_notificacoes_data ON notificacoes (data_criacao);

-- =====================================================================================
-- TABELA: LOG_AUDITORIA
-- =====================================================================================
CREATE TABLE IF NOT EXISTS log_auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,
    acao VARCHAR(100) NOT NULL,
    tabela_afetada VARCHAR(50),
    registro_id BIGINT,
    dados_anteriores CLOB, -- JSON
    dados_novos CLOB, -- JSON
    ip_origem VARCHAR(45),
    user_agent VARCHAR(500),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Relacionamentos
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices para log_auditoria
CREATE INDEX IF NOT EXISTS IX_auditoria_usuario ON log_auditoria (usuario_id);
CREATE INDEX IF NOT EXISTS IX_auditoria_acao ON log_auditoria (acao);
CREATE INDEX IF NOT EXISTS IX_auditoria_tabela ON log_auditoria (tabela_afetada);
CREATE INDEX IF NOT EXISTS IX_auditoria_data ON log_auditoria (data_criacao);

-- =====================================================================================
-- INSERIR CONFIGURAÇÕES PADRÃO DO SISTEMA
-- =====================================================================================
INSERT INTO configuracoes_sistema (chave, valor, descricao, tipo, categoria) VALUES
('HORAS_TRABALHO_DIA', '8.0', 'Horas de trabalho por dia', 'NUMBER', 'JORNADA'),
('HORAS_TRABALHO_SEMANA', '40.0', 'Horas de trabalho por semana', 'NUMBER', 'JORNADA'),
('TOLERANCIA_ATRASO_MINUTOS', '15', 'Tolerancia para atraso em minutos', 'NUMBER', 'JORNADA'),
('MAX_HORAS_EXTRAS_DIA', '4.0', 'Maximo de horas extras por dia', 'NUMBER', 'HORAS_EXTRAS'),
('MAX_HORAS_EXTRAS_MES', '60.0', 'Maximo de horas extras por mes', 'NUMBER', 'HORAS_EXTRAS'),
('PERCENTUAL_HORA_EXTRA', '50', 'Percentual adicional para hora extra (%)', 'NUMBER', 'HORAS_EXTRAS'),
('DIAS_FERIAS_ANO', '30', 'Dias de ferias por ano', 'NUMBER', 'FERIAS'),
('ANTECEDENCIA_FERIAS_DIAS', '30', 'Antecedencia minima para solicitar ferias (dias)', 'NUMBER', 'FERIAS'),
('RAIO_MAXIMO_PONTO_METROS', '100', 'Raio maximo para registrar ponto (metros)', 'NUMBER', 'LOCALIZACAO'),
('PRECISAO_FACE_MINIMA', '0.85', 'Precisao minima para reconhecimento facial', 'NUMBER', 'BIOMETRIA'),
('BACKUP_AUTOMATICO', 'true', 'Ativar backup automatico', 'BOOLEAN', 'SISTEMA'),
('SINCRONIZACAO_ATIVA', 'true', 'Ativar sincronizacao automatica', 'BOOLEAN', 'SISTEMA'),
('TEMA_INTERFACE', 'default', 'Tema da interface', 'STRING', 'INTERFACE'),
('EMAIL_NOTIFICACOES', 'true', 'Enviar notificacoes por email', 'BOOLEAN', 'NOTIFICACOES');

-- =====================================================================================
-- INSERIR USUARIOS PADRAO DO SISTEMA
-- =====================================================================================
INSERT INTO usuarios (nome, email, senha, tipo_usuario, cargo, departamento, ativo) VALUES
('Administrador do Sistema', 'admin@shiftly.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'ADMIN', 'Administrador', 'TI', TRUE),
('Maria Silva', 'maria.silva@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'RH', 'Analista de RH', 'Recursos Humanos', TRUE),
('Joao Santos', 'joao.santos@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Desenvolvedor', 'TI', TRUE),
('Ana Oliveira', 'ana.oliveira@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Designer', 'Marketing', TRUE),
('Pedro Costa', 'pedro.costa@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Analista', 'Vendas', TRUE),
('Carla Ferreira', 'carla.ferreira@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Assistente', 'Financeiro', TRUE);

-- =====================================================================================
-- INSERIR DADOS DE EXEMPLO PARA DEMONSTRAÇÃO
-- =====================================================================================

-- Pontos de exemplo
INSERT INTO pontos (usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, face_validada) VALUES
(3, DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', TRUE),
(4, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', TRUE),
(5, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', TRUE),
(3, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', TRUE),
(3, DATEADD('DAY', -1, DATEADD('HOUR', 8, CURRENT_TIMESTAMP)), 'SAIDA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', TRUE);

-- Solicitacoes de ferias de exemplo
INSERT INTO ferias (usuario_id, data_inicio, data_fim, dias_solicitados, status, observacoes) VALUES
(3, DATEADD('DAY', 30, CURRENT_DATE), DATEADD('DAY', 44, CURRENT_DATE), 15, 'PENDENTE', 'Ferias de final de ano'),
(4, DATEADD('DAY', 60, CURRENT_DATE), DATEADD('DAY', 69, CURRENT_DATE), 10, 'PENDENTE', 'Viagem em familia'),
(5, DATEADD('DAY', 90, CURRENT_DATE), DATEADD('DAY', 104, CURRENT_DATE), 15, 'APROVADA', 'Ferias aprovadas pelo RH');

-- Horas extras de exemplo
INSERT INTO horas_extras (usuario_id, data, horas, status, descricao) VALUES
(3, CURRENT_DATE, 2.5, 'APROVADA', 'Finalizacao de projeto urgente'),
(4, DATEADD('DAY', -1, CURRENT_DATE), 1.5, 'PENDENTE', 'Correcao de bugs'),
(5, DATEADD('DAY', -2, CURRENT_DATE), 3.0, 'APROVADA', 'Deploy de sistema');

-- Comprovantes de exemplo
INSERT INTO comprovantes (usuario_id, tipo_comprovante, referencia, periodo_inicio, periodo_fim, valor_bruto, valor_liquido, salario_base) VALUES
(3, 'SALARIO', '2024/09', DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, 5000.00, 4200.00, 5000.00),
(4, 'SALARIO', '2024/09', DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, 4500.00, 3800.00, 4500.00),
(5, 'SALARIO', '2024/09', DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, 4000.00, 3400.00, 4000.00);

-- Notificacoes de exemplo
INSERT INTO notificacoes (usuario_id, titulo, mensagem, tipo) VALUES
(3, 'Bem-vindo ao Shiftly!', 'Seu cadastro foi realizado com sucesso. Explore todas as funcionalidades do sistema.', 'SUCESSO'),
(4, 'Solicitacao de Ferias', 'Sua solicitacao de ferias esta aguardando aprovacao do RH.', 'INFO'),
(5, 'Ponto Registrado', 'Seu ponto de entrada foi registrado com sucesso as 08:00.', 'SUCESSO');

-- =====================================================================================
-- VIEWS PARA RELATÓRIOS (H2 Compatible)
-- =====================================================================================

-- View: Resumo de pontos por usuário
CREATE OR REPLACE VIEW vw_resumo_pontos_usuario AS
SELECT 
    u.id,
    u.nome,
    u.email,
    u.departamento,
    COUNT(p.id) as total_pontos,
    COUNT(CASE WHEN p.tipo_ponto = 'ENTRADA' THEN 1 END) as total_entradas,
    COUNT(CASE WHEN p.tipo_ponto = 'SAIDA' THEN 1 END) as total_saidas,
    COUNT(CASE WHEN p.face_validada = TRUE THEN 1 END) as pontos_validados,
    MAX(p.data_hora) as ultimo_ponto
FROM usuarios u
LEFT JOIN pontos p ON u.id = p.usuario_id
WHERE u.ativo = TRUE
GROUP BY u.id, u.nome, u.email, u.departamento;

-- View: Horas trabalhadas por dia (H2 Compatible)
CREATE OR REPLACE VIEW vw_horas_trabalhadas_dia AS
SELECT 
    u.id,
    u.nome,
    CAST(p.data_hora AS DATE) as data,
    MIN(CASE WHEN p.tipo_ponto = 'ENTRADA' THEN p.data_hora END) as primeira_entrada,
    MAX(CASE WHEN p.tipo_ponto = 'SAIDA' THEN p.data_hora END) as ultima_saida,
    CASE 
        WHEN MIN(CASE WHEN p.tipo_ponto = 'ENTRADA' THEN p.data_hora END) IS NOT NULL 
         AND MAX(CASE WHEN p.tipo_ponto = 'SAIDA' THEN p.data_hora END) IS NOT NULL
        THEN DATEDIFF('HOUR', 
             MIN(CASE WHEN p.tipo_ponto = 'ENTRADA' THEN p.data_hora END),
             MAX(CASE WHEN p.tipo_ponto = 'SAIDA' THEN p.data_hora END))
        ELSE 0
    END as horas_trabalhadas
FROM usuarios u
LEFT JOIN pontos p ON u.id = p.usuario_id
WHERE u.ativo = TRUE
GROUP BY u.id, u.nome, CAST(p.data_hora AS DATE);

-- =====================================================================================
-- FUNCOES AUXILIARES H2
-- =====================================================================================

-- Funcao para calcular saldo de ferias (H2 compatible)
-- Nota: H2 nao suporta funcoes Java complexas, sera implementada no codigo Java
