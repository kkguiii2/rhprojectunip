package com.shiftly.repository;

import com.shiftly.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Configuração e gerenciamento de conexões com banco de dados
 * Suporta SQL Server como principal e H2 como fallback
 */
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    // Configurações SQL Server (FORÇADAS para usar as credenciais corretas)
    private static final String SQL_SERVER_HOST = "localhost";
    private static final String SQL_SERVER_PORT = "1433";
    private static final String SQL_SERVER_DATABASE = "ShiftlyDB";
    private static final String SQL_SERVER_USERNAME = "guilherme";
    private static final String SQL_SERVER_PASSWORD = "teste123";
    
    // Configurações H2 (lidas do arquivo properties)
    private static final String H2_URL = getConfigValue("database.h2.url", "jdbc:h2:mem:shiftly;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    private static final String H2_USERNAME = getConfigValue("database.h2.username", "sa");
    private static final String H2_PASSWORD = getConfigValue("database.h2.password", "");
    
    private static DatabaseType currentDatabaseType = DatabaseType.SQL_SERVER;
    private static boolean sqlServerAvailable = false;
    
    public enum DatabaseType {
        SQL_SERVER, H2
    }
    
    /**
     * Obtém valor de configuração com fallback
     */
    private static String getConfigValue(String key, String defaultValue) {
        String value = ConfigUtil.getString(key);
        logger.info("Configuração {} = {} (padrão: {})", key, value, defaultValue);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Verifica se o SQL Server está disponível
     */
    public static boolean isSqlServerAvailable() {
        try (Connection conn = createSqlServerConnection()) {
            sqlServerAvailable = true;
            logger.info("SQL Server está disponível");
            return true;
        } catch (SQLException e) {
            sqlServerAvailable = false;
            logger.warn("SQL Server não está disponível: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtém uma conexão com o banco principal (SQL Server) ou fallback (H2)
     */
    public static Connection getConnection() throws SQLException {
        if (currentDatabaseType == DatabaseType.SQL_SERVER && isSqlServerAvailable()) {
            return createSqlServerConnection();
        } else {
            if (currentDatabaseType == DatabaseType.SQL_SERVER) {
                logger.info("Mudando para H2 como fallback");
                currentDatabaseType = DatabaseType.H2;
            }
            return createH2Connection();
        }
    }
    
    /**
     * Força o uso do SQL Server
     */
    public static Connection getSqlServerConnection() throws SQLException {
        return createSqlServerConnection();
    }
    
    /**
     * Força o uso do H2
     */
    public static Connection getH2Connection() throws SQLException {
        return createH2Connection();
    }
    
    /**
     * Cria conexão com SQL Server
     */
    private static Connection createSqlServerConnection() throws SQLException {
        String url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true", 
                                 SQL_SERVER_HOST, SQL_SERVER_PORT, SQL_SERVER_DATABASE);
        
        Properties props = new Properties();
        props.setProperty("user", SQL_SERVER_USERNAME);
        props.setProperty("password", SQL_SERVER_PASSWORD);
        props.setProperty("loginTimeout", "5"); // 5 segundos de timeout
        
        return DriverManager.getConnection(url, props);
    }
    
    /**
     * Cria conexão com H2
     */
    private static Connection createH2Connection() throws SQLException {
        return DriverManager.getConnection(H2_URL, H2_USERNAME, H2_PASSWORD);
    }
    
    /**
     * Retorna o tipo de banco atualmente em uso
     */
    public static DatabaseType getCurrentDatabaseType() {
        return currentDatabaseType;
    }
    
    /**
     * Define o tipo de banco a ser usado
     */
    public static void setCurrentDatabaseType(DatabaseType databaseType) {
        currentDatabaseType = databaseType;
        logger.info("Tipo de banco alterado para: {}", databaseType);
    }
    
    /**
     * Verifica se estamos usando SQL Server
     */
    public static boolean isUsingSqlServer() {
        return currentDatabaseType == DatabaseType.SQL_SERVER && sqlServerAvailable;
    }
    
    /**
     * Verifica se estamos usando H2
     */
    public static boolean isUsingH2() {
        return currentDatabaseType == DatabaseType.H2;
    }
    
    /**
     * Tenta reconectar com SQL Server
     */
    public static boolean tryReconnectSqlServer() {
        if (isSqlServerAvailable()) {
            currentDatabaseType = DatabaseType.SQL_SERVER;
            logger.info("Reconectado com SQL Server");
            return true;
        }
        return false;
    }
    
    /**
     * Fecha uma conexão de forma segura
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Erro ao fechar conexão: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Executa um teste de conectividade
     */
    public static void testConnectivity() {
        logger.info("=== Testando conectividade dos bancos ===");
        
        // Teste SQL Server
        try (Connection conn = createSqlServerConnection()) {
            logger.info("✓ SQL Server: Conectado com sucesso");
            sqlServerAvailable = true;
        } catch (SQLException e) {
            logger.warn("✗ SQL Server: Falha na conexão - {}", e.getMessage());
            sqlServerAvailable = false;
        }
        
        // Teste H2
        try (Connection conn = createH2Connection()) {
            logger.info("✓ H2: Conectado com sucesso");
        } catch (SQLException e) {
            logger.error("✗ H2: Falha na conexão - {}", e.getMessage());
        }
        
        logger.info("=== Fim do teste de conectividade ===");
    }
}
