package com.shiftly.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa um comprovante de pagamento
 */
public class Comprovante {
    
    private Long id;
    
    @NotNull(message = "Usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "Tipo de comprovante é obrigatório")
    private TipoComprovante tipoComprovante;
    
    @NotBlank(message = "Referência é obrigatória")
    private String referencia; // Ex: "Janeiro/2024", "Horas Extras Dezembro/2023"
    
    @NotNull(message = "Data de emissão é obrigatória")
    private LocalDate dataEmissao;
    
    @NotNull(message = "Período inicial é obrigatório")
    private LocalDate periodoInicio;
    
    @NotNull(message = "Período final é obrigatório")
    private LocalDate periodoFim;
    
    @NotNull(message = "Valor bruto é obrigatório")
    @Positive(message = "Valor bruto deve ser positivo")
    private Double valorBruto;
    
    private Double valorDescontos;
    
    @NotNull(message = "Valor líquido é obrigatório")
    @Positive(message = "Valor líquido deve ser positivo")
    private Double valorLiquido;
    
    // Detalhamento do pagamento
    private Double salarioBase;
    private Double horasExtras;
    private Double adicionalNoturno;
    private Double outrosProventos;
    private Double inss;
    private Double irrf;
    private Double valeTransporte;
    private Double valeRefeicao;
    private Double outrosDescontos;
    
    // Arquivo do comprovante
    private String caminhoArquivo; // Caminho para PDF ou imagem
    private String nomeArquivo;
    private Long tamanhoArquivo;
    
    // Controle
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private Long criadoPorUsuarioId; // ID do usuário RH que criou
    
    // Construtor padrão
    public Comprovante() {
        this.valorDescontos = 0.0;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Construtor básico
    public Comprovante(Long usuarioId, TipoComprovante tipoComprovante, String referencia,
                      LocalDate dataEmissao, LocalDate periodoInicio, LocalDate periodoFim,
                      Double valorBruto, Double valorLiquido) {
        this();
        this.usuarioId = usuarioId;
        this.tipoComprovante = tipoComprovante;
        this.referencia = referencia;
        this.dataEmissao = dataEmissao;
        this.periodoInicio = periodoInicio;
        this.periodoFim = periodoFim;
        this.valorBruto = valorBruto;
        this.valorLiquido = valorLiquido;
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
    
    public TipoComprovante getTipoComprovante() {
        return tipoComprovante;
    }
    
    public void setTipoComprovante(TipoComprovante tipoComprovante) {
        this.tipoComprovante = tipoComprovante;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getReferencia() {
        return referencia;
    }
    
    public void setReferencia(String referencia) {
        this.referencia = referencia;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDate getDataEmissao() {
        return dataEmissao;
    }
    
    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }
    
    public void setPeriodoInicio(LocalDate periodoInicio) {
        this.periodoInicio = periodoInicio;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDate getPeriodoFim() {
        return periodoFim;
    }
    
    public void setPeriodoFim(LocalDate periodoFim) {
        this.periodoFim = periodoFim;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getValorBruto() {
        return valorBruto;
    }
    
    public void setValorBruto(Double valorBruto) {
        this.valorBruto = valorBruto;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getValorDescontos() {
        return valorDescontos != null ? valorDescontos : 0.0;
    }
    
    public void setValorDescontos(Double valorDescontos) {
        this.valorDescontos = valorDescontos;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getValorLiquido() {
        return valorLiquido;
    }
    
    public void setValorLiquido(Double valorLiquido) {
        this.valorLiquido = valorLiquido;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getSalarioBase() {
        return salarioBase;
    }
    
    public void setSalarioBase(Double salarioBase) {
        this.salarioBase = salarioBase;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getHorasExtras() {
        return horasExtras;
    }
    
    public void setHorasExtras(Double horasExtras) {
        this.horasExtras = horasExtras;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getAdicionalNoturno() {
        return adicionalNoturno;
    }
    
    public void setAdicionalNoturno(Double adicionalNoturno) {
        this.adicionalNoturno = adicionalNoturno;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getOutrosProventos() {
        return outrosProventos;
    }
    
    public void setOutrosProventos(Double outrosProventos) {
        this.outrosProventos = outrosProventos;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getInss() {
        return inss;
    }
    
    public void setInss(Double inss) {
        this.inss = inss;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getIrrf() {
        return irrf;
    }
    
    public void setIrrf(Double irrf) {
        this.irrf = irrf;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getValeTransporte() {
        return valeTransporte;
    }
    
    public void setValeTransporte(Double valeTransporte) {
        this.valeTransporte = valeTransporte;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getValeRefeicao() {
        return valeRefeicao;
    }
    
    public void setValeRefeicao(Double valeRefeicao) {
        this.valeRefeicao = valeRefeicao;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getOutrosDescontos() {
        return outrosDescontos;
    }
    
    public void setOutrosDescontos(Double outrosDescontos) {
        this.outrosDescontos = outrosDescontos;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }
    
    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getNomeArquivo() {
        return nomeArquivo;
    }
    
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Long getTamanhoArquivo() {
        return tamanhoArquivo;
    }
    
    public void setTamanhoArquivo(Long tamanhoArquivo) {
        this.tamanhoArquivo = tamanhoArquivo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
    
    public Long getCriadoPorUsuarioId() {
        return criadoPorUsuarioId;
    }
    
    public void setCriadoPorUsuarioId(Long criadoPorUsuarioId) {
        this.criadoPorUsuarioId = criadoPorUsuarioId;
    }
    
    // Métodos utilitários
    public Double getTotalProventos() {
        double total = 0.0;
        if (salarioBase != null) total += salarioBase;
        if (horasExtras != null) total += horasExtras;
        if (adicionalNoturno != null) total += adicionalNoturno;
        if (outrosProventos != null) total += outrosProventos;
        return total;
    }
    
    public Double getTotalDescontos() {
        double total = 0.0;
        if (inss != null) total += inss;
        if (irrf != null) total += irrf;
        if (valeTransporte != null) total += valeTransporte;
        if (valeRefeicao != null) total += valeRefeicao;
        if (outrosDescontos != null) total += outrosDescontos;
        return total;
    }
    
    public boolean temArquivo() {
        return caminhoArquivo != null && !caminhoArquivo.trim().isEmpty();
    }
    
    public void calcularValorLiquido() {
        this.valorLiquido = valorBruto - getValorDescontos();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comprovante that = (Comprovante) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(usuarioId, that.usuarioId) && 
               Objects.equals(referencia, that.referencia);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, usuarioId, referencia);
    }
    
    @Override
    public String toString() {
        return "Comprovante{" +
               "id=" + id +
               ", usuarioId=" + usuarioId +
               ", tipoComprovante=" + tipoComprovante +
               ", referencia='" + referencia + '\'' +
               ", valorBruto=" + valorBruto +
               ", valorLiquido=" + valorLiquido +
               '}';
    }
}
