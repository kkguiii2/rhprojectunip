package com.shiftly.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface base para todos os repositories
 */
public abstract class BaseRepository<T, ID> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Busca uma entidade por ID
     */
    public abstract Optional<T> findById(ID id);
    
    /**
     * Busca todas as entidades
     */
    public abstract List<T> findAll();
    
    /**
     * Salva uma entidade (insert ou update)
     */
    public abstract T save(T entity);
    
    /**
     * Deleta uma entidade por ID
     */
    public abstract boolean deleteById(ID id);
    
    /**
     * Verifica se uma entidade existe por ID
     */
    public abstract boolean existsById(ID id);
    
    /**
     * Conta o total de entidades
     */
    public abstract long count();
    
    /**
     * Obtém uma conexão com o banco
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
    
    /**
     * Fecha recursos de forma segura
     */
    protected void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Erro ao fechar ResultSet: {}", e.getMessage());
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Erro ao fechar PreparedStatement: {}", e.getMessage());
            }
        }
        
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Erro ao fechar Connection: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Fecha recursos sem ResultSet
     */
    protected void closeResources(Connection conn, PreparedStatement stmt) {
        closeResources(conn, stmt, null);
    }
    
    /**
     * Executa uma operação com tratamento de exceção
     */
    protected <R> R executeWithConnection(ConnectionOperation<R> operation) {
        Connection conn = null;
        try {
            conn = getConnection();
            return operation.execute(conn);
        } catch (SQLException e) {
            logger.error("Erro ao executar operação no banco: {}", e.getMessage(), e);
            throw new RuntimeException("Erro de banco de dados", e);
        } finally {
            DatabaseConfig.closeConnection(conn);
        }
    }
    
    /**
     * Interface funcional para operações com conexão
     */
    @FunctionalInterface
    protected interface ConnectionOperation<R> {
        R execute(Connection conn) throws SQLException;
    }
}
