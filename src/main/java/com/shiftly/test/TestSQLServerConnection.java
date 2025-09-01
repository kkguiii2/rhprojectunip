package com.shiftly.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class TestSQLServerConnection {
    public static void main(String[] args) {
        System.out.println("=== TESTE DE CONEXÃO SQL SERVER ===");
        
        // Configurações
        String host = "localhost";
        String port = "1433";
        String database = "master"; // Conecta no master primeiro
        String username = "guilherme";
        String password = "teste123";
        
        // Teste 1: Conectar no master
        System.out.println("\n1. Testando conexão no master...");
        String masterUrl = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true", 
                                       host, port, database);
        
        try (Connection conn = DriverManager.getConnection(masterUrl, username, password)) {
            System.out.println("✅ CONECTADO NO MASTER com sucesso!");
            System.out.println("   URL: " + masterUrl);
            System.out.println("   Usuário: " + username);
            
            // Teste 2: Criar banco ShiftlyDB
            System.out.println("\n2. Testando criação do banco ShiftlyDB...");
            try (var stmt = conn.createStatement()) {
                String createDbSql = """
                    IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'ShiftlyDB')
                    BEGIN
                        CREATE DATABASE ShiftlyDB 
                        COLLATE SQL_Latin1_General_CP1_CI_AS;
                        PRINT 'Banco de dados ShiftlyDB criado com sucesso!';
                    END
                    """;
                
                stmt.execute(createDbSql);
                System.out.println("✅ Banco ShiftlyDB criado/verificado com sucesso!");
                
                // Teste 3: Conectar no ShiftlyDB
                System.out.println("\n3. Testando conexão no ShiftlyDB...");
                String shiftlyUrl = String.format("jdbc:sqlserver://%s:%s;databaseName=ShiftlyDB;encrypt=false;trustServerCertificate=true", 
                                               host, port);
                
                try (Connection shiftlyConn = DriverManager.getConnection(shiftlyUrl, username, password)) {
                    System.out.println("✅ CONECTADO NO SHIFTLYDB com sucesso!");
                    System.out.println("   URL: " + shiftlyUrl);
                    System.out.println("   Usuário: " + username);
                    
                    // Teste 4: Verificar se pode executar comandos
                    try (var stmt2 = shiftlyConn.createStatement()) {
                        stmt2.execute("SELECT 1 as teste");
                        System.out.println("✅ Comandos SQL funcionando perfeitamente!");
                    }
                    
                } catch (SQLException e) {
                    System.out.println("❌ ERRO ao conectar no ShiftlyDB: " + e.getMessage());
                }
                
            } catch (SQLException e) {
                System.out.println("❌ ERRO ao criar banco: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao conectar no master: " + e.getMessage());
            System.out.println("   Verifique se:");
            System.out.println("   1. SQL Server está rodando");
            System.out.println("   2. Porta 1433 está aberta");
            System.out.println("   3. Usuário 'guilherme' existe e tem permissões");
            System.out.println("   4. Senha 'teste123' está correta");
            System.out.println("   5. Autenticação SQL Server está habilitada");
        }
        
        System.out.println("\n=== FIM DO TESTE ===");
    }
}
