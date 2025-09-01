package com.shiftly.model;

/**
 * Enum que define os tipos de registro de ponto
 */
public enum TipoPonto {
    
    ENTRADA("Entrada", "Início do expediente"),
    SAIDA_ALMOCO("Saída para Almoço", "Saída para intervalo de almoço"),
    VOLTA_ALMOCO("Volta do Almoço", "Retorno do intervalo de almoço"),
    SAIDA("Saída", "Final do expediente"),
    ENTRADA_EXTRA("Entrada Extra", "Início de hora extra"),
    SAIDA_EXTRA("Saída Extra", "Final de hora extra");
    
    private final String nome;
    private final String descricao;
    
    TipoPonto(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public boolean isEntrada() {
        return this == ENTRADA || this == VOLTA_ALMOCO || this == ENTRADA_EXTRA;
    }
    
    public boolean isSaida() {
        return this == SAIDA || this == SAIDA_ALMOCO || this == SAIDA_EXTRA;
    }
    
    public boolean isHoraExtra() {
        return this == ENTRADA_EXTRA || this == SAIDA_EXTRA;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}
