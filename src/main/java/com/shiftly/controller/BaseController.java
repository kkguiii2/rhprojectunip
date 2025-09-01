package com.shiftly.controller;

import com.shiftly.service.AuthService;
import com.shiftly.util.JwtUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller base com funcionalidades comuns
 */
public abstract class BaseController {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    // Serviços compartilhados
    protected static AuthService authService = new AuthService();
    
    // Estado da aplicação
    protected static String currentAccessToken;
    protected static JwtUtil.UserInfo currentUser;
    
    /**
     * Inicialização do controller
     */
    @FXML
    public void initialize() {
        // Implementado pelas subclasses se necessário
    }
    
    /**
     * Carrega uma nova tela
     */
    protected void loadScene(String fxmlPath, String title) {
        loadScene(fxmlPath, title, null);
    }
    
    /**
     * Carrega uma nova tela em um stage específico
     */
    protected void loadScene(String fxmlPath, String title, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (stage == null) {
                stage = new Stage();
            }
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            
            logger.info("Tela carregada: {} - {}", title, fxmlPath);
            
        } catch (IOException e) {
            logger.error("Erro ao carregar tela {}: {}", fxmlPath, e.getMessage(), e);
            showError("Erro", "Não foi possível carregar a tela: " + e.getMessage());
        }
    }
    
    /**
     * Fecha a tela atual
     */
    protected void closeCurrentStage(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Navega para uma nova tela fechando a atual
     */
    protected void navigateToScene(String fxmlPath, String title, Node currentNode) {
        try {
            Stage currentStage = (Stage) currentNode.getScene().getWindow();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            currentStage.setTitle(title);
            currentStage.setScene(scene);
            
            logger.info("Navegou para: {} - {}", title, fxmlPath);
            
        } catch (IOException e) {
            logger.error("Erro ao navegar para {}: {}", fxmlPath, e.getMessage(), e);
            showError("Erro", "Não foi possível navegar para a tela: " + e.getMessage());
        }
    }
    
    /**
     * Mostra alerta de sucesso
     */
    protected void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Mostra alerta de erro
     */
    protected void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Mostra alerta de aviso
     */
    protected void showWarning(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    /**
     * Mostra diálogo de confirmação
     */
    protected boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Verifica se o usuário está autenticado
     */
    protected boolean isAuthenticated() {
        return currentAccessToken != null && authService.validateToken(currentAccessToken);
    }
    
    /**
     * Verifica se o usuário tem permissão de RH
     */
    protected boolean hasRhPermission() {
        return isAuthenticated() && authService.hasRhPermission(currentAccessToken);
    }
    
    /**
     * Verifica se o usuário tem permissão de Admin
     */
    protected boolean hasAdminPermission() {
        return isAuthenticated() && authService.hasAdminPermission(currentAccessToken);
    }
    
    /**
     * Redireciona para login se não autenticado
     */
    protected boolean checkAuthentication(Node currentNode) {
        if (!isAuthenticated()) {
            showWarning("Acesso Negado", "Você precisa estar logado para acessar esta funcionalidade.");
            navigateToScene("/fxml/login.fxml", "Shiftly - Login", currentNode);
            return false;
        }
        return true;
    }
    
    /**
     * Verifica permissão de RH
     */
    protected boolean checkRhPermission() {
        if (!hasRhPermission()) {
            showWarning("Acesso Negado", "Você não tem permissão para acessar esta funcionalidade.");
            return false;
        }
        return true;
    }
    
    /**
     * Verifica permissão de Admin
     */
    protected boolean checkAdminPermission() {
        if (!hasAdminPermission()) {
            showWarning("Acesso Negado", "Apenas administradores podem acessar esta funcionalidade.");
            return false;
        }
        return true;
    }
    
    /**
     * Realiza logout
     */
    protected void logout(Node currentNode) {
        if (currentAccessToken != null) {
            authService.logout(currentAccessToken);
        }
        
        currentAccessToken = null;
        currentUser = null;
        
        navigateToScene("/fxml/login.fxml", "Shiftly - Login", currentNode);
        logger.info("Logout realizado");
    }
    
    /**
     * Atualiza informações do usuário atual
     */
    protected void updateCurrentUser() {
        if (currentAccessToken != null) {
            Optional<JwtUtil.UserInfo> userInfoOpt = authService.getUserInfo(currentAccessToken);
            if (userInfoOpt.isPresent()) {
                currentUser = userInfoOpt.get();
            }
        }
    }
    
    /**
     * Obtém o usuário atual
     */
    protected JwtUtil.UserInfo getCurrentUser() {
        if (currentUser == null) {
            updateCurrentUser();
        }
        return currentUser;
    }
    
    /**
     * Executa tarefa em background
     */
    protected void runInBackground(Runnable task) {
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
    
    /**
     * Executa tarefa no thread da UI
     */
    protected void runInUIThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
    
    /**
     * Formata texto para exibição
     */
    protected String formatText(String text, Object... args) {
        if (text == null) return "";
        return String.format(text, args);
    }
    
    /**
     * Valida se um campo não está vazio
     */
    protected boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Limpa campos de texto
     */
    protected void clearFields(javafx.scene.control.TextInputControl... fields) {
        for (javafx.scene.control.TextInputControl field : fields) {
            if (field != null) {
                field.clear();
            }
        }
    }
    
    /**
     * Define foco em um campo
     */
    protected void setFocus(Node node) {
        Platform.runLater(() -> {
            if (node != null) {
                node.requestFocus();
            }
        });
    }
}
