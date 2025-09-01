package com.shiftly.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa o controle de horas extras de um usuário
 */
public class HorasExtras {
    
    private Long id;
    
    @NotNull(message = "Usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "Data é obrigatória")
    private LocalDate data;
    
    @NotNull(message = "Quantidade de horas é obrigatória")
    @Positive(message = "Quantidade de horas deve ser positiva")
    private Double horas;
    
    @NotNull(message = "Status é obrigatório")
    private StatusHorasExtras status;
    
    private String descricao;
    private String justificativa;
    private String motivoRecusa;
    
    // Dados de aprovação/recusa
    private Long aprovadoPorUsuarioId;
    private LocalDateTime dataAprovacao;
    
    // Dados de pagamento
    private Boolean pago;
    private LocalDate dataPagamento;
    private Double valorPago;
    
    // Controle de datas
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAtualizacao;
    
    // Construtor padrão
    public HorasExtras() {
        this.status = StatusHorasExtras.PENDENTE;
        this.pago = false;
        this.dataSolicitacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Construtor com parâmetros principais
    public HorasExtras(Long usuarioId, LocalDate data, Double horas) {
        this();
        this.usuarioId = usuarioId;
        this.data = data;
        this.horas = horas;
    }
    
    // Construtor completo
    public HorasExtras(Long usuarioId, LocalDate data, Double horas, String descricao, String justificativa) {
        this(usuarioId, data, horas);
        this.descricao = descricao;
        this.justificativa = justificativa;
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
    
    public LocalDate getData() {
        return data;
    }
    
    public void setData(LocalDate data) {
        this.data = data;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getHoras() {
        return horas;
    }
    
    public void setHoras(Double horas) {
        this.horas = horas;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public StatusHorasExtras getStatus() {
        return status;
    }
    
    public void setStatus(StatusHorasExtras status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getJustificativa() {
        return justificativa;
    }
    
    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
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
    
    public Boolean getPago() {
        return pago;
    }
    
    public void setPago(Boolean pago) {
        this.pago = pago;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDate getDataPagamento() {
        return dataPagamento;
    }
    
    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getValorPago() {
        return valorPago;
    }
    
    public void setValorPago(Double valorPago) {
        this.valorPago = valorPago;
        this.dataAtualizacao = LocalDateTime.now();
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
    public boolean isPendente() {
        return StatusHorasExtras.PENDENTE.equals(status);
    }
    
    public boolean isAprovada() {
        return StatusHorasExtras.APROVADA.equals(status);
    }
    
    public boolean isRecusada() {
        return StatusHorasExtras.RECUSADA.equals(status);
    }
    
    public boolean isPaga() {
        return Boolean.TRUE.equals(pago);
    }
    
    public void aprovar(Long usuarioRhId) {
        this.status = StatusHorasExtras.APROVADA;
        this.aprovadoPorUsuarioId = usuarioRhId;
        this.dataAprovacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.motivoRecusa = null; // Limpa motivo de recusa anterior se houver
    }
    
    public void recusar(Long usuarioRhId, String motivo) {
        this.status = StatusHorasExtras.RECUSADA;
        this.aprovadoPorUsuarioId = usuarioRhId;
        this.dataAprovacao = LocalDateTime.now();
        this.motivoRecusa = motivo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public void marcarComoPago(Double valor) {
        this.pago = true;
        this.valorPago = valor;
        this.dataPagamento = LocalDate.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double calcularValor(Double valorHora) {
        if (valorHora != null && horas != null) {
            // Horas extras normalmente são pagas com 50% de adicional
            return horas * valorHora * 1.5;
        }
        return 0.0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HorasExtras that = (HorasExtras) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(usuarioId, that.usuarioId) && 
               Objects.equals(data, that.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId, data);
    }
    
    @Override
    public String toString() {
        return "HorasExtras{" +
               "id=" + id +
               ", usuarioId=" + usuarioId +
               ", data=" + data +
               ", horas=" + horas +
               ", status=" + status +
               ", pago=" + pago +
               '}';
    }
}
