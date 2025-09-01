package com.shiftly.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe que representa um usuário do sistema Shiftly
 */
public class Usuario {
    
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;
    
    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
    private String cpf;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;
    
    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipoUsuario;
    
    private String cargo;
    private String departamento;
    private Double salario;
    private LocalDateTime dataAdmissao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private Boolean ativo;
    
    // Dados para reconhecimento facial (futuro)
    private String faceEncoding; // Base64 ou caminho para arquivo
    
    // Construtor padrão
    public Usuario() {
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Construtor com parâmetros principais
    public Usuario(String nome, String email, String cpf, String senha, TipoUsuario tipoUsuario) {
        this();
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
    
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getCargo() {
        return cargo;
    }
    
    public void setCargo(String cargo) {
        this.cargo = cargo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getDepartamento() {
        return departamento;
    }
    
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Double getSalario() {
        return salario;
    }
    
    public void setSalario(Double salario) {
        this.salario = salario;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDateTime getDataAdmissao() {
        return dataAdmissao;
    }
    
    public void setDataAdmissao(LocalDateTime dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
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
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public String getFaceEncoding() {
        return faceEncoding;
    }
    
    public void setFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Métodos utilitários
    public boolean isRH() {
        return TipoUsuario.RH.equals(tipoUsuario) || TipoUsuario.ADMIN.equals(tipoUsuario);
    }
    
    public boolean isAdmin() {
        return TipoUsuario.ADMIN.equals(tipoUsuario);
    }
    
    public boolean isColaborador() {
        return TipoUsuario.COLABORADOR.equals(tipoUsuario);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && 
               Objects.equals(cpf, usuario.cpf) && 
               Objects.equals(email, usuario.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, cpf, email);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", email='" + email + '\'' +
               ", cpf='" + cpf + '\'' +
               ", tipoUsuario=" + tipoUsuario +
               ", cargo='" + cargo + '\'' +
               ", departamento='" + departamento + '\'' +
               ", ativo=" + ativo +
               '}';
    }
}
