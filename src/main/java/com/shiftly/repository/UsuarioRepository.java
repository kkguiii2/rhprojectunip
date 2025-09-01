package com.shiftly.repository;

import com.shiftly.model.TipoUsuario;
import com.shiftly.model.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar usuários
 */
public class UsuarioRepository extends BaseRepository<Usuario, Long> {
    
    @Override
    public Optional<Usuario> findById(Long id) {
        if (id == null) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                       salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
                FROM usuarios WHERE id = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUsuario(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    @Override
    public List<Usuario> findAll() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                       salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
                FROM usuarios ORDER BY nome
                """;
            
            List<Usuario> usuarios = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
            return usuarios;
        });
    }
    
    @Override
    public Usuario save(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        
        return executeWithConnection(conn -> {
            if (usuario.getId() == null) {
                return insert(conn, usuario);
            } else {
                return update(conn, usuario);
            }
        });
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = "UPDATE usuarios SET ativo = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, false);
                stmt.setLong(2, id);
                
                return stmt.executeUpdate() > 0;
            }
        });
    }
    
    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = "SELECT 1 FROM usuarios WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
    
    @Override
    public long count() {
        return executeWithConnection(conn -> {
            String sql = "SELECT COUNT(*) FROM usuarios WHERE ativo = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        });
    }
    
    /**
     * Busca usuário por email
     */
    public Optional<Usuario> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                       salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
                FROM usuarios WHERE LOWER(email) = LOWER(?) AND ativo = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim());
                stmt.setBoolean(2, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUsuario(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    /**
     * Busca usuário por CPF
     */
    public Optional<Usuario> findByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                       salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
                FROM usuarios WHERE cpf = ? AND ativo = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cpf.trim());
                stmt.setBoolean(2, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUsuario(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    /**
     * Busca usuários por tipo
     */
    public List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario) {
        if (tipoUsuario == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                       salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
                FROM usuarios WHERE tipo_usuario = ? AND ativo = ? ORDER BY nome
                """;
            
            List<Usuario> usuarios = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, tipoUsuario.name());
                stmt.setBoolean(2, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        usuarios.add(mapResultSetToUsuario(rs));
                    }
                }
            }
            return usuarios;
        });
    }
    
    /**
     * Busca usuários por departamento
     */
    public List<Usuario> findByDepartamento(String departamento) {
        if (departamento == null || departamento.trim().isEmpty()) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                       salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
                FROM usuarios WHERE LOWER(departamento) = LOWER(?) AND ativo = ? ORDER BY nome
                """;
            
            List<Usuario> usuarios = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, departamento.trim());
                stmt.setBoolean(2, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        usuarios.add(mapResultSetToUsuario(rs));
                    }
                }
            }
            return usuarios;
        });
    }
    
    /**
     * Verifica se email já existe
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        
        return executeWithConnection(conn -> {
            String sql = "SELECT 1 FROM usuarios WHERE LOWER(email) = LOWER(?) AND ativo = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email.trim());
                stmt.setBoolean(2, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
    
    /**
     * Verifica se CPF já existe
     */
    public boolean existsByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) return false;
        
        return executeWithConnection(conn -> {
            String sql = "SELECT 1 FROM usuarios WHERE cpf = ? AND ativo = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cpf.trim());
                stmt.setBoolean(2, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
    
    /**
     * Busca usuários ativos
     */
    public List<Usuario> findAllAtivos() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                       salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding
                FROM usuarios WHERE ativo = ? ORDER BY nome
                """;
            
            List<Usuario> usuarios = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        usuarios.add(mapResultSetToUsuario(rs));
                    }
                }
            }
            return usuarios;
        });
    }
    
    /**
     * Insere um novo usuário
     */
    private Usuario insert(Connection conn, Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO usuarios (nome, email, cpf, senha, tipo_usuario, cargo, departamento, 
                                salario, data_admissao, data_criacao, data_atualizacao, ativo, face_encoding)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setUsuarioParameters(stmt, usuario);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir usuário");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getLong(1));
                }
            }
        }
        
        return usuario;
    }
    
    /**
     * Atualiza um usuário existente
     */
    private Usuario update(Connection conn, Usuario usuario) throws SQLException {
        String sql = """
            UPDATE usuarios SET nome = ?, email = ?, cpf = ?, senha = ?, tipo_usuario = ?, 
                              cargo = ?, departamento = ?, salario = ?, data_admissao = ?, 
                              data_atualizacao = ?, ativo = ?, face_encoding = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setUsuarioParameters(stmt, usuario);
            stmt.setLong(13, usuario.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Usuário não encontrado para atualização");
            }
        }
        
        return usuario;
    }
    
    /**
     * Define os parâmetros do usuário no PreparedStatement
     */
    private void setUsuarioParameters(PreparedStatement stmt, Usuario usuario) throws SQLException {
        stmt.setString(1, usuario.getNome());
        stmt.setString(2, usuario.getEmail());
        stmt.setString(3, usuario.getCpf());
        stmt.setString(4, usuario.getSenha());
        stmt.setString(5, usuario.getTipoUsuario().name());
        stmt.setString(6, usuario.getCargo());
        stmt.setString(7, usuario.getDepartamento());
        
        if (usuario.getSalario() != null) {
            stmt.setDouble(8, usuario.getSalario());
        } else {
            stmt.setNull(8, Types.DECIMAL);
        }
        
        if (usuario.getDataAdmissao() != null) {
            stmt.setTimestamp(9, Timestamp.valueOf(usuario.getDataAdmissao()));
        } else {
            stmt.setNull(9, Types.TIMESTAMP);
        }
        
        stmt.setTimestamp(10, Timestamp.valueOf(usuario.getDataCriacao()));
        stmt.setTimestamp(11, Timestamp.valueOf(usuario.getDataAtualizacao()));
        stmt.setBoolean(12, usuario.getAtivo());
        stmt.setString(13, usuario.getFaceEncoding());
    }
    
    /**
     * Mapeia ResultSet para objeto Usuario
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setCpf(rs.getString("cpf"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipo_usuario")));
        usuario.setCargo(rs.getString("cargo"));
        usuario.setDepartamento(rs.getString("departamento"));
        
        Double salario = rs.getDouble("salario");
        if (!rs.wasNull()) {
            usuario.setSalario(salario);
        }
        
        Timestamp dataAdmissao = rs.getTimestamp("data_admissao");
        if (dataAdmissao != null) {
            usuario.setDataAdmissao(dataAdmissao.toLocalDateTime());
        }
        
        usuario.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        usuario.setDataAtualizacao(rs.getTimestamp("data_atualizacao").toLocalDateTime());
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setFaceEncoding(rs.getString("face_encoding"));
        
        return usuario;
    }
}
