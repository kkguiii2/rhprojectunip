package com.shiftly.controller;

import com.shiftly.model.*;
import com.shiftly.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller para dashboard do RH
 */
public class RhDashboardController extends BaseController {
    
    // Serviços
    private UsuarioService usuarioService;
    private PontoService pontoService;
    private FeriasService feriasService;
    private HorasExtrasService horasExtrasService;
    
    // Elementos da interface - Header
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Button logoutButton;
    
    // Elementos da interface - Quick Actions
    @FXML private Button gerenciarUsuariosButton;
    @FXML private Button gerarRelatoriosButton;
    @FXML private Button aprovarSolicitacoesButton;
    @FXML private Button configurarSistemaButton;
    
    // Elementos da interface - Status Cards
    @FXML private Label totalColaboradoresLabel;
    @FXML private Label presentesHojeLabel;
    @FXML private Label solicitacoesPendentesLabel;
    @FXML private Label horasExtrasLabel;
    
    // Elementos da interface - Tabelas
    @FXML private TableView<Object> solicitacoesTable;
    @FXML private TableColumn<Object, String> colaboradorColumn;
    @FXML private TableColumn<Object, String> tipoSolicitacaoColumn;
    @FXML private TableColumn<Object, String> dataSolicitacaoColumn;
    @FXML private TableColumn<Object, String> statusSolicitacaoColumn;
    @FXML private TableColumn<Object, String> acoesSolicitacaoColumn;
    
    @FXML private ListView<String> atividadeRecenteList;
    
    // Timer para atualizar horário
    private javafx.animation.Timeline timelineTimer;
    
    @Override
    public void initialize() {
        super.initialize();
        
        // Inicializa serviços
        usuarioService = new UsuarioService();
        pontoService = new PontoService();
        feriasService = new FeriasService();
        horasExtrasService = new HorasExtrasService();
        
        // Configura interface
        setupInterface();
        
        // Carrega dados
        loadDashboardData();
        
        // Inicia timer para atualizar horário
        startTimeTimer();
        
        logger.info("Dashboard do RH inicializado");
    }
    
    /**
     * Configura elementos da interface
     */
    private void setupInterface() {
        // Configura tabela de solicitações
        setupSolicitacoesTable();
        
        // Configura informações do usuário
        updateUserInfo();
    }
    
    /**
     * Configura tabela de solicitações
     */
    private void setupSolicitacoesTable() {
        colaboradorColumn.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            String colaborador = "N/A";
            if (item instanceof Ferias) {
                // TODO: Buscar nome do colaborador pelo ID
                colaborador = "Colaborador";
            } else if (item instanceof HorasExtras) {
                // TODO: Buscar nome do colaborador pelo ID
                colaborador = "Colaborador";
            }
            return new javafx.beans.property.SimpleStringProperty(colaborador);
        });
        
        tipoSolicitacaoColumn.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            String tipo = item instanceof Ferias ? "Férias" : "Horas Extras";
            return new javafx.beans.property.SimpleStringProperty(tipo);
        });
        
        dataSolicitacaoColumn.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            LocalDateTime data = item instanceof Ferias ? 
                ((Ferias) item).getDataSolicitacao() :
                ((HorasExtras) item).getDataSolicitacao();
            String dataFormatada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return new javafx.beans.property.SimpleStringProperty(dataFormatada);
        });
        
        statusSolicitacaoColumn.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            String status = item instanceof Ferias ? 
                ((Ferias) item).getStatus().getNome() :
                ((HorasExtras) item).getStatus().getNome();
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        
        acoesSolicitacaoColumn.setCellFactory(column -> new TableCell<Object, String>() {
            private final Button aprovarButton = new Button("Aprovar");
            private final Button rejeitarButton = new Button("Rejeitar");
            
            {
                aprovarButton.setOnAction(e -> aprovarSolicitacao(getTableRow().getItem()));
                rejeitarButton.setOnAction(e -> rejeitarSolicitacao(getTableRow().getItem()));
                aprovarButton.getStyleClass().add("success");
                rejeitarButton.getStyleClass().add("danger");
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                    buttons.getChildren().addAll(aprovarButton, rejeitarButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    /**
     * Atualiza informações do usuário
     */
    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText("Bem-vindo(a), " + currentUser.getNome() + "!");
        }
    }
    
    /**
     * Inicia timer para atualizar horário
     */
    private void startTimeTimer() {
        timelineTimer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(1),
                e -> updateCurrentTime()
            )
        );
        timelineTimer.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timelineTimer.play();
    }
    
    /**
     * Atualiza horário atual
     */
    private void updateCurrentTime() {
        Platform.runLater(() -> {
            LocalDateTime now = LocalDateTime.now();
            currentTimeLabel.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        });
    }
    
    /**
     * Carrega dados do dashboard
     */
    private void loadDashboardData() {
        if (!checkAuthentication(welcomeLabel)) {
            return;
        }
        
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadStatusCards();
                loadSolicitacoesPendentes();
                loadAtividadeRecente();
                return null;
            }
        };
        
        loadTask.setOnSucceeded(e -> logger.info("Dados do dashboard RH carregados"));
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            logger.error("Erro ao carregar dados do dashboard RH: {}", exception.getMessage(), exception);
            showError("Erro", "Erro ao carregar dados do dashboard");
        });
        
        runInBackground(loadTask);
    }
    
    /**
     * Carrega dados dos cards de status
     */
    private void loadStatusCards() {
        // Dados simulados para demonstração
        Platform.runLater(() -> {
            totalColaboradoresLabel.setText("25");
            presentesHojeLabel.setText("20");
            solicitacoesPendentesLabel.setText("3");
            horasExtrasLabel.setText("128h");
        });
    }
    
    /**
     * Carrega solicitações pendentes
     */
    private void loadSolicitacoesPendentes() {
        // Dados simulados para demonstração
        Platform.runLater(() -> {
            ObservableList<Object> solicitacoes = FXCollections.observableArrayList();
            // Por enquanto, mostra tabela vazia
            solicitacoesTable.setItems(solicitacoes);
        });
    }
    
    /**
     * Carrega atividade recente
     */
    private void loadAtividadeRecente() {
        Platform.runLater(() -> {
            ObservableList<String> atividades = FXCollections.observableArrayList();
            atividades.add("João Santos registrou ponto de entrada às 08:00");
            atividades.add("Maria Silva solicitou férias para dezembro");
            atividades.add("Pedro Costa registrou 2h de hora extra");
            atividades.add("Ana Oliveira registrou ponto de saída às 18:00");
            atividades.add("Sistema sincronizado com sucesso");
            atividadeRecenteList.setItems(atividades);
        });
    }
    
    // Event Handlers
    
    @FXML
    private void handleGerenciarUsuarios() {
        showInfo("Em Desenvolvimento", "Funcionalidade de gerenciar usuários será implementada em breve");
    }
    
    @FXML
    private void handleGerarRelatorios() {
        showInfo("Em Desenvolvimento", "Funcionalidade de relatórios será implementada em breve");
    }
    
    @FXML
    private void handleAprovarSolicitacoes() {
        loadSolicitacoesPendentes();
    }
    
    @FXML
    private void handleConfigurarSistema() {
        showInfo("Em Desenvolvimento", "Funcionalidade de configuração será implementada em breve");
    }
    
    @FXML
    private void handleLogout() {
        if (timelineTimer != null) {
            timelineTimer.stop();
        }
        logout(logoutButton);
    }
    
    /**
     * Aprova uma solicitação
     */
    private void aprovarSolicitacao(Object solicitacao) {
        if (solicitacao == null) return;
        
        boolean confirmed = showConfirmation("Confirmar Aprovação", 
            "Deseja realmente aprovar esta solicitação?");
        
        if (!confirmed) return;
        
        Task<Boolean> aprovarTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Simulação de aprovação
                Thread.sleep(1000); // Simula processamento
                return true;
            }
        };
        
        aprovarTask.setOnSucceeded(e -> {
            if (aprovarTask.getValue()) {
                showSuccess("Sucesso", "Solicitação aprovada com sucesso");
                loadSolicitacoesPendentes();
                loadStatusCards();
            } else {
                showError("Erro", "Erro ao aprovar solicitação");
            }
        });
        
        aprovarTask.setOnFailed(e -> {
            Throwable exception = aprovarTask.getException();
            logger.error("Erro ao aprovar solicitação: {}", exception.getMessage(), exception);
            showError("Erro", "Erro ao aprovar solicitação: " + exception.getMessage());
        });
        
        runInBackground(aprovarTask);
    }
    
    /**
     * Rejeita uma solicitação
     */
    private void rejeitarSolicitacao(Object solicitacao) {
        if (solicitacao == null) return;
        
        boolean confirmed = showConfirmation("Confirmar Rejeição", 
            "Deseja realmente rejeitar esta solicitação?");
        
        if (!confirmed) return;
        
        Task<Boolean> rejeitarTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Simulação de rejeição
                Thread.sleep(1000); // Simula processamento
                return true;
            }
        };
        
        rejeitarTask.setOnSucceeded(e -> {
            if (rejeitarTask.getValue()) {
                showSuccess("Sucesso", "Solicitação rejeitada com sucesso");
                loadSolicitacoesPendentes();
                loadStatusCards();
            } else {
                showError("Erro", "Erro ao rejeitar solicitação");
            }
        });
        
        rejeitarTask.setOnFailed(e -> {
            Throwable exception = rejeitarTask.getException();
            logger.error("Erro ao rejeitar solicitação: {}", exception.getMessage(), exception);
            showError("Erro", "Erro ao rejeitar solicitação: " + exception.getMessage());
        });
        
        runInBackground(rejeitarTask);
    }
    
    /**
     * Mostra informação
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
