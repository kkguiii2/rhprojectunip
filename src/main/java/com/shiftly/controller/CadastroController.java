package com.shiftly.controller;

import com.shiftly.model.Usuario;
import com.shiftly.service.UsuarioService;
import com.shiftly.model.TipoUsuario;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Controller para a tela de cadastro de usuários
 */
public class CadastroController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(CadastroController.class);
    
    @FXML private ComboBox<String> tipoUsuarioCombo;
    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private PasswordField confirmarSenhaField;
    @FXML private TextField cargoField;
    @FXML private TextField departamentoField;
    @FXML private TextField telefoneField;
    @FXML private TextField cpfField;
    @FXML private DatePicker dataNascimentoPicker;
    @FXML private TextField enderecoField;
    @FXML private TextField cepField;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button cadastrarButton;
    @FXML private Button voltarButton;
    
    private UsuarioService usuarioService;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.info("Tela de cadastro inicializada");
        
        usuarioService = new UsuarioService();
        
        // Configurar ComboBox de tipo de usuário
        tipoUsuarioCombo.getItems().addAll(
            "COLABORADOR",
            "RH", 
            "ADMIN"
        );
        tipoUsuarioCombo.setValue("COLABORADOR");
        
        // Configurar DatePicker
        dataNascimentoPicker.setValue(LocalDate.now().minusYears(18));
        
        // Configurar validações em tempo real
        setupValidations();
        
        // Configurar eventos
        setupEventHandlers();
    }
    
    /**
     * Configura validações em tempo real
     */
    private void setupValidations() {
        // Validação de email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidEmail(newValue)) {
                emailField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            } else {
                emailField.setStyle("");
            }
        });
        
        // Validação de CPF
        cpfField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidCPF(newValue)) {
                cpfField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            } else {
                cpfField.setStyle("");
            }
        });
        
        // Validação de CEP
        cepField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidCEP(newValue)) {
                cepField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            } else {
                cpfField.setStyle("");
            }
        });
        
        // Validação de telefone
        telefoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidTelefone(newValue)) {
                telefoneField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
            } else {
                telefoneField.setStyle("");
            }
        });
    }
    
    /**
     * Configura handlers de eventos
     */
    private void setupEventHandlers() {
        // Enter no último campo executa cadastro
        cepField.setOnAction(e -> handleCadastrar());
        
        // Foco automático no próximo campo
        nomeField.setOnAction(e -> emailField.requestFocus());
        emailField.setOnAction(e -> senhaField.requestFocus());
        senhaField.setOnAction(e -> confirmarSenhaField.requestFocus());
        confirmarSenhaField.setOnAction(e -> cargoField.requestFocus());
        cargoField.setOnAction(e -> departamentoField.requestFocus());
        departamentoField.setOnAction(e -> telefoneField.requestFocus());
        telefoneField.setOnAction(e -> cpfField.requestFocus());
        cpfField.setOnAction(e -> dataNascimentoPicker.requestFocus());
        enderecoField.setOnAction(e -> cepField.requestFocus());
    }
    
    /**
     * Manipula o cadastro do usuário
     */
    @FXML
    private void handleCadastrar() {
        logger.info("Iniciando cadastro de usuário...");
        
        // Validações básicas
        if (!validateInputs()) {
            return;
        }
        
        // Desabilita interface durante cadastro
        setCadastroInProgress(true);
        
        // Cria usuário
        Usuario usuario = createUsuarioFromInputs();
        
        // Executa cadastro em background
        Task<Boolean> cadastroTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return usuarioService.criarUsuario(usuario) != null;
            }
        };
        
        cadastroTask.setOnSucceeded(e -> {
            boolean success = cadastroTask.getValue();
            if (success) {
                handleCadastroSuccess();
            } else {
                handleCadastroError("Erro ao cadastrar usuário. Tente novamente.");
            }
        });
        
        cadastroTask.setOnFailed(e -> {
            Throwable exception = cadastroTask.getException();
            logger.error("Erro no cadastro: {}", exception.getMessage(), exception);
            
            Platform.runLater(() -> {
                setCadastroInProgress(false);
                showStatus("Erro interno. Tente novamente.", "error");
            });
        });
        
        Thread cadastroThread = new Thread(cadastroTask);
        cadastroThread.setDaemon(true);
        cadastroThread.start();
    }
    
    /**
     * Valida os campos de entrada
     */
    private boolean validateInputs() {
        // Tipo de usuário
        if (tipoUsuarioCombo.getValue() == null) {
            showStatus("Selecione o tipo de usuário", "error");
            tipoUsuarioCombo.requestFocus();
            return false;
        }
        
        // Nome
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            showStatus("Nome é obrigatório", "error");
            nomeField.requestFocus();
            return false;
        }
        
        if (nome.length() < 3) {
            showStatus("Nome deve ter pelo menos 3 caracteres", "error");
            nomeField.requestFocus();
            return false;
        }
        
        // Email
        String email = emailField.getText().trim();
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
        
        // Senha
        String senha = senhaField.getText();
        if (senha.isEmpty()) {
            showStatus("Senha é obrigatória", "error");
            senhaField.requestFocus();
            return false;
        }
        
        if (senha.length() < 6) {
            showStatus("Senha deve ter pelo menos 6 caracteres", "error");
            senhaField.requestFocus();
            return false;
        }
        
        // Confirmar senha
        String confirmarSenha = confirmarSenhaField.getText();
        if (!senha.equals(confirmarSenha)) {
            showStatus("Senhas não coincidem", "error");
            confirmarSenhaField.requestFocus();
            return false;
        }
        
        // CPF (se preenchido)
        String cpf = cpfField.getText().trim();
        if (!cpf.isEmpty() && !isValidCPF(cpf)) {
            showStatus("CPF inválido", "error");
            cpfField.requestFocus();
            return false;
        }
        
        // CEP (se preenchido)
        String cep = cepField.getText().trim();
        if (!cep.isEmpty() && !isValidCEP(cep)) {
            showStatus("CEP inválido", "error");
            cepField.requestFocus();
            return false;
        }
        
        // Telefone (se preenchido)
        String telefone = telefoneField.getText().trim();
        if (!telefone.isEmpty() && !isValidTelefone(telefone)) {
            showStatus("Telefone inválido", "error");
            telefoneField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Cria objeto Usuario a partir dos campos
     */
    private Usuario createUsuarioFromInputs() {
        Usuario usuario = new Usuario();
        
        usuario.setNome(nomeField.getText().trim());
        usuario.setEmail(emailField.getText().trim().toLowerCase());
        usuario.setSenha(senhaField.getText());
        usuario.setTipoUsuario(TipoUsuario.valueOf(tipoUsuarioCombo.getValue()));
        usuario.setCargo(cargoField.getText().trim());
        usuario.setDepartamento(departamentoField.getText().trim());
        usuario.setCpf(cpfField.getText().trim());
        usuario.setAtivo(true);
        
        return usuario;
    }
    
    /**
     * Manipula sucesso no cadastro
     */
    private void handleCadastroSuccess() {
        Platform.runLater(() -> {
            setCadastroInProgress(false);
            
            // Mostra mensagem de sucesso
            showStatus("Usuário cadastrado com sucesso! Redirecionando para login...", "success");
            
            // Aguarda 2 segundos e volta para login
            Task<Void> delayTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(2000);
                    return null;
                }
            };
            
            delayTask.setOnSucceeded(e -> {
                Platform.runLater(() -> handleVoltar());
            });
            
            Thread delayThread = new Thread(delayTask);
            delayThread.setDaemon(true);
            delayThread.start();
        });
    }
    
    /**
     * Manipula erro no cadastro
     */
    private void handleCadastroError(String message) {
        Platform.runLater(() -> {
            setCadastroInProgress(false);
            showStatus(message, "error");
        });
    }
    
    /**
     * Define estado de cadastro em progresso
     */
    private void setCadastroInProgress(boolean inProgress) {
        cadastrarButton.setDisable(inProgress);
        voltarButton.setDisable(inProgress);
        tipoUsuarioCombo.setDisable(inProgress);
        nomeField.setDisable(inProgress);
        emailField.setDisable(inProgress);
        senhaField.setDisable(inProgress);
        confirmarSenhaField.setDisable(inProgress);
        cargoField.setDisable(inProgress);
        departamentoField.setDisable(inProgress);
        telefoneField.setDisable(inProgress);
        cpfField.setDisable(inProgress);
        dataNascimentoPicker.setDisable(inProgress);
        enderecoField.setDisable(inProgress);
        cepField.setDisable(inProgress);
        progressIndicator.setVisible(inProgress);
        
        if (inProgress) {
            showStatus("Cadastrando usuário...", "info");
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
     * Volta para tela de login
     */
    @FXML
    private void handleVoltar() {
        try {
            // Navega para a tela de login
            Stage currentStage = (Stage) voltarButton.getScene().getWindow();
            Scene loginScene = new Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/login.fxml")));
            currentStage.setScene(loginScene);
            currentStage.setTitle("Shiftly - Login");
        } catch (Exception e) {
            logger.error("Erro ao navegar para login: {}", e.getMessage(), e);
        }
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
    
    // =============================================================================
    // VALIDAÇÕES
    // =============================================================================
    
    /**
     * Valida formato do email
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Valida formato do CPF
     */
    private boolean isValidCPF(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");
        
        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }
        
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Validação dos dígitos verificadores
        try {
            int[] digits = cpf.chars().map(Character::getNumericValue).toArray();
            
            // Primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += digits[i] * (10 - i);
            }
            int remainder = sum % 11;
            int firstDigit = remainder < 2 ? 0 : 11 - remainder;
            
            if (digits[9] != firstDigit) {
                return false;
            }
            
            // Segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += digits[i] * (11 - i);
            }
            remainder = sum % 11;
            int secondDigit = remainder < 2 ? 0 : 11 - remainder;
            
            return digits[10] == secondDigit;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Valida formato do CEP
     */
    private boolean isValidCEP(String cep) {
        // Remove caracteres não numéricos
        cep = cep.replaceAll("[^0-9]", "");
        
        // Verifica se tem 8 dígitos
        return cep.length() == 8;
    }
    
    /**
     * Valida formato do telefone
     */
    private boolean isValidTelefone(String telefone) {
        // Remove caracteres não numéricos
        telefone = telefone.replaceAll("[^0-9]", "");
        
        // Verifica se tem 10 ou 11 dígitos (com DDD)
        return telefone.length() >= 10 && telefone.length() <= 11;
    }
}
