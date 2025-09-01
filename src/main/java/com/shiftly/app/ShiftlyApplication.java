package com.shiftly.app;

import com.shiftly.repository.DatabaseConfig;
import com.shiftly.repository.DatabaseInitializer;
import com.shiftly.repository.DatabaseSynchronizer;
import com.shiftly.service.SyncService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Classe principal da aplicação Shiftly
 */
public class ShiftlyApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(ShiftlyApplication.class);
    
    // Configurações da aplicação
    private static final String APP_TITLE = "Shiftly - Sistema de Controle de Ponto Eletrônico";
    private static final String APP_VERSION = "1.0.0";
    private static final int MIN_WIDTH = 1200;
    private static final int MIN_HEIGHT = 800;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        logger.info("=== Inicializando Shiftly v{} ===", APP_VERSION);
        
        // Inicializa sistema de banco de dados
        initializeDatabase();
        
        // Inicia sincronização automática
        startAutoSync();
        
        logger.info("Aplicação inicializada com sucesso");
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Iniciando interface gráfica...");
        
        try {
            // Carrega a tela de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            // Configura a cena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configura a janela principal
            setupPrimaryStage(primaryStage, scene);
            
            // Exibe a janela
            primaryStage.show();
            
            logger.info("Interface gráfica iniciada com sucesso");
            
        } catch (IOException e) {
            logger.error("Erro ao carregar interface gráfica: {}", e.getMessage(), e);
            showErrorAndExit("Erro Fatal", "Não foi possível carregar a interface gráfica: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
        
        logger.info("Finalizando aplicação...");
        
        // Para sincronização automática
        DatabaseSynchronizer.stopAutoSync();
        
        // Limpa recursos
        cleanup();
        
        logger.info("Aplicação finalizada");
    }
    
    /**
     * Configura a janela principal
     */
    private void setupPrimaryStage(Stage primaryStage, Scene scene) {
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.centerOnScreen();
        
        // Define ícone da aplicação
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Não foi possível carregar ícone da aplicação: {}", e.getMessage());
        }
        
        // Configura evento de fechamento
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Solicitação de fechamento da aplicação");
            Platform.exit();
        });
        
        // Maximiza a janela
        primaryStage.setMaximized(true);
    }
    
    /**
     * Inicializa o sistema de banco de dados
     */
    private void initializeDatabase() {
        logger.info("Inicializando sistema de banco de dados...");
        
        try {
            // Testa conectividade
            DatabaseConfig.testConnectivity();
            
            // Inicializa estruturas do banco
            DatabaseInitializer.initializeDatabase();
            
            logger.info("Sistema de banco de dados inicializado com sucesso");
            
        } catch (Exception e) {
            logger.error("Erro crítico ao inicializar banco de dados: {}", e.getMessage(), e);
            showErrorAndExit("Erro de Banco de Dados", 
                "Não foi possível inicializar o banco de dados.\n" +
                "Verifique a configuração do SQL Server ou entre em contato com o suporte.\n\n" +
                "O sistema funcionará em modo offline com banco H2.");
        }
    }
    
    /**
     * Inicia sincronização automática
     */
    private void startAutoSync() {
        logger.info("Iniciando sincronização automática...");
        
        try {
            // Inicia sincronização automática com SQL Server
            DatabaseSynchronizer.startAutoSync();
            
            // Inicia serviço de sincronização offline
            SyncService syncService = new SyncService();
            
            logger.info("Sincronização automática iniciada");
        } catch (Exception e) {
            logger.warn("Erro ao iniciar sincronização automática: {}", e.getMessage());
        }
    }
    
    /**
     * Limpa recursos da aplicação
     */
    private void cleanup() {
        // Executa limpeza de sessões expiradas
        try {
            // TODO: Implementar limpeza de recursos se necessário
            logger.debug("Limpeza de recursos concluída");
        } catch (Exception e) {
            logger.warn("Erro durante limpeza de recursos: {}", e.getMessage());
        }
    }
    
    /**
     * Mostra erro crítico e encerra aplicação
     */
    private void showErrorAndExit(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Erro Crítico");
            alert.setContentText(message);
            alert.showAndWait();
            
            Platform.exit();
            System.exit(1);
        });
    }
    
    /**
     * Método main principal
     */
    public static void main(String[] args) {
        logger.info("Iniciando Shiftly...");
        
        // Configura propriedades do sistema
        setupSystemProperties();
        
        // Configura tratamento de exceções não capturadas
        setupExceptionHandling();
        
        // Inicia aplicação JavaFX
        launch(args);
    }
    
    /**
     * Configura propriedades do sistema
     */
    private static void setupSystemProperties() {
        // Configurações de log
        System.setProperty("logback.configurationFile", "logback.xml");
        
        // Configurações de rede (caso necessário para futuras integrações)
        System.setProperty("java.net.useSystemProxies", "true");
        
        logger.debug("Propriedades do sistema configuradas");
    }
    
    /**
     * Configura tratamento de exceções não capturadas
     */
    private static void setupExceptionHandling() {
        // Thread principal
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            logger.error("Exceção não capturada na thread {}: {}", thread.getName(), exception.getMessage(), exception);
        });
        
        // Threads JavaFX
        Platform.runLater(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((thread, exception) -> {
                logger.error("Exceção não capturada na thread JavaFX: {}", exception.getMessage(), exception);
                
                Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Erro Inesperado");
                    alert.setHeaderText("Ocorreu um erro inesperado");
                    alert.setContentText("O sistema encontrou um erro inesperado. Por favor, reinicie a aplicação.");
                    alert.showAndWait();
                });
            });
        });
        
        logger.debug("Tratamento de exceções configurado");
    }
    
    /**
     * Obtém informações da aplicação
     */
    public static String getApplicationInfo() {
        return String.format("%s v%s", APP_TITLE, APP_VERSION);
    }
    
    /**
     * Obtém versão da aplicação
     */
    public static String getVersion() {
        return APP_VERSION;
    }
}
