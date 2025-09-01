package com.shiftly.controller;

import com.shiftly.model.Ponto;
import com.shiftly.model.TipoPonto;
import com.shiftly.service.AuthService;
import com.shiftly.service.PontoService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller para tela de registro de ponto
 * Gerencia reconhecimento facial, geolocalização e validações
 */
public class RegistrarPontoController extends BaseController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistrarPontoController.class);
    
    // Services
    private PontoService pontoService;
    private AuthService authService;
    
    // FXML Components - Header
    @FXML private Button voltarButton;
    @FXML private Label dataHoraLabel;
    
    // FXML Components - Status
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator statusIndicator;
    @FXML private Label localizacaoLabel;
    
    // FXML Components - Tipo de Ponto
    @FXML private ToggleButton entradaButton;
    @FXML private ToggleButton saidaButton;
    
    // FXML Components - Reconhecimento Facial
    @FXML private Label cameraStatus;
    @FXML private Rectangle cameraPreview;
    @FXML private Button ativarCameraButton;
    @FXML private Label faceValidationStatus;
    @FXML private ProgressBar faceProgressBar;
    @FXML private Label faceConfidenceLabel;
    
    // FXML Components - Geolocalização
    @FXML private Label coordenadasLabel;
    @FXML private Label precisaoLabel;
    @FXML private Label enderecoLabel;
    @FXML private Button obterLocalizacaoButton;
    @FXML private Label distanciaLabel;
    @FXML private Label validacaoLocalLabel;
    
    // FXML Components - Formulário
    @FXML private TextArea observacoesTextArea;
    @FXML private Button cancelarButton;
    @FXML private Button registrarPontoButton;
    
    // FXML Components - Status do Registro
    @FXML private VBox statusRegistroContainer;
    @FXML private ProgressIndicator registroProgressIndicator;
    @FXML private Label statusRegistroLabel;
    
    // Estado da Tela
    private TipoPonto tipoSelecionado;
    private Double latitude;
    private Double longitude;
    private String endereco;
    private Double precisao;
    private boolean faceValidada = false;
    private double faceConfidence = 0.0;
    private boolean cameraAtiva = false;
    private boolean localizacaoValida = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pontoService = new PontoService();
        authService = new AuthService();
        
        setupComponents();
        setupToggleButtons();
        initializeScreen();
        startPeriodicUpdates();
        
        logger.info("Tela de registro de ponto inicializada");
    }
    
    /**
     * Configura componentes da tela
     */
    private void setupComponents() {
        // Configurar preview da câmera
        cameraPreview.setStyle("-fx-fill: linear-gradient(to bottom, #f8f9fa, #e9ecef);");
        
        // Configurar validações
        validarFormulario();
        
        // Listeners para mudanças
        observacoesTextArea.textProperty().addListener((obs, old, newVal) -> validarFormulario());
    }
    
    /**
     * Configura toggle buttons exclusivos
     */
    private void setupToggleButtons() {
        ToggleGroup toggleGroup = new ToggleGroup();
        entradaButton.setToggleGroup(toggleGroup);
        saidaButton.setToggleGroup(toggleGroup);
        
        toggleGroup.selectedToggleProperty().addListener((obs, old, newToggle) -> {
            if (newToggle != null) {
                if (newToggle == entradaButton) {
                    tipoSelecionado = TipoPonto.ENTRADA;
                } else {
                    tipoSelecionado = TipoPonto.SAIDA;
                }
                validarFormulario();
                logger.debug("Tipo de ponto selecionado: {}", tipoSelecionado);
            }
        });
    }
    
    /**
     * Inicializa estado da tela
     */
    private void initializeScreen() {
        updateDateTime();
        updateStatus("Sistema Online", true);
        obterLocalizacaoAutomaticamente();
    }
    
    /**
     * Inicia atualizações periódicas
     */
    private void startPeriodicUpdates() {
        // Atualizar data/hora a cada segundo
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Atualiza data e hora atual
     */
    private void updateDateTime() {
        Platform.runLater(() -> {
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss"));
            dataHoraLabel.setText(dateTime);
        });
    }
    
    /**
     * Atualiza status do sistema
     */
    private void updateStatus(String message, boolean online) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusIndicator.setProgress(online ? 1.0 : -1.0);
            statusIndicator.getStyleClass().clear();
            statusIndicator.getStyleClass().add(online ? "status-online" : "status-offline");
        });
    }
    
    /**
     * Valida se formulário está completo
     */
    private void validarFormulario() {
        boolean formValido = tipoSelecionado != null && 
                            localizacaoValida && 
                            faceValidada;
        
        Platform.runLater(() -> {
            registrarPontoButton.setDisable(!formValido);
            
            if (formValido) {
                registrarPontoButton.setText("✅ REGISTRAR PONTO");
                registrarPontoButton.getStyleClass().clear();
                registrarPontoButton.getStyleClass().add("button-success");
            } else {
                registrarPontoButton.setText("📍 REGISTRAR PONTO");
                registrarPontoButton.getStyleClass().clear();
                registrarPontoButton.getStyleClass().add("button-primary");
            }
        });
    }
    
    // ===== EVENT HANDLERS =====
    
    @FXML
    private void handleVoltar() {
        logger.info("Voltando para dashboard");
        
        // Navegar de acordo com o tipo de usuário
        // TODO: Implementar verificação de tipo de usuário
        if (false) { // Temporário
            loadScene("Shiftly - Painel RH", "/fxml/rh-dashboard.fxml");
        } else {
            loadScene("Shiftly - Meu Painel", "/fxml/colaborador-dashboard.fxml");
        }
    }
    
    @FXML
    private void handleTipoSelecionado() {
        // Lógica já está no listener do ToggleGroup
        logger.debug("Tipo de ponto selecionado via botão");
    }
    
    @FXML
    private void handleAtivarCamera() {
        if (!cameraAtiva) {
            iniciarCamera();
        } else {
            pararCamera();
        }
    }
    
    @FXML
    private void handleObterLocalizacao() {
        obterLocalizacaoManualmente();
    }
    
    @FXML
    private void handleCancelar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelar Registro");
        alert.setHeaderText("Deseja cancelar o registro de ponto?");
        alert.setContentText("Todas as informações serão perdidas.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                handleVoltar();
            }
        });
    }
    
    @FXML
    private void handleRegistrarPonto() {
        if (!validarAntesRegistro()) {
            return;
        }
        
        setRegistroInProgress(true);
        
        Task<Boolean> registroTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Validando dados...");
                Thread.sleep(500); // Simula validação
                
                updateMessage("Processando reconhecimento facial...");
                Thread.sleep(1000); // Simula processamento
                
                updateMessage("Verificando localização...");
                Thread.sleep(500); // Simula verificação
                
                updateMessage("Registrando ponto...");
                
                // Criar o ponto
                Ponto ponto = new Ponto();
                ponto.setUsuarioId(1L); // TODO: Obter usuário atual
                ponto.setDataHora(LocalDateTime.now());
                ponto.setTipoPonto(tipoSelecionado);
                ponto.setLatitude(latitude);
                ponto.setLongitude(longitude);
                ponto.setEndereco(endereco);
                ponto.setPrecisao(precisao);
                ponto.setFaceMatch(String.valueOf(faceConfidence));
                ponto.setFaceValidada(faceValidada);
                ponto.setObservacoes(observacoesTextArea.getText().trim());
                
                // Salvar no banco
                Ponto pontoSalvo = pontoService.salvarPonto(ponto);
                
                updateMessage("Ponto registrado com sucesso!");
                Thread.sleep(1000);
                
                return pontoSalvo != null;
            }
            
            @Override
            protected void succeeded() {
                setRegistroInProgress(false);
                
                if (getValue()) {
                    showAlert(Alert.AlertType.INFORMATION, "Ponto Registrado!", 
                        "Seu ponto foi registrado com sucesso!\n\n" +
                        "Tipo: " + tipoSelecionado + "\n" +
                        "Horário: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n" +
                        "Local: " + (endereco != null ? endereco : "Coordenadas registradas"));
                    
                    handleVoltar();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erro no Registro", "Não foi possível registrar o ponto. Tente novamente.");
                }
            }
            
            @Override
            protected void failed() {
                setRegistroInProgress(false);
                logger.error("Erro ao registrar ponto", getException());
                showAlert(Alert.AlertType.ERROR, "Erro no Registro", "Erro interno: " + getException().getMessage());
            }
        };
        
        // Bind da mensagem
        statusRegistroLabel.textProperty().bind(registroTask.messageProperty());
        
        // Executar task
        new Thread(registroTask).start();
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Valida dados antes do registro
     */
    private boolean validarAntesRegistro() {
        if (tipoSelecionado == null) {
            showAlert(Alert.AlertType.WARNING, "Validação", "Selecione o tipo de ponto (Entrada ou Saída).");
            return false;
        }
        
        if (!localizacaoValida) {
            showAlert(Alert.AlertType.WARNING, "Validação", "Obtenha uma localização válida antes de registrar o ponto.");
            return false;
        }
        
        if (!faceValidada) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação");
            alert.setHeaderText("Reconhecimento facial não validado");
            alert.setContentText("Deseja registrar o ponto manualmente?\n\n" +
                "ATENÇÃO: Pontos manuais podem necessitar aprovação posterior.");
            
            return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        }
        
        return true;
    }
    
    /**
     * Define estado de registro em progresso
     */
    private void setRegistroInProgress(boolean inProgress) {
        Platform.runLater(() -> {
            statusRegistroContainer.setVisible(inProgress);
            registrarPontoButton.setDisable(inProgress);
            cancelarButton.setDisable(inProgress);
            
            if (inProgress) {
                registroProgressIndicator.setProgress(-1.0);
            }
        });
    }
    
    /**
     * Inicia câmera para reconhecimento facial
     */
    private void iniciarCamera() {
        logger.info("Iniciando câmera...");
        
        Task<Boolean> cameraTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Simula inicialização da câmera
                updateMessage("Inicializando câmera...");
                Thread.sleep(2000);
                
                updateMessage("Câmera ativa");
                return true;
            }
            
            @Override
            protected void succeeded() {
                if (getValue()) {
                    cameraAtiva = true;
                    Platform.runLater(() -> {
                        cameraStatus.setText("📷 Câmera Ativa");
                        cameraPreview.setStyle("-fx-fill: linear-gradient(to bottom, #28a745, #20c997);");
                        ativarCameraButton.setText("Parar Câmera");
                        faceValidationStatus.setText("🟡 Processando...");
                    });
                    
                    // Simular reconhecimento facial
                    simularReconhecimentoFacial();
                }
            }
            
            @Override
            protected void failed() {
                logger.error("Erro ao iniciar câmera", getException());
                showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível acessar a câmera: " + getException().getMessage());
            }
        };
        
        new Thread(cameraTask).start();
    }
    
    /**
     * Para câmera
     */
    private void pararCamera() {
        cameraAtiva = false;
        faceValidada = false;
        faceConfidence = 0.0;
        
        Platform.runLater(() -> {
            cameraStatus.setText("📷 Câmera Inativa");
            cameraPreview.setStyle("-fx-fill: linear-gradient(to bottom, #f8f9fa, #e9ecef);");
            ativarCameraButton.setText("Ativar Câmera");
            faceValidationStatus.setText("🔴 Aguardando");
            faceProgressBar.setProgress(0.0);
            faceConfidenceLabel.setText("Confiança: 0%");
        });
        
        validarFormulario();
    }
    
    /**
     * Simula processo de reconhecimento facial
     */
    private void simularReconhecimentoFacial() {
        Task<Boolean> faceTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Simula processamento gradual
                for (int i = 0; i <= 100; i += 5) {
                    if (isCancelled()) break;
                    
                    final int progress = i;
                    Platform.runLater(() -> {
                        faceProgressBar.setProgress(progress / 100.0);
                        faceConfidenceLabel.setText("Confiança: " + progress + "%");
                    });
                    
                    Thread.sleep(100);
                }
                
                // Simula resultado (85% de confiança)
                faceConfidence = 0.85 + (Math.random() * 0.10); // 85-95%
                return faceConfidence >= 0.85;
            }
            
            @Override
            protected void succeeded() {
                if (getValue()) {
                    faceValidada = true;
                    Platform.runLater(() -> {
                        faceValidationStatus.setText("✅ Validado");
                        faceConfidenceLabel.setText(String.format("Confiança: %.1f%%", faceConfidence * 100));
                        faceProgressBar.getStyleClass().add("progress-success");
                    });
                } else {
                    Platform.runLater(() -> {
                        faceValidationStatus.setText("❌ Falha na validação");
                        faceProgressBar.getStyleClass().add("progress-error");
                    });
                }
                
                validarFormulario();
            }
        };
        
        new Thread(faceTask).start();
    }
    
    /**
     * Obtém localização automaticamente
     */
    private void obterLocalizacaoAutomaticamente() {
        Platform.runLater(() -> {
            localizacaoLabel.setText("📍 Obtendo localização...");
        });
        
        obterLocalizacao(false);
    }
    
    /**
     * Obtém localização manualmente
     */
    private void obterLocalizacaoManualmente() {
        obterLocalizacao(true);
    }
    
    /**
     * Processo de obtenção de localização
     */
    private void obterLocalizacao(boolean manual) {
        if (manual) {
            Platform.runLater(() -> {
                obterLocalizacaoButton.setDisable(true);
                obterLocalizacaoButton.setText("🔄 Obtendo...");
            });
        }
        
        Task<Boolean> locationTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Simula obtenção de localização
                Thread.sleep(2000);
                
                // Coordenadas simuladas (Av. Paulista, São Paulo)
                latitude = -23.550520 + (Math.random() - 0.5) * 0.001;
                longitude = -46.633309 + (Math.random() - 0.5) * 0.001;
                precisao = 5.0 + Math.random() * 15.0; // 5-20 metros
                endereco = "Av. Paulista, 1000 - Bela Vista, São Paulo - SP";
                
                return true;
            }
            
            @Override
            protected void succeeded() {
                if (getValue()) {
                    // Calcular distância da empresa (simulado)
                    double distanciaEmpresa = 25.0 + Math.random() * 50.0; // 25-75 metros
                    localizacaoValida = distanciaEmpresa <= 100.0; // Raio de 100m
                    
                    Platform.runLater(() -> {
                        coordenadasLabel.setText(String.format("Lat: %.6f, Long: %.6f", latitude, longitude));
                        precisaoLabel.setText(String.format("%.1f metros", precisao));
                        enderecoLabel.setText(endereco);
                        distanciaLabel.setText(String.format("🎯 Distância: %.1f metros", distanciaEmpresa));
                        
                        if (localizacaoValida) {
                            validacaoLocalLabel.setText("✅ Local Válido");
                            validacaoLocalLabel.getStyleClass().clear();
                            validacaoLocalLabel.getStyleClass().add("validation-success");
                            localizacaoLabel.setText("📍 Localização obtida");
                        } else {
                            validacaoLocalLabel.setText("⚠️ Fora do raio permitido");
                            validacaoLocalLabel.getStyleClass().clear();
                            validacaoLocalLabel.getStyleClass().add("validation-warning");
                            localizacaoLabel.setText("📍 Localização fora do raio");
                        }
                        
                        if (manual) {
                            obterLocalizacaoButton.setDisable(false);
                            obterLocalizacaoButton.setText("🔄 Atualizar Localização");
                        }
                    });
                    
                    validarFormulario();
                }
            }
            
            @Override
            protected void failed() {
                logger.error("Erro ao obter localização", getException());
                
                Platform.runLater(() -> {
                    localizacaoLabel.setText("📍 Erro ao obter localização");
                    validacaoLocalLabel.setText("❌ Erro na localização");
                    
                    if (manual) {
                        obterLocalizacaoButton.setDisable(false);
                        obterLocalizacaoButton.setText("🔄 Tentar Novamente");
                    }
                });
            }
        };
        
        new Thread(locationTask).start();
    }
    
    /**
     * Exibe alertas para o usuário
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
