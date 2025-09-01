package com.shiftly.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa um registro de ponto no sistema
 */
public class Ponto {
    
    private Long id;
    
    @NotNull(message = "Usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "Data/hora do ponto é obrigatória")
    private LocalDateTime dataHora;
    
    @NotNull(message = "Tipo de ponto é obrigatório")
    private TipoPonto tipoPonto;
    
    // Dados de geolocalização
    private Double latitude;
    private Double longitude;
    private String endereco;
    private Double precisao; // em metros
    
    // Dados de reconhecimento facial
    private String faceMatch; // Percentual de match ou hash
    private Boolean faceValidada;
    
    // Controle administrativo
    private String observacoes;
    private Boolean manual; // Se foi inserido manualmente pelo RH
    private Long corrigidoPorUsuarioId; // ID do usuário RH que fez correção
    private LocalDateTime dataCorrecao;
    private String motivoCorrecao;
    
    private LocalDateTime dataCriacao;
    
    // Construtor padrão
    public Ponto() {
        this.dataCriacao = LocalDateTime.now();
        this.manual = false;
        this.faceValidada = false;
    }
    
    // Construtor para ponto normal
    public Ponto(Long usuarioId, LocalDateTime dataHora, TipoPonto tipoPonto) {
        this();
        this.usuarioId = usuarioId;
        this.dataHora = dataHora;
        this.tipoPonto = tipoPonto;
    }
    
    // Construtor com geolocalização
    public Ponto(Long usuarioId, LocalDateTime dataHora, TipoPonto tipoPonto, 
                 Double latitude, Double longitude) {
        this(usuarioId, dataHora, tipoPonto);
        this.latitude = latitude;
        this.longitude = longitude;
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
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public TipoPonto getTipoPonto() {
        return tipoPonto;
    }
    
    public void setTipoPonto(TipoPonto tipoPonto) {
        this.tipoPonto = tipoPonto;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getEndereco() {
        return endereco;
    }
    
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    
    public Double getPrecisao() {
        return precisao;
    }
    
    public void setPrecisao(Double precisao) {
        this.precisao = precisao;
    }
    
    public String getFaceMatch() {
        return faceMatch;
    }
    
    public void setFaceMatch(String faceMatch) {
        this.faceMatch = faceMatch;
    }
    
    public Boolean getFaceValidada() {
        return faceValidada;
    }
    
    public void setFaceValidada(Boolean faceValidada) {
        this.faceValidada = faceValidada;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public Boolean getManual() {
        return manual;
    }
    
    public void setManual(Boolean manual) {
        this.manual = manual;
    }
    
    public Long getCorrigidoPorUsuarioId() {
        return corrigidoPorUsuarioId;
    }
    
    public void setCorrigidoPorUsuarioId(Long corrigidoPorUsuarioId) {
        this.corrigidoPorUsuarioId = corrigidoPorUsuarioId;
    }
    
    public LocalDateTime getDataCorrecao() {
        return dataCorrecao;
    }
    
    public void setDataCorrecao(LocalDateTime dataCorrecao) {
        this.dataCorrecao = dataCorrecao;
    }
    
    public String getMotivoCorrecao() {
        return motivoCorrecao;
    }
    
    public void setMotivoCorrecao(String motivoCorrecao) {
        this.motivoCorrecao = motivoCorrecao;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    // Métodos utilitários
    public boolean temGeolocalizacao() {
        return latitude != null && longitude != null;
    }
    
    public boolean foiCorrigido() {
        return corrigidoPorUsuarioId != null;
    }
    
    public void corrigir(Long usuarioRhId, String motivo) {
        this.corrigidoPorUsuarioId = usuarioRhId;
        this.dataCorrecao = LocalDateTime.now();
        this.motivoCorrecao = motivo;
        this.manual = true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ponto ponto = (Ponto) o;
        return Objects.equals(id, ponto.id) && 
               Objects.equals(usuarioId, ponto.usuarioId) && 
               Objects.equals(dataHora, ponto.dataHora);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId, dataHora);
    }
    
    @Override
    public String toString() {
        return "Ponto{" +
               "id=" + id +
               ", usuarioId=" + usuarioId +
               ", dataHora=" + dataHora +
               ", tipoPonto=" + tipoPonto +
               ", manual=" + manual +
               ", faceValidada=" + faceValidada +
               '}';
    }
}
