-- =====================================================================================
-- SCRIPT SIMPLIFICADO DO BANCO DE DADOS SHIFTLY PARA SQL SERVER
-- Sistema de Controle de Ponto Eletrônico com Reconhecimento Facial e Geolocalização
-- =====================================================================================

-- =====================================================================================
-- TABELA: USUARIOS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[usuarios]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[usuarios](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [nome] [varchar](100) NOT NULL,
        [email] [varchar](100) NOT NULL,
        [senha] [varchar](255) NOT NULL,
        [tipo_usuario] [varchar](20) NOT NULL,
        [cargo] [varchar](50) NULL,
        [departamento] [varchar](50) NULL,
        [salario] [decimal](10,2) NULL,
        [data_admissao] [datetime] NULL,
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_atualizacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [ativo] [bit] NOT NULL DEFAULT 1,
        [face_encoding] [nvarchar](max) NULL,
        [telefone] [varchar](20) NULL,
        [endereco] [varchar](200) NULL,
        [cep] [varchar](10) NULL,
        [data_nascimento] [date] NULL,
        [cpf] [varchar](14) NULL,
        [rg] [varchar](20) NULL,
        [foto_perfil] [varchar](255) NULL,
        CONSTRAINT [PK_usuarios] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    -- Constraint para tipo_usuario
    ALTER TABLE [dbo].[usuarios] ADD CONSTRAINT [CK_usuarios_tipo] CHECK ([tipo_usuario] IN ('COLABORADOR', 'RH', 'ADMIN'))
    
    PRINT 'Tabela usuarios criada com sucesso!'
END

-- =====================================================================================
-- TABELA: PONTOS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[pontos]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[pontos](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [usuario_id] [bigint] NOT NULL,
        [data_hora] [datetime] NOT NULL DEFAULT GETDATE(),
        [tipo_ponto] [varchar](10) NOT NULL,
        [latitude] [decimal](10, 8) NULL,
        [longitude] [decimal](11, 8) NULL,
        [endereco] [varchar](200) NULL,
        [precisao] [decimal](8, 2) NULL,
        [face_match] [varchar](10) NULL,
        [face_validada] [bit] NOT NULL DEFAULT 0,
        [face_data] [nvarchar](max) NULL,
        [observacoes] [varchar](500) NULL,
        [manual] [bit] NOT NULL DEFAULT 0,
        [corrigido_por_usuario_id] [bigint] NULL,
        [data_correcao] [datetime] NULL,
        [motivo_correcao] [varchar](500) NULL,
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [PK_pontos] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    -- Constraint para tipo_ponto
    ALTER TABLE [dbo].[pontos] ADD CONSTRAINT [CK_pontos_tipo] CHECK ([tipo_ponto] IN ('ENTRADA', 'SAIDA'))
    
    PRINT 'Tabela pontos criada com sucesso!'
END

-- =====================================================================================
-- TABELA: FERIAS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ferias]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[ferias](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [usuario_id] [bigint] NOT NULL,
        [data_inicio] [date] NOT NULL,
        [data_fim] [date] NOT NULL,
        [dias_solicitados] [int] NOT NULL,
        [status] [varchar](20) NOT NULL DEFAULT 'PENDENTE',
        [observacoes] [varchar](1000) NULL,
        [motivo_recusa] [varchar](500) NULL,
        [aprovado_por_usuario_id] [bigint] NULL,
        [data_aprovacao] [datetime] NULL,
        [data_solicitacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_atualizacao] [datetime] NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [PK_ferias] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    -- Constraint para status
    ALTER TABLE [dbo].[ferias] ADD CONSTRAINT [CK_ferias_status] CHECK ([status] IN ('PENDENTE', 'APROVADA', 'REJEITADA', 'CANCELADA'))
    
    PRINT 'Tabela ferias criada com sucesso!'
END

-- =====================================================================================
-- TABELA: HORAS_EXTRAS
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[horas_extras]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[horas_extras](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [usuario_id] [bigint] NOT NULL,
        [data] [date] NOT NULL,
        [horas] [decimal](4,2) NOT NULL,
        [status] [varchar](20) NOT NULL DEFAULT 'PENDENTE',
        [descricao] [varchar](500) NOT NULL,
        [justificativa] [varchar](1000) NULL,
        [motivo_recusa] [varchar](500) NULL,
        [aprovado_por_usuario_id] [bigint] NULL,
        [data_aprovacao] [datetime] NULL,
        [data_solicitacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_atualizacao] [datetime] NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [PK_horas_extras] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    -- Constraint para status
    ALTER TABLE [dbo].[horas_extras] ADD CONSTRAINT [CK_horas_extras_status] CHECK ([status] IN ('PENDENTE', 'APROVADA', 'REJEITADA'))
    
    PRINT 'Tabela horas_extras criada com sucesso!'
END

-- =====================================================================================
-- TABELA: COMPROVANTES
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[comprovantes]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[comprovantes](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [usuario_id] [bigint] NOT NULL,
        [tipo_comprovante] [varchar](20) NOT NULL,
        [referencia] [varchar](50) NOT NULL,
        [data_emissao] [date] NOT NULL DEFAULT GETDATE(),
        [periodo_inicio] [date] NOT NULL,
        [periodo_fim] [date] NOT NULL,
        [valor_bruto] [decimal](10,2) NOT NULL,
        [valor_descontos] [decimal](10,2) NOT NULL DEFAULT 0,
        [valor_liquido] [decimal](10,2) NOT NULL,
        [salario_base] [decimal](10,2) NULL,
        [horas_extras] [decimal](10,2) NOT NULL DEFAULT 0,
        [adicional_noturno] [decimal](10,2) NOT NULL DEFAULT 0,
        [vale_transporte] [decimal](10,2) NOT NULL DEFAULT 0,
        [vale_refeicao] [decimal](10,2) NOT NULL DEFAULT 0,
        [plano_saude] [decimal](10,2) NOT NULL DEFAULT 0,
        [inss] [decimal](10,2) NOT NULL DEFAULT 0,
        [irrf] [decimal](10,2) NOT NULL DEFAULT 0,
        [fgts] [decimal](10,2) NOT NULL DEFAULT 0,
        [arquivo_nome] [varchar](255) NULL,
        [arquivo_caminho] [varchar](500) NULL,
        [arquivo_tamanho] [bigint] NULL,
        [gerado_por_usuario_id] [bigint] NULL,
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_atualizacao] [datetime] NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [PK_comprovantes] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    -- Constraint para tipo_comprovante
    ALTER TABLE [dbo].[comprovantes] ADD CONSTRAINT [CK_comprovantes_tipo] CHECK ([tipo_comprovante] IN ('SALARIO', 'FERIAS', 'RESCISAO', 'ADIANTAMENTO'))
    
    PRINT 'Tabela comprovantes criada com sucesso!'
END

-- =====================================================================================
-- TABELA: CONFIGURACOES_SISTEMA
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[configuracoes_sistema]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[configuracoes_sistema](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [chave] [varchar](100) NOT NULL,
        [valor] [varchar](1000) NOT NULL,
        [descricao] [varchar](500) NULL,
        [tipo] [varchar](20) NOT NULL DEFAULT 'STRING',
        [categoria] [varchar](50) NOT NULL DEFAULT 'GERAL',
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_atualizacao] [datetime] NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [PK_configuracoes_sistema] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    -- Constraint para tipo
    ALTER TABLE [dbo].[configuracoes_sistema] ADD CONSTRAINT [CK_configuracoes_tipo] CHECK ([tipo] IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON'))
    
    PRINT 'Tabela configuracoes_sistema criada com sucesso!'
END

-- =====================================================================================
-- TABELA: NOTIFICACOES
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[notificacoes]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[notificacoes](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [usuario_id] [bigint] NOT NULL,
        [titulo] [varchar](100) NOT NULL,
        [mensagem] [varchar](500) NOT NULL,
        [tipo] [varchar](20) NOT NULL DEFAULT 'INFO',
        [lida] [bit] NOT NULL DEFAULT 0,
        [url_acao] [varchar](200) NULL,
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        [data_leitura] [datetime] NULL,
        CONSTRAINT [PK_notificacoes] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    -- Constraint para tipo
    ALTER TABLE [dbo].[notificacoes] ADD CONSTRAINT [CK_notificacoes_tipo] CHECK ([tipo] IN ('INFO', 'SUCESSO', 'AVISO', 'ERRO'))
    
    PRINT 'Tabela notificacoes criada com sucesso!'
END

-- =====================================================================================
-- TABELA: LOG_AUDITORIA
-- =====================================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[log_auditoria]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[log_auditoria](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [usuario_id] [bigint] NULL,
        [acao] [varchar](100) NOT NULL,
        [tabela_afetada] [varchar](50) NULL,
        [registro_id] [bigint] NULL,
        [dados_anteriores] [nvarchar](max) NULL,
        [dados_novos] [nvarchar](max) NULL,
        [ip_origem] [varchar](45) NULL,
        [user_agent] [varchar](500) NULL,
        [data_criacao] [datetime] NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [PK_log_auditoria] PRIMARY KEY CLUSTERED ([id] ASC)
    )
    
    PRINT 'Tabela log_auditoria criada com sucesso!'
END

-- =====================================================================================
-- CHAVES ESTRANGEIRAS
-- =====================================================================================

-- FK pontos -> usuarios
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_pontos_usuarios]') AND parent_object_id = OBJECT_ID(N'[dbo].[pontos]'))
BEGIN
    ALTER TABLE [dbo].[pontos] ADD CONSTRAINT [FK_pontos_usuarios] FOREIGN KEY([usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK pontos -> usuarios criada!'
END

-- FK pontos -> usuarios (corrigido_por)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_pontos_usuarios_corrigido]') AND parent_object_id = OBJECT_ID(N'[dbo].[pontos]'))
BEGIN
    ALTER TABLE [dbo].[pontos] ADD CONSTRAINT [FK_pontos_usuarios_corrigido] FOREIGN KEY([corrigido_por_usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK pontos -> usuarios (corrigido_por) criada!'
END

-- FK ferias -> usuarios
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_ferias_usuarios]') AND parent_object_id = OBJECT_ID(N'[dbo].[ferias]'))
BEGIN
    ALTER TABLE [dbo].[ferias] ADD CONSTRAINT [FK_ferias_usuarios] FOREIGN KEY([usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK ferias -> usuarios criada!'
END

-- FK ferias -> usuarios (aprovado_por)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_ferias_usuarios_aprovado]') AND parent_object_id = OBJECT_ID(N'[dbo].[ferias]'))
BEGIN
    ALTER TABLE [dbo].[ferias] ADD CONSTRAINT [FK_ferias_usuarios_aprovado] FOREIGN KEY([aprovado_por_usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK ferias -> usuarios (aprovado_por) criada!'
END

-- FK horas_extras -> usuarios
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_horas_extras_usuarios]') AND parent_object_id = OBJECT_ID(N'[dbo].[horas_extras]'))
BEGIN
    ALTER TABLE [dbo].[horas_extras] ADD CONSTRAINT [FK_horas_extras_usuarios] FOREIGN KEY([usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK horas_extras -> usuarios criada!'
END

-- FK horas_extras -> usuarios (aprovado_por)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_horas_extras_usuarios_aprovado]') AND parent_object_id = OBJECT_ID(N'[dbo].[horas_extras]'))
BEGIN
    ALTER TABLE [dbo].[horas_extras] ADD CONSTRAINT [FK_horas_extras_usuarios_aprovado] FOREIGN KEY([aprovado_por_usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK horas_extras -> usuarios (aprovado_por) criada!'
END

-- FK comprovantes -> usuarios
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_comprovantes_usuarios]') AND parent_object_id = OBJECT_ID(N'[dbo].[comprovantes]'))
BEGIN
    ALTER TABLE [dbo].[comprovantes] ADD CONSTRAINT [FK_comprovantes_usuarios] FOREIGN KEY([usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK comprovantes -> usuarios criada!'
END

-- FK comprovantes -> usuarios (gerado_por)
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_comprovantes_usuarios_gerado]') AND parent_object_id = OBJECT_ID(N'[dbo].[comprovantes]'))
BEGIN
    ALTER TABLE [dbo].[comprovantes] ADD CONSTRAINT [FK_comprovantes_usuarios_gerado] FOREIGN KEY([gerado_por_usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK comprovantes -> usuarios (gerado_por) criada!'
END

-- FK notificacoes -> usuarios
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_notificacoes_usuarios]') AND parent_object_id = OBJECT_ID(N'[dbo].[notificacoes]'))
BEGIN
    ALTER TABLE [dbo].[notificacoes] ADD CONSTRAINT [FK_notificacoes_usuarios] FOREIGN KEY([usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK notificacoes -> usuarios criada!'
END

-- FK log_auditoria -> usuarios
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'[dbo].[FK_log_auditoria_usuarios]') AND parent_object_id = OBJECT_ID(N'[dbo].[log_auditoria]'))
BEGIN
    ALTER TABLE [dbo].[log_auditoria] ADD CONSTRAINT [FK_log_auditoria_usuarios] FOREIGN KEY([usuario_id]) REFERENCES [dbo].[usuarios] ([id])
    PRINT 'FK log_auditoria -> usuarios criada!'
END

-- =====================================================================================
-- INSERIR DADOS INICIAIS
-- =====================================================================================

-- Usuários padrão
IF NOT EXISTS (SELECT * FROM usuarios WHERE email = 'admin@shiftly.com')
BEGIN
    INSERT INTO usuarios (nome, email, senha, tipo_usuario, cargo, departamento, ativo) VALUES
    ('Administrador do Sistema', 'admin@shiftly.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'ADMIN', 'Administrador', 'TI', 1),
    ('Maria Silva', 'maria.silva@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'RH', 'Analista de RH', 'Recursos Humanos', 1),
    ('Joao Santos', 'joao.santos@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Desenvolvedor', 'TI', 1),
    ('Ana Oliveira', 'ana.oliveira@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Designer', 'Marketing', 1),
    ('Pedro Costa', 'pedro.costa@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Analista', 'Vendas', 1),
    ('Carla Ferreira', 'carla.ferreira@empresa.com', '$2a$10$N.zmdr9k7uOCQb0OgleLW.4iIWe2YZ8CfHFBXZwG6Mx.SuRfxfWwK', 'COLABORADOR', 'Assistente', 'Financeiro', 1)
    
    PRINT 'Usuários padrão inseridos com sucesso!'
END

-- Configurações do sistema
IF NOT EXISTS (SELECT * FROM configuracoes_sistema WHERE chave = 'HORAS_TRABALHO_DIA')
BEGIN
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
    ('EMAIL_NOTIFICACOES', 'true', 'Enviar notificacoes por email', 'BOOLEAN', 'NOTIFICACOES')
    
    PRINT 'Configurações do sistema inseridas com sucesso!'
END

-- Dados de exemplo
IF NOT EXISTS (SELECT * FROM pontos WHERE usuario_id = 3)
BEGIN
    INSERT INTO pontos (usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, face_validada) VALUES
    (3, DATEADD(HOUR, -2, GETDATE()), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', 1),
    (4, DATEADD(HOUR, -1, GETDATE()), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', 1),
    (5, DATEADD(MINUTE, -30, GETDATE()), 'ENTRADA', -23.550520, -46.633309, 'Av. Paulista, 1000, Sao Paulo', 1)
    
    PRINT 'Dados de exemplo inseridos com sucesso!'
END

PRINT '=== BANCO DE DADOS SHIFTLY INICIALIZADO COM SUCESSO! ==='
