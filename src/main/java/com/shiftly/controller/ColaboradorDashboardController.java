package com.shiftly.controller;

import com.shiftly.model.*;
import com.shiftly.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.animation.Timeline;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller para dashboard do colaborador
 */
public class ColaboradorDashboardController extends BaseController {
    
    // Serviços
    private PontoService pontoService;
    private FeriasService feriasService;
    private HorasExtrasService horasExtrasService;
    @SuppressWarnings("unused") // Será utilizado em futuras implementações
    private ComprovanteService comprovanteService;
    
    // Elementos da interface - Header
    @FXML private Label welcomeLabel;
    @FXML private Label userNameLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Button logoutButton;
    
    // Elementos da interface - Quick Actions
    @FXML private Button registrarPontoButton;
    @FXML private Button solicitarFeriasButton;
    @FXML private Button registrarHorasExtrasButton;
    @FXML private Button verComprovantesButton;
    
    // Elementos da interface - Status Cards
    @FXML private Label horasTrabalhadasHojeLabel;
    @FXML private Label saldoHorasExtrasLabel;
    @FXML private Label diasFeriasDisponivelLabel;
    @FXML private Label ultimoPontoLabel;
    
    // Elementos da interface - Histórico de Pontos
    @FXML private TableView<Ponto> pontosTable;
    @FXML private TableColumn<Ponto, LocalDateTime> dataHoraColumn;
    @FXML private TableColumn<Ponto, String> tipoPontoColumn;
    @FXML private TableColumn<Ponto, String> statusColumn;
    @FXML private DatePicker dataInicioFilter;
    @FXML private DatePicker dataFimFilter;
    @FXML private Button filtrarPontosButton;
    
    // Elementos da interface - Minhas Solicitações
    @FXML private TableView<Object> solicitacoesTable;
    @FXML private TableColumn<Object, String> tipoSolicitacaoColumn;
    @FXML private TableColumn<Object, LocalDate> dataSolicitacaoColumn;
    @FXML private TableColumn<Object, String> statusSolicitacaoColumn;
    @FXML private TableColumn<Object, String> acoesSolicitacaoColumn;
    
    // Elementos da interface - Gráficos
    @FXML private LineChart<String, Number> horasChart;
    @FXML private PieChart statusPieChart;
    
    // Timer para atualizar horário
    private Timeline timelineTimer;
    
    @Override
    public void initialize() {
        super.initialize();
        
        // Inicializa serviços
        pontoService = new PontoService();
        feriasService = new FeriasService();
        horasExtrasService = new HorasExtrasService();
        comprovanteService = new ComprovanteService();
        
        // Configura interface
        setupInterface();
        
        // Carrega dados
        loadDashboardData();
        
        // Inicia timer para atualizar horário
        startTimeTimer();
        
        logger.info("Dashboard do colaborador inicializado");
    }
    
    /**
     * Configura elementos da interface
     */
    private void setupInterface() {
        // Configura tabela de pontos
        setupPontosTable();
        
        // Configura tabela de solicitações
        setupSolicitacoesTable();
        
        // Configura filtros de data
        dataInicioFilter.setValue(LocalDate.now().minusDays(7));
        dataFimFilter.setValue(LocalDate.now());
        
        // Configura informações do usuário
        updateUserInfo();
    }
    
    /**
     * Configura tabela de pontos
     */
    private void setupPontosTable() {
        dataHoraColumn.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
        dataHoraColumn.setCellFactory(column -> new TableCell<Ponto, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            }
        });
        
        tipoPontoColumn.setCellValueFactory(new PropertyValueFactory<>("tipoPonto"));
        tipoPontoColumn.setCellFactory(column -> new TableCell<Ponto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    TipoPonto tipo = TipoPonto.valueOf(item);
                    setText(tipo.getNome());
                }
            }
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            Ponto ponto = cellData.getValue();
            String status = ponto.getManual() ? "Manual" : 
                           ponto.getFaceValidada() ? "Validado" : "Pendente";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }
    
    /**
     * Configura tabela de solicitações
     */
    private void setupSolicitacoesTable() {
        tipoSolicitacaoColumn.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            String tipo = item instanceof Ferias ? "Férias" : "Horas Extras";
            return new javafx.beans.property.SimpleStringProperty(tipo);
        });
        
        dataSolicitacaoColumn.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            LocalDate data = item instanceof Ferias ? 
                ((Ferias) item).getDataSolicitacao().toLocalDate() :
                ((HorasExtras) item).getDataSolicitacao().toLocalDate();
            return new javafx.beans.property.SimpleObjectProperty<>(data);
        });
        
        statusSolicitacaoColumn.setCellValueFactory(cellData -> {
            Object item = cellData.getValue();
            String status = item instanceof Ferias ? 
                ((Ferias) item).getStatus().getNome() :
                ((HorasExtras) item).getStatus().getNome();
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        
        acoesSolicitacaoColumn.setCellFactory(column -> new TableCell<Object, String>() {
            private final Button editButton = new Button("Editar");
            private final Button cancelButton = new Button("Cancelar");
            
            {
                editButton.setOnAction(e -> editSolicitacao(getTableRow().getItem()));
                cancelButton.setOnAction(e -> cancelSolicitacao(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Object solicitacao = getTableRow().getItem();
                    boolean canEdit = false;
                    boolean canCancel = false;
                    
                    if (solicitacao instanceof Ferias) {
                        Ferias ferias = (Ferias) solicitacao;
                        canEdit = ferias.getStatus().permiteEdicao();
                        canCancel = ferias.getStatus().permiteCancelamento();
                    } else if (solicitacao instanceof HorasExtras) {
                        HorasExtras horas = (HorasExtras) solicitacao;
                        canEdit = horas.getStatus().permiteEdicao();
                        canCancel = horas.getStatus().permiteEdicao();
                    }
                    
                    editButton.setVisible(canEdit);
                    cancelButton.setVisible(canCancel);
                    
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                    if (canEdit) buttons.getChildren().add(editButton);
                    if (canCancel) buttons.getChildren().add(cancelButton);
                    
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
            userNameLabel.setText(currentUser.getNome());
            welcomeLabel.setText("Bem-vindo(a), " + currentUser.getNome().split(" ")[0] + "!");
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
                loadPontosHistory();
                loadSolicitacoes();
                loadCharts();
                return null;
            }
        };
        
        loadTask.setOnSucceeded(e -> logger.info("Dados do dashboard carregados"));
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            logger.error("Erro ao carregar dados do dashboard: {}", exception.getMessage(), exception);
            showError("Erro", "Erro ao carregar dados do dashboard");
        });
        
        runInBackground(loadTask);
    }
    
    /**
     * Carrega dados dos cards de status
     */
    private void loadStatusCards() {
        Long userId = currentUser.getId();
        LocalDate hoje = LocalDate.now();
        
        // Horas trabalhadas hoje
        double horasHoje = pontoService.calcularHorasTrabalhadas(userId, hoje);
        Platform.runLater(() -> horasTrabalhadasHojeLabel.setText(String.format("%.1f h", horasHoje)));
        
        // Saldo de horas extras
        Double saldoHoras = horasExtrasService.calcularSaldoHorasExtras(userId);
        Platform.runLater(() -> saldoHorasExtrasLabel.setText(String.format("%.1f h", saldoHoras)));
        
        // Dias de férias disponíveis
        int diasFerias = feriasService.calcularSaldoFerias(userId);
        Platform.runLater(() -> diasFeriasDisponivelLabel.setText(diasFerias + " dias"));
        
        // Último ponto
        pontoService.buscarUltimoPonto(userId).ifPresentOrElse(
            ponto -> Platform.runLater(() -> {
                String ultimoPonto = ponto.getTipoPonto().getNome() + " - " +
                    ponto.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));
                ultimoPontoLabel.setText(ultimoPonto);
            }),
            () -> Platform.runLater(() -> ultimoPontoLabel.setText("Nenhum registro"))
        );
    }
    
    /**
     * Carrega histórico de pontos
     */
    private void loadPontosHistory() {
        Long userId = currentUser.getId();
        LocalDate inicio = dataInicioFilter.getValue();
        LocalDate fim = dataFimFilter.getValue();
        
        List<Ponto> pontos = pontoService.buscarPontosPorUsuarioEPeriodo(userId, inicio, fim);
        
        Platform.runLater(() -> {
            ObservableList<Ponto> pontosData = FXCollections.observableArrayList(pontos);
            pontosTable.setItems(pontosData);
        });
    }
    
    /**
     * Carrega solicitações (férias e horas extras)
     */
    private void loadSolicitacoes() {
        Long userId = currentUser.getId();
        
        List<Ferias> ferias = feriasService.buscarFeriasPorUsuario(userId);
        List<HorasExtras> horasExtras = horasExtrasService.buscarHorasExtrasPorUsuario(userId);
        
        Platform.runLater(() -> {
            ObservableList<Object> solicitacoes = FXCollections.observableArrayList();
            solicitacoes.addAll(ferias);
            solicitacoes.addAll(horasExtras);
            solicitacoesTable.setItems(solicitacoes);
        });
    }
    
    /**
     * Carrega gráficos
     */
    private void loadCharts() {
        loadHorasChart();
        loadStatusChart();
    }
    
    /**
     * Carrega gráfico de horas trabalhadas
     */
    private void loadHorasChart() {
        Long userId = currentUser.getId();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Horas Trabalhadas");
        
        // Últimos 7 dias
        for (int i = 6; i >= 0; i--) {
            LocalDate data = LocalDate.now().minusDays(i);
            double horas = pontoService.calcularHorasTrabalhadas(userId, data);
            series.getData().add(new XYChart.Data<>(
                data.format(DateTimeFormatter.ofPattern("dd/MM")), horas));
        }
        
        Platform.runLater(() -> {
            horasChart.getData().clear();
            horasChart.getData().add(series);
        });
    }
    
    /**
     * Carrega gráfico de status
     */
    private void loadStatusChart() {
        Long userId = currentUser.getId();
        
        // Conta pontos por status nos últimos 30 dias
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fim = LocalDate.now();
        List<Ponto> pontos = pontoService.buscarPontosPorUsuarioEPeriodo(userId, inicio, fim);
        
        // Declara variáveis como final para uso em lambda
        final int[] contadores = {0, 0, 0}; // [validados, pendentes, manuais]
        
        for (Ponto ponto : pontos) {
            if (ponto.getManual()) {
                contadores[2]++; // manuais
            } else if (ponto.getFaceValidada()) {
                contadores[0]++; // validados
            } else {
                contadores[1]++; // pendentes
            }
        }
        
        Platform.runLater(() -> {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Validados", contadores[0]),
                new PieChart.Data("Pendentes", contadores[1]),
                new PieChart.Data("Manuais", contadores[2])
            );
            statusPieChart.setData(pieChartData);
        });
    }
    
    // Event Handlers
    
    @FXML
    private void handleRegistrarPonto() {
        logger.info("Navegando para tela de registro de ponto para usuário: {}", currentUser.getEmail());
        loadScene("Shiftly - Registrar Ponto", "/fxml/colaborador-registrar-ponto.fxml");
    }
    
    @FXML
    private void handleSolicitarFerias() {
        loadScene("/fxml/solicitar-ferias.fxml", "Solicitar Férias");
    }
    
    @FXML
    private void handleRegistrarHorasExtras() {
        loadScene("/fxml/registrar-horas-extras.fxml", "Registrar Horas Extras");
    }
    
    @FXML
    private void handleVerComprovantes() {
        loadScene("/fxml/comprovantes.fxml", "Meus Comprovantes");
    }
    
    @FXML
    private void handleFiltrarPontos() {
        loadPontosHistory();
    }
    
    @FXML
    private void handleLogout() {
        if (timelineTimer != null) {
            timelineTimer.stop();
        }
        logout(logoutButton);
    }
    
    /**
     * Edita uma solicitação
     */
    private void editSolicitacao(Object solicitacao) {
        if (solicitacao instanceof Ferias) {
            // TODO: Implementar edição de férias
            showInfo("Em Desenvolvimento", "Edição de férias será implementada em breve");
        } else if (solicitacao instanceof HorasExtras) {
            // TODO: Implementar edição de horas extras
            showInfo("Em Desenvolvimento", "Edição de horas extras será implementada em breve");
        }
    }
    
    /**
     * Cancela uma solicitação
     */
    private void cancelSolicitacao(Object solicitacao) {
        boolean confirmed = showConfirmation("Confirmar Cancelamento", 
            "Deseja realmente cancelar esta solicitação?");
        
        if (!confirmed) {
            return;
        }
        
        Task<Boolean> cancelTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                if (solicitacao instanceof Ferias) {
                    Ferias ferias = (Ferias) solicitacao;
                    feriasService.cancelarFerias(ferias.getId(), currentUser.getId());
                    return true;
                } else if (solicitacao instanceof HorasExtras) {
                    HorasExtras horas = (HorasExtras) solicitacao;
                    return horasExtrasService.deletarHorasExtras(horas.getId(), currentUser.getId());
                }
                return false;
            }
        };
        
        cancelTask.setOnSucceeded(e -> {
            if (cancelTask.getValue()) {
                showSuccess("Sucesso", "Solicitação cancelada com sucesso");
                loadSolicitacoes();
            } else {
                showError("Erro", "Erro ao cancelar solicitação");
            }
        });
        
        cancelTask.setOnFailed(e -> {
            Throwable exception = cancelTask.getException();
            logger.error("Erro ao cancelar solicitação: {}", exception.getMessage(), exception);
            showError("Erro", "Erro ao cancelar solicitação: " + exception.getMessage());
        });
        
        runInBackground(cancelTask);
    }
    
    /**
     * Atualiza dashboard
     */
    @FXML
    private void handleRefresh() {
        loadDashboardData();
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
