package com.shiftly.model;

/**
 * Enum que define os tipos de comprovante de pagamento
 */
public enum TipoComprovante {
    
    SALARIO("Salário", "Comprovante de pagamento de salário mensal"),
    HORAS_EXTRAS("Horas Extras", "Comprovante de pagamento de horas extras"),
    FERIAS("Férias", "Comprovante de pagamento de férias"),
    DECIMO_TERCEIRO("13º Salário", "Comprovante de pagamento do décimo terceiro salário"),
    OUTROS("Outros", "Outros tipos de comprovante");
    
    private final String nome;
    private final String descricao;
    
    TipoComprovante(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}
