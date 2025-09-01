package com.shiftly.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsável pela sincronização de dados entre H2 e SQL Server
 */
public class DatabaseSynchronizer {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSynchronizer.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean synchronizationRunning = false;
    
    /**
     * Inicia o processo de sincronização automática
     */
    public static void startAutoSync() {
        // Verifica a cada 30 segundos se o SQL Server voltou online
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (DatabaseConfig.isUsingH2() && DatabaseConfig.isSqlServerAvailable()) {
                    logger.info("SQL Server detectado online. Iniciando sincronização...");
                    syncH2ToSqlServer();
                }
            } catch (Exception e) {
                logger.error("Erro na verificação de sincronização automática: {}", e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
        
        logger.info("Sincronização automática iniciada (verificação a cada 30 segundos)");
    }
    
    /**
     * Para o processo de sincronização automática
     */
    public static void stopAutoSync() {
        scheduler.shutdown();
        logger.info("Sincronização automática interrompida");
    }
    
    /**
     * Sincroniza dados do H2 para o SQL Server de forma assíncrona
     */
    public static CompletableFuture<Boolean> syncH2ToSqlServerAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return syncH2ToSqlServer();
            } catch (Exception e) {
                logger.error("Erro na sincronização assíncrona: {}", e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Sincroniza dados do H2 para o SQL Server
     */
    public static boolean syncH2ToSqlServer() {
        if (synchronizationRunning) {
            logger.warn("Sincronização já está em execução");
            return false;
        }
        
        synchronizationRunning = true;
        logger.info("=== Iniciando sincronização H2 → SQL Server ===");
        
        try {
            // Verifica se SQL Server está disponível
            if (!DatabaseConfig.isSqlServerAvailable()) {
                logger.warn("SQL Server não está disponível para sincronização");
                return false;
            }
            
            // Sincroniza cada tabela
            boolean success = true;
            success &= syncTable("usuarios");
            success &= syncTable("pontos");
            success &= syncTable("ferias");
            success &= syncTable("horas_extras");
            success &= syncTable("comprovantes");
            
            if (success) {
                // Troca para SQL Server como banco principal
                DatabaseConfig.setCurrentDatabaseType(DatabaseConfig.DatabaseType.SQL_SERVER);
                logger.info("✓ Sincronização concluída com sucesso. Mudando para SQL Server.");
            } else {
                logger.error("✗ Sincronização concluída com erros");
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Erro geral na sincronização: {}", e.getMessage(), e);
            return false;
        } finally {
            synchronizationRunning = false;
            logger.info("=== Fim da sincronização ===");
        }
    }
    
    /**
     * Sincroniza uma tabela específica do H2 para o SQL Server
     */
    private static boolean syncTable(String tableName) {
        logger.info("Sincronizando tabela: {}", tableName);
        
        try (Connection h2Conn = DatabaseConfig.getH2Connection();
             Connection sqlConn = DatabaseConfig.getSqlServerConnection()) {
            
            // Conta registros no H2
            int h2Count = getRecordCount(h2Conn, tableName);
            if (h2Count == 0) {
                logger.info("Tabela {} está vazia no H2, pulando sincronização", tableName);
                return true;
            }
            
            // Conta registros no SQL Server
            int sqlCount = getRecordCount(sqlConn, tableName);
            
            logger.info("Tabela {}: H2={} registros, SQL Server={} registros", tableName, h2Count, sqlCount);
            
            // Se SQL Server tem mais ou igual registros, não sincroniza
            if (sqlCount >= h2Count) {
                logger.info("SQL Server já possui dados atualizados para tabela {}", tableName);
                return true;
            }
            
            // Executa a sincronização baseada na tabela
            switch (tableName.toLowerCase()) {
                case "usuarios":
                    return syncUsuarios(h2Conn, sqlConn);
                case "pontos":
                    return syncPontos(h2Conn, sqlConn);
                case "ferias":
                    return syncFerias(h2Conn, sqlConn);
                case "horas_extras":
                    return syncHorasExtras(h2Conn, sqlConn);
                case "comprovantes":
                    return syncComprovantes(h2Conn, sqlConn);
                default:
                    logger.warn("Tabela {} não reconhecida para sincronização", tableName);
                    return false;
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao sincronizar tabela {}: {}", tableName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Conta registros em uma tabela
     */
    private static int getRecordCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Sincroniza tabela de usuários
     */
    private static boolean syncUsuarios(Connection h2Conn, Connection sqlConn) throws SQLException {
        String selectSql = """
            SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                   salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
            FROM usuarios
            """;
        
        String insertSql = """
            INSERT INTO usuarios (nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                                salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return syncTableData(h2Conn, sqlConn, selectSql, insertSql, "usuarios");
    }
    
    /**
     * Sincroniza tabela de pontos
     */
    private static boolean syncPontos(Connection h2Conn, Connection sqlConn) throws SQLException {
        String selectSql = """
            SELECT usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                   face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                   data_correcao, motivo_correcao, data_criacao
            FROM pontos
            """;
        
        String insertSql = """
            INSERT INTO pontos (usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                              face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                              data_correcao, motivo_correcao, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return syncTableData(h2Conn, sqlConn, selectSql, insertSql, "pontos");
    }
    
    /**
     * Sincroniza tabela de férias
     */
    private static boolean syncFerias(Connection h2Conn, Connection sqlConn) throws SQLException {
        String selectSql = """
            SELECT usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                   aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
            FROM ferias
            """;
        
        String insertSql = """
            INSERT INTO ferias (usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                              aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return syncTableData(h2Conn, sqlConn, selectSql, insertSql, "ferias");
    }
    
    /**
     * Sincroniza tabela de horas extras
     */
    private static boolean syncHorasExtras(Connection h2Conn, Connection sqlConn) throws SQLException {
        String selectSql = """
            SELECT usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                   aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                   data_solicitacao, data_atualizacao
            FROM horas_extras
            """;
        
        String insertSql = """
            INSERT INTO horas_extras (usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                                    aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                                    data_solicitacao, data_atualizacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return syncTableData(h2Conn, sqlConn, selectSql, insertSql, "horas_extras");
    }
    
    /**
     * Sincroniza tabela de comprovantes
     */
    private static boolean syncComprovantes(Connection h2Conn, Connection sqlConn) throws SQLException {
        String selectSql = """
            SELECT usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                   valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                   outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                   caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
            FROM comprovantes
            """;
        
        String insertSql = """
            INSERT INTO comprovantes (usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                                    valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                                    outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                                    caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return syncTableData(h2Conn, sqlConn, selectSql, insertSql, "comprovantes");
    }
    
    /**
     * Executa a sincronização de dados entre as duas conexões
     */
    private static boolean syncTableData(Connection sourceConn, Connection targetConn, 
                                       String selectSql, String insertSql, String tableName) throws SQLException {
        
        List<Object[]> records = new ArrayList<>();
        
        // Lê dados do H2
        try (PreparedStatement selectStmt = sourceConn.prepareStatement(selectSql);
             ResultSet rs = selectStmt.executeQuery()) {
            
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();
            
            while (rs.next()) {
                Object[] record = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    record[i - 1] = rs.getObject(i);
                }
                records.add(record);
            }
        }
        
        if (records.isEmpty()) {
            logger.info("Nenhum registro encontrado em {} no H2", tableName);
            return true;
        }
        
        // Limpa tabela no SQL Server antes de inserir
        try (PreparedStatement deleteStmt = targetConn.prepareStatement("DELETE FROM " + tableName)) {
            deleteStmt.executeUpdate();
            logger.info("Tabela {} limpa no SQL Server", tableName);
        }
        
        // Insere dados no SQL Server
        targetConn.setAutoCommit(false);
        try (PreparedStatement insertStmt = targetConn.prepareStatement(insertSql)) {
            
            int batchSize = 0;
            for (Object[] record : records) {
                for (int i = 0; i < record.length; i++) {
                    insertStmt.setObject(i + 1, record[i]);
                }
                insertStmt.addBatch();
                
                if (++batchSize % 100 == 0) {
                    insertStmt.executeBatch();
                    targetConn.commit();
                }
            }
            
            // Executa o restante
            insertStmt.executeBatch();
            targetConn.commit();
            
            logger.info("✓ {} registros sincronizados na tabela {}", records.size(), tableName);
            return true;
            
        } catch (SQLException e) {
            targetConn.rollback();
            logger.error("Erro ao inserir dados na tabela {}: {}", tableName, e.getMessage());
            throw e;
        } finally {
            targetConn.setAutoCommit(true);
        }
    }
    
    /**
     * Força uma sincronização manual completa
     */
    public static void forceSyncNow() {
        logger.info("Forçando sincronização manual...");
        syncH2ToSqlServerAsync().thenAccept(success -> {
            if (success) {
                logger.info("Sincronização manual concluída com sucesso");
            } else {
                logger.error("Sincronização manual falhou");
            }
        });
    }
    
    /**
     * Verifica se a sincronização está em execução
     */
    public static boolean isSynchronizationRunning() {
        return synchronizationRunning;
    }
}
