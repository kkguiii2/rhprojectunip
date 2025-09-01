package com.shiftly.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Classe que representa uma solicitação de férias
 */
public class Ferias {
    
    private Long id;
    
    @NotNull(message = "Usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;
    
    @NotNull(message = "Data de fim é obrigatória")
    private LocalDate dataFim;
    
    @NotNull(message = "Status é obrigatório")
    private StatusFerias status;
    
    private String observacoes;
    private String motivoRecusa;
    
    // Dados de aprovação/recusa
    private Long aprovadoPorUsuarioId;
    private LocalDateTime dataAprovacao;
    
    // Controle de datas
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAtualizacao;
    
    // Construtor padrão
    public Ferias() {
        this.status = StatusFerias.PENDENTE;
        this.dataSolicitacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Construtor com parâmetros principais
    public Ferias(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        this();
        this.usuarioId = usuarioId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }
    
    // Construtor completo
    public Ferias(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, String observacoes) {
        this(usuarioId, dataInicio, dataFim);
        this.observacoes = observacoes;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public StatusFerias getStatus() {
        return status;
    }
    
    public void setStatus(StatusFerias status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getMotivoRecusa() {
        return motivoRecusa;
    }
    
    public void setMotivoRecusa(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Long getAprovadoPorUsuarioId() {
        return aprovadoPorUsuarioId;
    }
    
    public void setAprovadoPorUsuarioId(Long aprovadoPorUsuarioId) {
        this.aprovadoPorUsuarioId = aprovadoPorUsuarioId;
    }
    
    public LocalDateTime getDataAprovacao() {
        return dataAprovacao;
    }
    
    public void setDataAprovacao(LocalDateTime dataAprovacao) {
        this.dataAprovacao = dataAprovacao;
    }
    
    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }
    
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    
    // Métodos utilitários
    public long getDuracaoDias() {
        if (dataInicio != null && dataFim != null) {
            return ChronoUnit.DAYS.between(dataInicio, dataFim) + 1; // +1 para incluir o último dia
        }
        return 0;
    }
    
    public boolean isPendente() {
        return StatusFerias.PENDENTE.equals(status);
    }
    
    public boolean isAprovada() {
        return StatusFerias.APROVADA.equals(status);
    }
    
    public boolean isRecusada() {
        return StatusFerias.RECUSADA.equals(status);
    }
    
    public boolean isCancelada() {
        return StatusFerias.CANCELADA.equals(status);
    }
    
    public void aprovar(Long usuarioRhId) {
        this.status = StatusFerias.APROVADA;
        this.aprovadoPorUsuarioId = usuarioRhId;
        this.dataAprovacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.motivoRecusa = null; // Limpa motivo de recusa anterior se houver
    }
    
    public void recusar(Long usuarioRhId, String motivo) {
        this.status = StatusFerias.RECUSADA;
        this.aprovadoPorUsuarioId = usuarioRhId;
        this.dataAprovacao = LocalDateTime.now();
        this.motivoRecusa = motivo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public void cancelar() {
        this.status = StatusFerias.CANCELADA;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public boolean isValidaPeriodo() {
        return dataInicio != null && dataFim != null && 
               !dataFim.isBefore(dataInicio) &&
               !dataInicio.isBefore(LocalDate.now());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ferias ferias = (Ferias) o;
        return Objects.equals(id, ferias.id) && 
               Objects.equals(usuarioId, ferias.usuarioId) && 
               Objects.equals(dataInicio, ferias.dataInicio) && 
               Objects.equals(dataFim, ferias.dataFim);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId, dataInicio, dataFim);
    }
    
    @Override
    public String toString() {
        return "Ferias{" +
               "id=" + id +
               ", usuarioId=" + usuarioId +
               ", dataInicio=" + dataInicio +
               ", dataFim=" + dataFim +
               ", status=" + status +
               ", duracaoDias=" + getDuracaoDias() +
               '}';
    }
}
