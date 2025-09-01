package com.shiftly.model;

/**
 * Enum que define os status possíveis para solicitações de férias
 */
public enum StatusFerias {
    
    PENDENTE("Pendente", "Aguardando aprovação do RH"),
    APROVADA("Aprovada", "Férias aprovadas pelo RH"),
    RECUSADA("Recusada", "Férias recusadas pelo RH"),
    CANCELADA("Cancelada", "Férias canceladas pelo solicitante");
    
    private final String nome;
    private final String descricao;
    
    StatusFerias(String nome, String descricao) {
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
    
    public boolean permiteCancelamento() {
        return this == PENDENTE || this == APROVADA;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}
