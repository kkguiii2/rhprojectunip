package com.shiftly.controller;

import com.shiftly.service.AuthService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Controller para tela de login
 */
public class LoginController extends BaseController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private CheckBox rememberMeCheckBox;
    
    @Override
    public void initialize() {
        // Configura eventos
        setupEventHandlers();
        
        // Esconde indicador de progresso
        progressIndicator.setVisible(false);
        statusLabel.setText("");
        
        // Define foco inicial
        Platform.runLater(() -> emailField.requestFocus());
        
        logger.info("Tela de login inicializada");
    }
    
    /**
     * Configura manipuladores de eventos
     */
    private void setupEventHandlers() {
        // Enter no campo de email move para senha
        emailField.setOnKeyPressed(this::handleEmailKeyPress);
        
        // Enter no campo de senha executa login
        passwordField.setOnKeyPressed(this::handlePasswordKeyPress);
        
        // Limpa status quando usuário digita
        emailField.textProperty().addListener((obs, oldText, newText) -> clearStatus());
        passwordField.textProperty().addListener((obs, oldText, newText) -> clearStatus());
    }
    
    /**
     * Manipula teclas no campo de email
     */
    private void handleEmailKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            passwordField.requestFocus();
            event.consume();
        }
    }
    
    /**
     * Manipula teclas no campo de senha
     */
    private void handlePasswordKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
            event.consume();
        }
    }
    
    /**
     * Manipula o evento de login
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validações básicas
        if (!validateInputs(email, password)) {
            return;
        }
        
        // Desabilita interface durante login
        setLoginInProgress(true);
        
        // Executa login em background
        Task<AuthService.AuthResult> loginTask = new Task<AuthService.AuthResult>() {
            @Override
            protected AuthService.AuthResult call() throws Exception {
                return authService.login(email, password);
            }
        };
        
        loginTask.setOnSucceeded(e -> {
            AuthService.AuthResult result = loginTask.getValue();
            handleLoginResult(result);
        });
        
        loginTask.setOnFailed(e -> {
            Throwable exception = loginTask.getException();
            logger.error("Erro no login: {}", exception.getMessage(), exception);
            
            Platform.runLater(() -> {
                setLoginInProgress(false);
                showStatus("Erro interno. Tente novamente.", "error");
            });
        });
        
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }
    
    /**
     * Valida os campos de entrada
     */
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showStatus("Email é obrigatório", "error");
            emailField.requestFocus();
            return false;
        }
        
        if (!isValidEmail(email)) {
            showStatus("Email inválido", "error");
            emailField.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            showStatus("Senha é obrigatória", "error");
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            showStatus("Senha deve ter pelo menos 6 caracteres", "error");
            passwordField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida formato do email
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Manipula o resultado do login
     */
    private void handleLoginResult(AuthService.AuthResult result) {
        Platform.runLater(() -> {
            setLoginInProgress(false);
            
            if (result.isSuccess()) {
                // Armazena tokens
                currentAccessToken = result.getAccessToken();
                currentUser = null; // Será carregado quando necessário
                
                logger.info("Login realizado com sucesso para: {}", result.getUsuario().getEmail());
                
                // Navega para tela principal
                navigateToMainScreen();
                
            } else {
                showStatus(result.getMessage(), "error");
                passwordField.clear();
                passwordField.requestFocus();
            }
        });
    }
    
    /**
     * Navega para a tela principal baseada no tipo de usuário
     */
    private void navigateToMainScreen() {
        try {
            // Obtém informações do usuário
            updateCurrentUser();
            
            if (currentUser != null) {
                // Escolhe tela baseada no tipo de usuário
                if (currentUser.isRh()) {
                    navigateToScene("/fxml/rh-dashboard.fxml", "Shiftly - Painel RH", emailField);
                } else {
                    navigateToScene("/fxml/colaborador-dashboard.fxml", "Shiftly - Meu Painel", emailField);
                }
            } else {
                throw new RuntimeException("Não foi possível carregar informações do usuário");
            }
            
        } catch (Exception e) {
            logger.error("Erro ao navegar para tela principal: {}", e.getMessage(), e);
            showStatus("Erro ao carregar tela principal", "error");
        }
    }
    
    /**
     * Define estado de login em progresso
     */
    private void setLoginInProgress(boolean inProgress) {
        loginButton.setDisable(inProgress);
        emailField.setDisable(inProgress);
        passwordField.setDisable(inProgress);
        rememberMeCheckBox.setDisable(inProgress);
        progressIndicator.setVisible(inProgress);
        
        if (inProgress) {
            showStatus("Autenticando...", "info");
        }
    }
    
    /**
     * Exibe mensagem de status
     */
    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error", "success", "info");
        statusLabel.getStyleClass().add(type);
        statusLabel.setVisible(true);
    }
    
    /**
     * Limpa mensagem de status
     */
    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
    }
    
    /**
     * Navega para tela de cadastro
     */
    @FXML
    private void handleCadastro() {
        try {
            // Navega para a tela de cadastro
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            Scene cadastroScene = new Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/cadastro.fxml")));
            currentStage.setScene(cadastroScene);
            currentStage.setTitle("Shiftly - Cadastro");
        } catch (Exception e) {
            logger.error("Erro ao navegar para cadastro: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manipula esqueci minha senha
     */
    @FXML
    private void handleForgotPassword() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            showStatus("Digite seu email primeiro", "error");
            emailField.requestFocus();
            return;
        }
        
        if (!isValidEmail(email)) {
            showStatus("Email inválido", "error");
            emailField.requestFocus();
            return;
        }
        
        // TODO: Implementar recuperação de senha
        showWarning("Funcionalidade em Desenvolvimento", 
                   "A recuperação de senha será implementada em breve.\n" +
                   "Entre em contato com o RH para redefinir sua senha.");
    }
    
    /**
     * Abre tela sobre o sistema
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre o Shiftly");
        alert.setHeaderText("Sistema de Controle de Ponto Eletrônico");
        alert.setContentText(
            "Shiftly v1.0.0\n\n" +
            "Sistema completo de controle de ponto com:\n" +
            "• Reconhecimento facial\n" +
            "• Geolocalização\n" +
            "• Gestão de férias\n" +
            "• Controle de horas extras\n" +
            "• Relatórios e comprovantes\n\n" +
            "Desenvolvido com Java e JavaFX"
        );
        alert.showAndWait();
    }
    
    /**
     * Fecha a aplicação
     */
    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
