package com.shiftly.repository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Classe responsável por inicializar as estruturas do banco de dados
 * Suporta SQL Server e H2 com scripts específicos e completos
 */
public class DatabaseInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Inicializa o banco de dados com todas as estruturas necessárias
     */
    public static void initializeDatabase() {
        logger.info("=== INICIALIZAÇÃO DO BANCO DE DADOS ===");
        logger.info("Iniciando criação das estruturas do banco de dados...");
        
        try {
            if (DatabaseConfig.isSqlServerAvailable()) {
                logger.info("SQL Server disponível - inicializando...");
                initializeSqlServer();
            } else {
                logger.info("SQL Server não disponível - usando H2 como fallback...");
                initializeH2();
            }
            logger.info("=== BANCO DE DADOS INICIALIZADO COM SUCESSO ===");
        } catch (Exception e) {
            logger.error("=== ERRO NA INICIALIZAÇÃO DO BANCO ===");
            logger.error("Erro ao inicializar banco de dados: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na inicialização do banco de dados", e);
        }
    }
    
    /**
     * Inicializa SQL Server com script completo
     */
    private static void initializeSqlServer() {
        logger.info("Inicializando SQL Server...");
        
        try {
            // Primeiro, conecta no master para criar o banco
            createSqlServerDatabase();
            
            // Depois, executa o script simplificado no banco criado
            executeSqlServerScript();
            
            logger.info("SQL Server inicializado com sucesso");
        } catch (SQLException e) {
            logger.warn("SQL Server não está disponível: {}", e.getMessage());
            logger.info("Mudando para H2 como fallback");
            initializeH2();
        }
    }
    
    /**
     * Cria o banco de dados no SQL Server
     */
    private static void createSqlServerDatabase() throws SQLException {
        String createDbSql = """
            IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'ShiftlyDB')
            BEGIN
                CREATE DATABASE ShiftlyDB 
                COLLATE SQL_Latin1_General_CP1_CI_AS;
                PRINT 'Banco de dados ShiftlyDB criado com sucesso!';
            END
            """;
        
        // Conecta no master para criar o banco
        String masterUrl = "jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false;trustServerCertificate=true";
        
        try (Connection conn = java.sql.DriverManager.getConnection(masterUrl, "guilherme", "teste123");
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createDbSql);
            logger.info("Banco ShiftlyDB criado/verificado com sucesso");
            
        } catch (SQLException e) {
            logger.error("Erro ao criar banco ShiftlyDB: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Executa o script completo do SQL Server
     */
    private static void executeSqlServerScript() {
        logger.info("Executando script completo do SQL Server...");
        
        try (Connection conn = DatabaseConfig.getSqlServerConnection()) {
            String script = loadScriptFromResource("/database/create-database-simple.sql");
            executeScript(conn, script);
            logger.info("Script simplificado do SQL Server executado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao executar script do SQL Server: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao executar script SQL Server", e);
        }
    }
    
    /**
     * Inicializa H2 com script específico
     */
    private static void initializeH2() {
        logger.info("Inicializando H2...");
        
        try (Connection conn = DatabaseConfig.getH2Connection()) {
            String script = loadScriptFromResource("/database/create-database-h2.sql");
            executeScript(conn, script);
            logger.info("H2 inicializado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao inicializar H2: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao inicializar H2", e);
        }
    }
    
    /**
     * Carrega script SQL de um recurso
     */
    private static String loadScriptFromResource(String resourcePath) {
        logger.info("Tentando carregar script: {}", resourcePath);
        
        try (InputStream inputStream = DatabaseInitializer.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                logger.error("InputStream é null para: {}", resourcePath);
                throw new RuntimeException("Script não encontrado: " + resourcePath);
            }
            
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            scanner.useDelimiter("\\A");
            String script = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            
            if (script.isEmpty()) {
                throw new RuntimeException("Script está vazio: " + resourcePath);
            }
            
            logger.info("Script carregado com sucesso: {} ({} caracteres)", resourcePath, script.length());
            logger.debug("Primeiros 200 caracteres: {}", script.substring(0, Math.min(200, script.length())));
            return script;
            
        } catch (Exception e) {
            logger.error("Erro ao carregar script {}: {}", resourcePath, e.getMessage(), e);
            throw new RuntimeException("Falha ao carregar script: " + resourcePath, e);
        }
    }
    
    /**
     * Executa um script SQL dividindo por comandos
     */
    private static void executeScript(Connection conn, String script) throws SQLException {
        logger.info("Iniciando execução do script SQL...");
        
        // Remove comentários e divide por comandos
        String[] commands = script
            .replaceAll("--.*", "") // Remove comentários de linha
            .replaceAll("/\\*[\\s\\S]*?\\*/", "") // Remove comentários de bloco
            .split(";");
        
        logger.info("Script dividido em {} comandos", commands.length);
        
        int executedCommands = 0;
        int failedCommands = 0;
        
        try (Statement stmt = conn.createStatement()) {
            for (int i = 0; i < commands.length; i++) {
                String command = commands[i];
                String cleanCommand = command.trim();
                
                if (!cleanCommand.isEmpty() && !cleanCommand.equals("GO")) {
                    try {
                        logger.debug("Executando comando {}: {}", i + 1, cleanCommand.substring(0, Math.min(50, cleanCommand.length())) + "...");
                        stmt.execute(cleanCommand);
                        executedCommands++;
                        
                        if (executedCommands % 5 == 0) {
                            logger.info("Executados {} comandos SQL", executedCommands);
                        }
                        
                    } catch (SQLException e) {
                        failedCommands++;
                        // Log o erro mas continua (muitos comandos podem já existir)
                        logger.warn("Comando SQL {} falhou (pode já existir): {}", i + 1, e.getMessage());
                    }
                }
            }
        }
        
        logger.info("Script executado: {} comandos processados, {} falharam", executedCommands, failedCommands);
    }
    
    /**
     * Verifica se as tabelas principais existem
     */
    public static boolean isDatabaseInitialized() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tenta fazer uma consulta simples na tabela usuarios
            stmt.executeQuery("SELECT COUNT(*) FROM usuarios LIMIT 1");
            return true;
            
        } catch (Exception e) {
            logger.debug("Banco de dados não inicializado: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Força re-inicialização do banco (usado para testes)
     */
    public static void reinitializeDatabase() {
        logger.warn("REINICIALIZANDO BANCO DE DADOS - DADOS SERÃO PERDIDOS!");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Drop das tabelas principais (ordem reversa devido a FKs)
            String[] dropTables = {
                "DROP TABLE IF EXISTS log_auditoria",
                "DROP TABLE IF EXISTS notificacoes", 
                "DROP TABLE IF EXISTS comprovantes",
                "DROP TABLE IF EXISTS horas_extras",
                "DROP TABLE IF EXISTS ferias",
                "DROP TABLE IF EXISTS pontos",
                "DROP TABLE IF EXISTS configuracoes_sistema",
                "DROP TABLE IF EXISTS usuarios"
            };
            
            for (String dropSql : dropTables) {
                try {
                    stmt.execute(dropSql);
                } catch (SQLException e) {
                    logger.debug("Tabela não existia: {}", e.getMessage());
                }
            }
            
            logger.info("Tabelas removidas, reinicializando...");
            initializeDatabase();
            
        } catch (Exception e) {
            logger.error("Erro ao reinicializar banco: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao reinicializar banco", e);
        }
    }
    
    /**
     * Retorna estatísticas do banco
     */
    public static String getDatabaseStats() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder stats = new StringBuilder();
            stats.append("=== ESTATÍSTICAS DO BANCO DE DADOS ===\n");
            stats.append("Tipo: ").append(DatabaseConfig.getCurrentDatabaseType()).append("\n");
            
            String[] tables = {"usuarios", "pontos", "ferias", "horas_extras", "comprovantes", "notificacoes"};
            
            try (Statement stmt = conn.createStatement()) {
                for (String table : tables) {
                    try {
                        var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                        if (rs.next()) {
                            stats.append(String.format("%-20s: %d registros\n", table, rs.getInt(1)));
                        }
                    } catch (SQLException e) {
                        stats.append(String.format("%-20s: Erro (%s)\n", table, e.getMessage()));
                    }
                }
            }
            
            return stats.toString();
            
        } catch (Exception e) {
            return "Erro ao obter estatísticas: " + e.getMessage();
        }
    }
}