package com.shiftly.model;

/**
 * Enum que define os tipos de usuário no sistema Shiftly
 */
public enum TipoUsuario {
    
    COLABORADOR("Colaborador", "Funcionário comum com acesso básico"),
    RH("RH", "Recursos Humanos com acesso administrativo"),
    ADMIN("Administrador", "Administrador do sistema com acesso total");
    
    private final String nome;
    private final String descricao;
    
    TipoUsuario(String nome, String descricao) {
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
