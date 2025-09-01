package com.shiftly.model;

/**
 * Enum que define os status possíveis para horas extras
 */
public enum StatusHorasExtras {
    
    PENDENTE("Pendente", "Aguardando aprovação do RH"),
    APROVADA("Aprovada", "Horas extras aprovadas pelo RH"),
    RECUSADA("Recusada", "Horas extras recusadas pelo RH");
    
    private final String nome;
    private final String descricao;
    
    StatusHorasExtras(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public boolean permiteEdicao() {
        return this == PENDENTE;
    }
    
    public boolean permiteAprovacao() {
        return this == PENDENTE;
    }
    
    public boolean permitePagamento() {
        return this == APROVADA;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}
