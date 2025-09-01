package com.shiftly.repository;

import com.shiftly.model.Ferias;
import com.shiftly.model.StatusFerias;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar férias
 */
public class FeriasRepository extends BaseRepository<Ferias, Long> {
    
    @Override
    public Optional<Ferias> findById(Long id) {
        if (id == null) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
                FROM ferias WHERE id = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToFerias(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    @Override
    public List<Ferias> findAll() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
                FROM ferias ORDER BY data_solicitacao DESC
                """;
            
            List<Ferias> feriasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    feriasList.add(mapResultSetToFerias(rs));
                }
            }
            return feriasList;
        });
    }
    
    @Override
    public Ferias save(Ferias ferias) {
        if (ferias == null) {
            throw new IllegalArgumentException("Férias não pode ser nulo");
        }
        
        return executeWithConnection(conn -> {
            if (ferias.getId() == null) {
                return insert(conn, ferias);
            } else {
                return update(conn, ferias);
            }
        });
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = "DELETE FROM ferias WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            }
        });
    }
    
    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = "SELECT 1 FROM ferias WHERE id = ?";
            
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
            String sql = "SELECT COUNT(*) FROM ferias";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        });
    }
    
    /**
     * Busca férias por usuário
     */
    public List<Ferias> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
                FROM ferias WHERE usuario_id = ? ORDER BY data_solicitacao DESC
                """;
            
            List<Ferias> feriasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        feriasList.add(mapResultSetToFerias(rs));
                    }
                }
            }
            return feriasList;
        });
    }
    
    /**
     * Busca férias por status
     */
    public List<Ferias> findByStatus(StatusFerias status) {
        if (status == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
                FROM ferias WHERE status = ? ORDER BY data_solicitacao DESC
                """;
            
            List<Ferias> feriasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        feriasList.add(mapResultSetToFerias(rs));
                    }
                }
            }
            return feriasList;
        });
    }
    
    /**
     * Busca férias pendentes
     */
    public List<Ferias> findFeriasPendentes() {
        return findByStatus(StatusFerias.PENDENTE);
    }
    
    /**
     * Busca férias aprovadas
     */
    public List<Ferias> findFeriasAprovadas() {
        return findByStatus(StatusFerias.APROVADA);
    }
    
    /**
     * Busca férias por usuário e status
     */
    public List<Ferias> findByUsuarioIdAndStatus(Long usuarioId, StatusFerias status) {
        if (usuarioId == null || status == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
                FROM ferias WHERE usuario_id = ? AND status = ? ORDER BY data_solicitacao DESC
                """;
            
            List<Ferias> feriasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setString(2, status.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        feriasList.add(mapResultSetToFerias(rs));
                    }
                }
            }
            return feriasList;
        });
    }
    
    /**
     * Busca férias em um período específico
     */
    public List<Ferias> findFeriasNoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
                FROM ferias 
                WHERE (data_inicio BETWEEN ? AND ?) OR (data_fim BETWEEN ? AND ?) OR (data_inicio <= ? AND data_fim >= ?)
                ORDER BY data_inicio
                """;
            
            List<Ferias> feriasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(dataInicio));
                stmt.setDate(2, Date.valueOf(dataFim));
                stmt.setDate(3, Date.valueOf(dataInicio));
                stmt.setDate(4, Date.valueOf(dataFim));
                stmt.setDate(5, Date.valueOf(dataInicio));
                stmt.setDate(6, Date.valueOf(dataFim));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        feriasList.add(mapResultSetToFerias(rs));
                    }
                }
            }
            return feriasList;
        });
    }
    
    /**
     * Verifica se usuário tem conflito de férias no período
     */
    public boolean temConflitoFerias(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, Long feriasIdExcluir) {
        if (usuarioId == null || dataInicio == null || dataFim == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT COUNT(*) FROM ferias 
                WHERE usuario_id = ? AND status IN ('PENDENTE', 'APROVADA')
                AND ((data_inicio BETWEEN ? AND ?) OR (data_fim BETWEEN ? AND ?) OR (data_inicio <= ? AND data_fim >= ?))
                """ + (feriasIdExcluir != null ? "AND id != ?" : "");
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setDate(2, Date.valueOf(dataInicio));
                stmt.setDate(3, Date.valueOf(dataFim));
                stmt.setDate(4, Date.valueOf(dataInicio));
                stmt.setDate(5, Date.valueOf(dataFim));
                stmt.setDate(6, Date.valueOf(dataInicio));
                stmt.setDate(7, Date.valueOf(dataFim));
                
                if (feriasIdExcluir != null) {
                    stmt.setLong(8, feriasIdExcluir);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        });
    }
    
    /**
     * Conta dias de férias já aprovadas no ano para um usuário
     */
    public int contarDiasFeriasNoAno(Long usuarioId, int ano) {
        if (usuarioId == null) return 0;
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT COALESCE(SUM(DATEDIFF(data_fim, data_inicio) + 1), 0) as total_dias
                FROM ferias 
                WHERE usuario_id = ? AND status = 'APROVADA' 
                AND YEAR(data_inicio) = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setInt(2, ano);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getInt("total_dias") : 0;
                }
            }
        });
    }
    
    /**
     * Busca férias que vencem em breve (próximas do limite)
     */
    public List<Ferias> findFeriasVencendoEm(int dias) {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao
                FROM ferias 
                WHERE status = 'APROVADA' AND data_inicio BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
                ORDER BY data_inicio
                """;
            
            List<Ferias> feriasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, dias);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        feriasList.add(mapResultSetToFerias(rs));
                    }
                }
            }
            return feriasList;
        });
    }
    
    /**
     * Insere novas férias
     */
    private Ferias insert(Connection conn, Ferias ferias) throws SQLException {
        String sql = """
            INSERT INTO ferias (usuario_id, data_inicio, data_fim, status, observacoes, motivo_recusa,
                              aprovado_por_usuario_id, data_aprovacao, data_solicitacao, data_atualizacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFeriasParameters(stmt, ferias);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir férias");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ferias.setId(generatedKeys.getLong(1));
                }
            }
        }
        
        return ferias;
    }
    
    /**
     * Atualiza férias existentes
     */
    private Ferias update(Connection conn, Ferias ferias) throws SQLException {
        String sql = """
            UPDATE ferias SET usuario_id = ?, data_inicio = ?, data_fim = ?, status = ?, observacoes = ?,
                            motivo_recusa = ?, aprovado_por_usuario_id = ?, data_aprovacao = ?, data_atualizacao = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setFeriasParameters(stmt, ferias);
            stmt.setLong(10, ferias.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Férias não encontradas para atualização");
            }
        }
        
        return ferias;
    }
    
    /**
     * Define os parâmetros das férias no PreparedStatement
     */
    private void setFeriasParameters(PreparedStatement stmt, Ferias ferias) throws SQLException {
        stmt.setLong(1, ferias.getUsuarioId());
        stmt.setDate(2, Date.valueOf(ferias.getDataInicio()));
        stmt.setDate(3, Date.valueOf(ferias.getDataFim()));
        stmt.setString(4, ferias.getStatus().name());
        stmt.setString(5, ferias.getObservacoes());
        stmt.setString(6, ferias.getMotivoRecusa());
        
        if (ferias.getAprovadoPorUsuarioId() != null) {
            stmt.setLong(7, ferias.getAprovadoPorUsuarioId());
        } else {
            stmt.setNull(7, Types.BIGINT);
        }
        
        if (ferias.getDataAprovacao() != null) {
            stmt.setTimestamp(8, Timestamp.valueOf(ferias.getDataAprovacao()));
        } else {
            stmt.setNull(8, Types.TIMESTAMP);
        }
        
        stmt.setTimestamp(9, Timestamp.valueOf(ferias.getDataSolicitacao()));
        stmt.setTimestamp(10, Timestamp.valueOf(ferias.getDataAtualizacao()));
    }
    
    /**
     * Mapeia ResultSet para objeto Ferias
     */
    private Ferias mapResultSetToFerias(ResultSet rs) throws SQLException {
        Ferias ferias = new Ferias();
        
        ferias.setId(rs.getLong("id"));
        ferias.setUsuarioId(rs.getLong("usuario_id"));
        ferias.setDataInicio(rs.getDate("data_inicio").toLocalDate());
        ferias.setDataFim(rs.getDate("data_fim").toLocalDate());
        ferias.setStatus(StatusFerias.valueOf(rs.getString("status")));
        ferias.setObservacoes(rs.getString("observacoes"));
        ferias.setMotivoRecusa(rs.getString("motivo_recusa"));
        
        Long aprovadoPorUsuarioId = rs.getLong("aprovado_por_usuario_id");
        if (!rs.wasNull()) {
            ferias.setAprovadoPorUsuarioId(aprovadoPorUsuarioId);
        }
        
        Timestamp dataAprovacao = rs.getTimestamp("data_aprovacao");
        if (dataAprovacao != null) {
            ferias.setDataAprovacao(dataAprovacao.toLocalDateTime());
        }
        
        ferias.setDataSolicitacao(rs.getTimestamp("data_solicitacao").toLocalDateTime());
        ferias.setDataAtualizacao(rs.getTimestamp("data_atualizacao").toLocalDateTime());
        
        return ferias;
    }
}
