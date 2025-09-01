-- =====================================================================================
-- SCRIPT COMPLETO DO BANCO DE DADOS SHIFTLY
-- Sistema de Controle de Ponto Eletrônico com Reconhecimento Facial e Geolocalização
-- =====================================================================================

-- Criar banco de dados (se não existir)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'ShiftlyDB')
BEGIN
    CREATE DATABASE ShiftlyDB
    COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Banco de dados ShiftlyDB criado com sucesso!';
END
ELSE
BEGIN
    PRINT 'Banco de dados ShiftlyDB já existe.';
END;

USE ShiftlyDB;

-- =====================================================================================
-- TABELA: USUARIOS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='usuarios' AND xtype='U')
BEGIN
    CREATE TABLE usuarios (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        nome NVARCHAR(100) NOT NULL,
        email NVARCHAR(100) NOT NULL UNIQUE,
        senha NVARCHAR(255) NOT NULL,
        tipo_usuario NVARCHAR(20) NOT NULL CHECK (tipo_usuario IN ('COLABORADOR', 'RH', 'ADMIN')),
        cargo NVARCHAR(50),
        departamento NVARCHAR(50),
        salario DECIMAL(10,2),
        data_admissao DATETIME2,
        data_criacao DATETIME2 DEFAULT GETDATE(),
        data_atualizacao DATETIME2 DEFAULT GETDATE(),
        ativo BIT DEFAULT 1,
        face_encoding NVARCHAR(MAX), -- JSON com dados do rosto
        telefone NVARCHAR(20),
        endereco NVARCHAR(200),
        cep NVARCHAR(10),
        data_nascimento DATE,
        cpf NVARCHAR(14),
        rg NVARCHAR(20),
        foto_perfil NVARCHAR(255), -- caminho para foto
        
        -- Índices
        INDEX IX_usuarios_email (email),
        INDEX IX_usuarios_tipo (tipo_usuario),
        INDEX IX_usuarios_ativo (ativo)
    );
    PRINT 'Tabela usuarios criada com sucesso!';
END;

-- =====================================================================================
-- TABELA: PONTOS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='pontos' AND xtype='U')
BEGIN
    CREATE TABLE pontos (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        usuario_id BIGINT NOT NULL,
        data_hora DATETIME2 NOT NULL DEFAULT GETDATE(),
        tipo_ponto NVARCHAR(10) NOT NULL CHECK (tipo_ponto IN ('ENTRADA', 'SAIDA')),
        
        -- Geolocalização
        latitude DECIMAL(10, 8),
        longitude DECIMAL(11, 8),
        endereco NVARCHAR(200),
        precisao DECIMAL(8, 2), -- em metros
        
        -- Reconhecimento facial
        face_match DECIMAL(5, 4), -- percentual de match (0.0000 a 1.0000)
        face_validada BIT DEFAULT 0,
        face_data NVARCHAR(MAX), -- dados da foto capturada
        
        -- Controle e auditoria
        observacoes NVARCHAR(500),
        manual BIT DEFAULT 0, -- se foi inserido manualmente
        corrigido_por_usuario_id BIGINT,
        data_correcao DATETIME2,
        motivo_correcao NVARCHAR(500),
        data_criacao DATETIME2 DEFAULT GETDATE(),
        
        -- Relacionamentos
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
        FOREIGN KEY (corrigido_por_usuario_id) REFERENCES usuarios(id),
        
        -- Índices
        INDEX IX_pontos_usuario_data (usuario_id, data_hora),
        INDEX IX_pontos_data (data_hora),
        INDEX IX_pontos_tipo (tipo_ponto),
        INDEX IX_pontos_validacao (face_validada)
    );
    PRINT 'Tabela pontos criada com sucesso!';
END;

-- =====================================================================================
-- TABELA: FERIAS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ferias' AND xtype='U')
BEGIN
    CREATE TABLE ferias (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        usuario_id BIGINT NOT NULL,
        data_inicio DATE NOT NULL,
        data_fim DATE NOT NULL,
        dias_solicitados INT NOT NULL,
        status NVARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA', 'CANCELADA')),
        observacoes NVARCHAR(1000),
        motivo_recusa NVARCHAR(500),
        
        -- Auditoria
        aprovado_por_usuario_id BIGINT,
        data_aprovacao DATETIME2,
        data_solicitacao DATETIME2 DEFAULT GETDATE(),
        data_criacao DATETIME2 DEFAULT GETDATE(),
        data_atualizacao DATETIME2 DEFAULT GETDATE(),
        
        -- Relacionamentos
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
        FOREIGN KEY (aprovado_por_usuario_id) REFERENCES usuarios(id),
        
        -- Índices
        INDEX IX_ferias_usuario (usuario_id),
        INDEX IX_ferias_status (status),
        INDEX IX_ferias_datas (data_inicio, data_fim),
        
        -- Constraints
        CHECK (data_fim >= data_inicio),
        CHECK (dias_solicitados > 0)
    );
    PRINT 'Tabela ferias criada com sucesso!';
END;

-- =====================================================================================
-- TABELA: HORAS_EXTRAS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='horas_extras' AND xtype='U')
BEGIN
    CREATE TABLE horas_extras (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        usuario_id BIGINT NOT NULL,
        data DATE NOT NULL,
        horas DECIMAL(4,2) NOT NULL,
        status NVARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA')),
        descricao NVARCHAR(500) NOT NULL,
        justificativa NVARCHAR(1000),
        motivo_recusa NVARCHAR(500),
        
        -- Auditoria
        aprovado_por_usuario_id BIGINT,
        data_aprovacao DATETIME2,
        data_solicitacao DATETIME2 DEFAULT GETDATE(),
        data_criacao DATETIME2 DEFAULT GETDATE(),
        data_atualizacao DATETIME2 DEFAULT GETDATE(),
        
        -- Relacionamentos
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
        FOREIGN KEY (aprovado_por_usuario_id) REFERENCES usuarios(id),
        
        -- Índices
        INDEX IX_horas_extras_usuario (usuario_id),
        INDEX IX_horas_extras_status (status),
        INDEX IX_horas_extras_data (data),
        
        -- Constraints
        CHECK (horas > 0 AND horas <= 12) -- máximo 12h extras por dia
    );
    PRINT 'Tabela horas_extras criada com sucesso!';
END;

-- =====================================================================================
-- TABELA: COMPROVANTES
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='comprovantes' AND xtype='U')
BEGIN
    CREATE TABLE comprovantes (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        usuario_id BIGINT NOT NULL,
        tipo_comprovante NVARCHAR(20) NOT NULL CHECK (tipo_comprovante IN ('SALARIO', 'FERIAS', 'RESCISAO', 'ADIANTAMENTO')),
        referencia NVARCHAR(50) NOT NULL, -- ano/mês ou período
        data_emissao DATE NOT NULL DEFAULT GETDATE(),
        
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
        arquivo_nome NVARCHAR(255),
        arquivo_caminho NVARCHAR(500),
        arquivo_tamanho BIGINT,
        
        -- Auditoria
        gerado_por_usuario_id BIGINT,
        data_criacao DATETIME2 DEFAULT GETDATE(),
        data_atualizacao DATETIME2 DEFAULT GETDATE(),
        
        -- Relacionamentos
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
        FOREIGN KEY (gerado_por_usuario_id) REFERENCES usuarios(id),
        
        -- Índices
        INDEX IX_comprovantes_usuario (usuario_id),
        INDEX IX_comprovantes_tipo (tipo_comprovante),
        INDEX IX_comprovantes_referencia (referencia),
        INDEX IX_comprovantes_emissao (data_emissao)
    );
    PRINT 'Tabela comprovantes criada com sucesso!';
END;

-- =====================================================================================
-- TABELA: CONFIGURACOES_SISTEMA
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='configuracoes_sistema' AND xtype='U')
BEGIN
    CREATE TABLE configuracoes_sistema (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        chave NVARCHAR(100) NOT NULL UNIQUE,
        valor NVARCHAR(1000) NOT NULL,
        descricao NVARCHAR(500),
        tipo NVARCHAR(20) NOT NULL DEFAULT 'STRING' CHECK (tipo IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
        categoria NVARCHAR(50) NOT NULL DEFAULT 'GERAL',
        data_criacao DATETIME2 DEFAULT GETDATE(),
        data_atualizacao DATETIME2 DEFAULT GETDATE(),
        
        -- Índices
        INDEX IX_configuracoes_chave (chave),
        INDEX IX_configuracoes_categoria (categoria)
    );
    PRINT 'Tabela configuracoes_sistema criada com sucesso!';
END;

-- =====================================================================================
-- TABELA: NOTIFICACOES
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='notificacoes' AND xtype='U')
BEGIN
    CREATE TABLE notificacoes (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        usuario_id BIGINT NOT NULL,
        titulo NVARCHAR(100) NOT NULL,
        mensagem NVARCHAR(500) NOT NULL,
        tipo NVARCHAR(20) NOT NULL DEFAULT 'INFO' CHECK (tipo IN ('INFO', 'SUCESSO', 'AVISO', 'ERRO')),
        lida BIT DEFAULT 0,
        url_acao NVARCHAR(200), -- URL para ação relacionada
        data_criacao DATETIME2 DEFAULT GETDATE(),
        data_leitura DATETIME2,
        
        -- Relacionamentos
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
        
        -- Índices
        INDEX IX_notificacoes_usuario (usuario_id),
        INDEX IX_notificacoes_lida (lida),
        INDEX IX_notificacoes_data (data_criacao)
    );
    PRINT 'Tabela notificacoes criada com sucesso!';
END;

-- =====================================================================================
-- TABELA: LOG_AUDITORIA
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='log_auditoria' AND xtype='U')
BEGIN
    CREATE TABLE log_auditoria (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        usuario_id BIGINT,
        acao NVARCHAR(100) NOT NULL,
        tabela_afetada NVARCHAR(50),
        registro_id BIGINT,
        dados_anteriores NVARCHAR(MAX), -- JSON
        dados_novos NVARCHAR(MAX), -- JSON
        ip_origem NVARCHAR(45),
        user_agent NVARCHAR(500),
        data_criacao DATETIME2 DEFAULT GETDATE(),
        
        -- Relacionamentos
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
        
        -- Índices
        INDEX IX_auditoria_usuario (usuario_id),
        INDEX IX_auditoria_acao (acao),
        INDEX IX_auditoria_tabela (tabela_afetada),
        INDEX IX_auditoria_data (data_criacao)
    );
    PRINT 'Tabela log_auditoria criada com sucesso!';
END;

-- =====================================================================================
-- INSERIR CONFIGURAÇÕES PADRÃO DO SISTEMA
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM configuracoes_sistema WHERE chave = 'HORAS_TRABALHO_DIA')
BEGIN
    INSERT INTO configuracoes_sistema (chave, valor, descricao, tipo, categoria) VALUES
    ('HORAS_TRABALHO_DIA', '8.0', 'Horas de trabalho por dia', 'NUMBER', 'JORNADA'),
    ('HORAS_TRABALHO_SEMANA', '40.0', 'Horas de trabalho por semana', 'NUMBER', 'JORNADA'),
    ('TOLERANCIA_ATRASO_MINUTOS', '15', 'Tolerância para atraso em minutos', 'NUMBER', 'JORNADA'),
    ('MAX_HORAS_EXTRAS_DIA', '4.0', 'Máximo de horas extras por dia', 'NUMBER', 'HORAS_EXTRAS'),
    ('MAX_HORAS_EXTRAS_MES', '60.0', 'Máximo de horas extras por mês', 'NUMBER', 'HORAS_EXTRAS'),
    ('PERCENTUAL_HORA_EXTRA', '50', 'Percentual adicional para hora extra (%)', 'NUMBER', 'HORAS_EXTRAS'),
    ('DIAS_FERIAS_ANO', '30', 'Dias de férias por ano', 'NUMBER', 'FERIAS'),
    ('ANTECEDENCIA_FERIAS_DIAS', '30', 'Antecedência mínima para solicitar férias (dias)', 'NUMBER', 'FERIAS'),
    ('RAIO_MAXIMO_PONTO_METROS', '100', 'Raio máximo para registrar ponto (metros)', 'NUMBER', 'LOCALIZACAO'),
    ('PRECISAO_FACE_MINIMA', '0.85', 'Precisão mínima para reconhecimento facial', 'NUMBER', 'BIOMETRIA'),
    ('BACKUP_AUTOMATICO', 'true', 'Ativar backup automático', 'BOOLEAN', 'SISTEMA'),
    ('SINCRONIZACAO_ATIVA', 'true', 'Ativar sincronização automática', 'BOOLEAN', 'SISTEMA'),
    ('TEMA_INTERFACE', 'default', 'Tema da interface', 'STRING', 'INTERFACE'),
    ('EMAIL_NOTIFICACOES', 'true', 'Enviar notificações por email', 'BOOLEAN', 'NOTIFICACOES');
    
    PRINT 'Configurações padrão inseridas com sucesso!';
END;

-- =====================================================================================
-- INSERIR USUÁRIOS PADRÃO DO SISTEMA
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'admin@shiftly.com')
BEGIN
    INSERT INTO usuarios (nome, email, senha, tipo_usuario, cargo, departamento, ativo) VALUES
    ('Administrador do Sistema', 'admin@shiftly.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'ADMIN', 'Administrador', 'TI', 1),
    ('Maria Silva', 'maria.silva@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'RH', 'Analista de RH', 'Recursos Humanos', 1),
    ('João Santos', 'joao.santos@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Desenvolvedor', 'TI', 1),
    ('Ana Oliveira', 'ana.oliveira@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Designer', 'Marketing', 1),
    ('Pedro Costa', 'pedro.costa@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Analista', 'Vendas', 1),
    ('Carla Ferreira', 'carla.ferreira@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Assistente', 'Financeiro', 1);
    
    PRINT 'Usuários padrão inseridos com sucesso!';
END;

-- =====================================================================================
-- INSERIR DADOS DE EXEMPLO PARA DEMONSTRAÇÃO
-- =====================================================================================

-- Pontos de exemplo
IF NOT EXISTS (SELECT * FROM pontos WHERE usuario_id = 3)
BEGIN
    INSERT INTO pontos (usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, face_validada) VALUES
    (3, DATEADD(HOUR, -2, GETDATE()), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, São Paulo', 1),
    (4, DATEADD(HOUR, -1, GETDATE()), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, São Paulo', 1),
    (5, DATEADD(MINUTE, -30, GETDATE()), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, São Paulo', 1);
    
    PRINT 'Pontos de exemplo inseridos com sucesso!';
END;

-- Solicitações de férias de exemplo
IF NOT EXISTS (SELECT * FROM ferias WHERE usuario_id = 3)
BEGIN
    INSERT INTO ferias (usuario_id, data_inicio, data_fim, dias_solicitados, status, observacoes) VALUES
    (3, DATEADD(DAY, 30, GETDATE()), DATEADD(DAY, 44, GETDATE()), 15, 'PENDENTE', 'Férias de final de ano'),
    (4, DATEADD(DAY, 60, GETDATE()), DATEADD(DAY, 69, GETDATE()), 10, 'PENDENTE', 'Viagem em família');
    
    PRINT 'Solicitações de férias de exemplo inseridas com sucesso!';
END;

-- Horas extras de exemplo
IF NOT EXISTS (SELECT * FROM horas_extras WHERE usuario_id = 3)
BEGIN
    INSERT INTO horas_extras (usuario_id, data, horas, status, descricao) VALUES
    (3, CAST(GETDATE() AS DATE), 2.5, 'APROVADA', 'Finalização de projeto urgente'),
    (4, CAST(DATEADD(DAY, -1, GETDATE()) AS DATE), 1.5, 'PENDENTE', 'Correção de bugs');
    
    PRINT 'Horas extras de exemplo inseridas com sucesso!';
END;

-- =====================================================================================
-- VIEWS PARA RELATÓRIOS
-- =====================================================================================

-- View: Resumo de pontos por usuário
IF NOT EXISTS (SELECT * FROM sys.views WHERE name = 'vw_resumo_pontos_usuario')
BEGIN
    EXEC('CREATE VIEW vw_resumo_pontos_usuario AS
    SELECT 
        u.id,
        u.nome,
        u.email,
        u.departamento,
        COUNT(p.id) as total_pontos,
        COUNT(CASE WHEN p.tipo_ponto = ''ENTRADA'' THEN 1 END) as total_entradas,
        COUNT(CASE WHEN p.tipo_ponto = ''SAIDA'' THEN 1 END) as total_saidas,
        COUNT(CASE WHEN p.face_validada = 1 THEN 1 END) as pontos_validados,
        MAX(p.data_hora) as ultimo_ponto
    FROM usuarios u
    LEFT JOIN pontos p ON u.id = p.usuario_id
    WHERE u.ativo = 1
    GROUP BY u.id, u.nome, u.email, u.departamento');
    
    PRINT 'View vw_resumo_pontos_usuario criada com sucesso!';
END;

-- View: Horas trabalhadas por dia
IF NOT EXISTS (SELECT * FROM sys.views WHERE name = 'vw_horas_trabalhadas_dia')
BEGIN
    EXEC('CREATE VIEW vw_horas_trabalhadas_dia AS
    WITH PontosPorDia AS (
        SELECT 
            usuario_id,
            CAST(data_hora AS DATE) as data,
            MIN(CASE WHEN tipo_ponto = ''ENTRADA'' THEN data_hora END) as primeira_entrada,
            MAX(CASE WHEN tipo_ponto = ''SAIDA'' THEN data_hora END) as ultima_saida
        FROM pontos
        GROUP BY usuario_id, CAST(data_hora AS DATE)
    )
    SELECT 
        u.id,
        u.nome,
        ppd.data,
        ppd.primeira_entrada,
        ppd.ultima_saida,
        CASE 
            WHEN ppd.primeira_entrada IS NOT NULL AND ppd.ultima_saida IS NOT NULL
            THEN DATEDIFF(MINUTE, ppd.primeira_entrada, ppd.ultima_saida) / 60.0
            ELSE 0
        END as horas_trabalhadas
    FROM usuarios u
    INNER JOIN PontosPorDia ppd ON u.id = ppd.usuario_id
    WHERE u.ativo = 1');
    
    PRINT 'View vw_horas_trabalhadas_dia criada com sucesso!';
END;

-- =====================================================================================
-- PROCEDURES PARA OPERAÇÕES COMUNS
-- =====================================================================================

-- Procedure: Calcular saldo de férias
IF NOT EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_calcular_saldo_ferias')
BEGIN
    EXEC('CREATE PROCEDURE sp_calcular_saldo_ferias
        @usuario_id BIGINT,
        @saldo_disponivel INT OUTPUT
    AS
    BEGIN
        DECLARE @dias_direito INT = 30; -- Padrão de 30 dias por ano
        DECLARE @dias_usados INT = 0;
        
        -- Calcula dias já utilizados no ano atual
        SELECT @dias_usados = ISNULL(SUM(dias_solicitados), 0)
        FROM ferias 
        WHERE usuario_id = @usuario_id 
        AND status = ''APROVADA''
        AND YEAR(data_inicio) = YEAR(GETDATE());
        
        SET @saldo_disponivel = @dias_direito - @dias_usados;
        
        -- Garante que não seja negativo
        IF @saldo_disponivel < 0
            SET @saldo_disponivel = 0;
    END');
    
    PRINT 'Procedure sp_calcular_saldo_ferias criada com sucesso!';
END;

-- =====================================================================================
-- TRIGGERS PARA AUDITORIA
-- =====================================================================================

-- Trigger: Auditoria na tabela usuarios
IF NOT EXISTS (SELECT * FROM sys.triggers WHERE name = 'tr_usuarios_auditoria')
BEGIN
    EXEC('CREATE TRIGGER tr_usuarios_auditoria
    ON usuarios
    AFTER INSERT, UPDATE, DELETE
    AS
    BEGIN
        SET NOCOUNT ON;
        
        IF EXISTS(SELECT * FROM inserted) AND EXISTS(SELECT * FROM deleted)
        BEGIN
            -- UPDATE
            INSERT INTO log_auditoria (usuario_id, acao, tabela_afetada, registro_id, dados_anteriores, dados_novos)
            SELECT 
                i.id,
                ''UPDATE'',
                ''usuarios'',
                i.id,
                (SELECT * FROM deleted d WHERE d.id = i.id FOR JSON AUTO),
                (SELECT * FROM inserted i2 WHERE i2.id = i.id FOR JSON AUTO)
            FROM inserted i;
        END
        ELSE IF EXISTS(SELECT * FROM inserted)
        BEGIN
            -- INSERT
            INSERT INTO log_auditoria (usuario_id, acao, tabela_afetada, registro_id, dados_novos)
            SELECT id, ''INSERT'', ''usuarios'', id, (SELECT * FROM inserted i2 WHERE i2.id = inserted.id FOR JSON AUTO)
            FROM inserted;
        END
        ELSE IF EXISTS(SELECT * FROM deleted)
        BEGIN
            -- DELETE
            INSERT INTO log_auditoria (usuario_id, acao, tabela_afetada, registro_id, dados_anteriores)
            SELECT id, ''DELETE'', ''usuarios'', id, (SELECT * FROM deleted d2 WHERE d2.id = deleted.id FOR JSON AUTO)
            FROM deleted;
        END
    END');
    
    PRINT 'Trigger tr_usuarios_auditoria criado com sucesso!';
END;

-- =====================================================================================
-- INDEXES ADICIONAIS PARA PERFORMANCE
-- =====================================================================================

-- Índices compostos para consultas frequentes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_pontos_usuario_data_tipo')
BEGIN
    CREATE INDEX IX_pontos_usuario_data_tipo ON pontos (usuario_id, data_hora, tipo_ponto);
    PRINT 'Índice IX_pontos_usuario_data_tipo criado com sucesso!';
END;

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_ferias_usuario_status_data')
BEGIN
    CREATE INDEX IX_ferias_usuario_status_data ON ferias (usuario_id, status, data_inicio);
    PRINT 'Índice IX_ferias_usuario_status_data criado com sucesso!';
END;

-- =====================================================================================
-- FUNCÕES AUXILIARES
-- =====================================================================================

-- Função: Calcular idade
IF NOT EXISTS (SELECT * FROM sys.objects WHERE name = 'fn_calcular_idade' AND type = 'FN')
BEGIN
    EXEC('CREATE FUNCTION fn_calcular_idade(@data_nascimento DATE)
    RETURNS INT
    AS
    BEGIN
        DECLARE @idade INT;
        SET @idade = DATEDIFF(YEAR, @data_nascimento, GETDATE()) -
                    CASE 
                        WHEN MONTH(@data_nascimento) > MONTH(GETDATE()) OR 
                             (MONTH(@data_nascimento) = MONTH(GETDATE()) AND DAY(@data_nascimento) > DAY(GETDATE()))
                        THEN 1 
                        ELSE 0 
                    END;
        RETURN @idade;
    END');
    
    PRINT 'Função fn_calcular_idade criada com sucesso!';
END;

-- =====================================================================================
-- FINALIZAÇÃO
-- =====================================================================================

PRINT '=============================================================================';
PRINT 'SCRIPT COMPLETO DO BANCO DE DADOS SHIFTLY EXECUTADO COM SUCESSO!';
PRINT '=============================================================================';
PRINT 'Tabelas criadas: usuarios, pontos, ferias, horas_extras, comprovantes,';
PRINT '                configuracoes_sistema, notificacoes, log_auditoria';
PRINT 'Views criadas: vw_resumo_pontos_usuario, vw_horas_trabalhadas_dia';
PRINT 'Procedures criadas: sp_calcular_saldo_ferias';
PRINT 'Triggers criados: tr_usuarios_auditoria';
PRINT 'Dados de exemplo inseridos para demonstração';
PRINT '=============================================================================';
PRINT 'SISTEMA PRONTO PARA USO!';
PRINT '=============================================================================';
