package com.shiftly.repository;

import com.shiftly.model.Ponto;
import com.shiftly.model.TipoPonto;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar registros de ponto
 */
public class PontoRepository extends BaseRepository<Ponto, Long> {
    
    @Override
    public Optional<Ponto> findById(Long id) {
        if (id == null) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos WHERE id = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPonto(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    @Override
    public List<Ponto> findAll() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos ORDER BY data_hora DESC
                """;
            
            List<Ponto> pontos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    pontos.add(mapResultSetToPonto(rs));
                }
            }
            return pontos;
        });
    }
    
    @Override
    public Ponto save(Ponto ponto) {
        if (ponto == null) {
            throw new IllegalArgumentException("Ponto não pode ser nulo");
        }
        
        return executeWithConnection(conn -> {
            if (ponto.getId() == null) {
                return insert(conn, ponto);
            } else {
                return update(conn, ponto);
            }
        });
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = "DELETE FROM pontos WHERE id = ?";
            
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
            String sql = "SELECT 1 FROM pontos WHERE id = ?";
            
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
            String sql = "SELECT COUNT(*) FROM pontos";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        });
    }
    
    /**
     * Busca pontos por usuário
     */
    public List<Ponto> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos WHERE usuario_id = ? ORDER BY data_hora DESC
                """;
            
            List<Ponto> pontos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        pontos.add(mapResultSetToPonto(rs));
                    }
                }
            }
            return pontos;
        });
    }
    
    /**
     * Busca pontos por usuário e data
     */
    public List<Ponto> findByUsuarioIdAndData(Long usuarioId, LocalDate data) {
        if (usuarioId == null || data == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos 
                WHERE usuario_id = ? AND CAST(data_hora AS DATE) = ?
                ORDER BY data_hora
                """;
            
            List<Ponto> pontos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setDate(2, Date.valueOf(data));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        pontos.add(mapResultSetToPonto(rs));
                    }
                }
            }
            return pontos;
        });
    }
    
    /**
     * Busca pontos por usuário em um período
     */
    public List<Ponto> findByUsuarioIdAndPeriodo(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        if (usuarioId == null || dataInicio == null || dataFim == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos 
                WHERE usuario_id = ? AND CAST(data_hora AS DATE) BETWEEN ? AND ?
                ORDER BY data_hora
                """;
            
            List<Ponto> pontos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setDate(2, Date.valueOf(dataInicio));
                stmt.setDate(3, Date.valueOf(dataFim));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        pontos.add(mapResultSetToPonto(rs));
                    }
                }
            }
            return pontos;
        });
    }
    
    /**
     * Busca o último ponto de um usuário
     */
    public Optional<Ponto> findUltimoPontoByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos 
                WHERE usuario_id = ? 
                ORDER BY data_hora DESC 
                LIMIT 1
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPonto(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    /**
     * Busca pontos do dia atual por usuário
     */
    public List<Ponto> findPontosHojeByUsuarioId(Long usuarioId) {
        return findByUsuarioIdAndData(usuarioId, LocalDate.now());
    }
    
    /**
     * Busca pontos manuais (corrigidos pelo RH)
     */
    public List<Ponto> findPontosManuais() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos 
                WHERE manual = ? 
                ORDER BY data_correcao DESC
                """;
            
            List<Ponto> pontos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, true);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        pontos.add(mapResultSetToPonto(rs));
                    }
                }
            }
            return pontos;
        });
    }
    
    /**
     * Busca pontos que precisam de validação facial
     */
    public List<Ponto> findPontosSemValidacaoFacial() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                       face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                       data_correcao, motivo_correcao, data_criacao
                FROM pontos 
                WHERE face_validada = ? 
                ORDER BY data_hora DESC
                """;
            
            List<Ponto> pontos = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, false);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        pontos.add(mapResultSetToPonto(rs));
                    }
                }
            }
            return pontos;
        });
    }
    
    /**
     * Conta horas trabalhadas de um usuário em uma data
     */
    public double calcularHorasTrabalhadasNaData(Long usuarioId, LocalDate data) {
        List<Ponto> pontos = findByUsuarioIdAndData(usuarioId, data);
        
        if (pontos.isEmpty()) return 0.0;
        
        double totalHoras = 0.0;
        LocalDateTime entrada = null;
        
        for (Ponto ponto : pontos) {
            if (ponto.getTipoPonto().isEntrada()) {
                entrada = ponto.getDataHora();
            } else if (ponto.getTipoPonto().isSaida() && entrada != null) {
                long minutosTrabalho = java.time.Duration.between(entrada, ponto.getDataHora()).toMinutes();
                totalHoras += minutosTrabalho / 60.0;
                entrada = null;
            }
        }
        
        return totalHoras;
    }
    
    /**
     * Insere um novo ponto
     */
    private Ponto insert(Connection conn, Ponto ponto) throws SQLException {
        String sql = """
            INSERT INTO pontos (usuario_id, data_hora, tipo_ponto, latitude, longitude, endereco, precisao,
                              face_match, face_validada, observacoes, manual, corrigido_por_usuario_id,
                              data_correcao, motivo_correcao, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPontoParameters(stmt, ponto);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir ponto");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ponto.setId(generatedKeys.getLong(1));
                }
            }
        }
        
        return ponto;
    }
    
    /**
     * Atualiza um ponto existente
     */
    private Ponto update(Connection conn, Ponto ponto) throws SQLException {
        String sql = """
            UPDATE pontos SET usuario_id = ?, data_hora = ?, tipo_ponto = ?, latitude = ?, longitude = ?,
                            endereco = ?, precisao = ?, face_match = ?, face_validada = ?, observacoes = ?,
                            manual = ?, corrigido_por_usuario_id = ?, data_correcao = ?, motivo_correcao = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setPontoParameters(stmt, ponto);
            stmt.setLong(15, ponto.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Ponto não encontrado para atualização");
            }
        }
        
        return ponto;
    }
    
    /**
     * Define os parâmetros do ponto no PreparedStatement
     */
    private void setPontoParameters(PreparedStatement stmt, Ponto ponto) throws SQLException {
        stmt.setLong(1, ponto.getUsuarioId());
        stmt.setTimestamp(2, Timestamp.valueOf(ponto.getDataHora()));
        stmt.setString(3, ponto.getTipoPonto().name());
        
        if (ponto.getLatitude() != null) {
            stmt.setDouble(4, ponto.getLatitude());
        } else {
            stmt.setNull(4, Types.DECIMAL);
        }
        
        if (ponto.getLongitude() != null) {
            stmt.setDouble(5, ponto.getLongitude());
        } else {
            stmt.setNull(5, Types.DECIMAL);
        }
        
        stmt.setString(6, ponto.getEndereco());
        
        if (ponto.getPrecisao() != null) {
            stmt.setDouble(7, ponto.getPrecisao());
        } else {
            stmt.setNull(7, Types.DECIMAL);
        }
        
        stmt.setString(8, ponto.getFaceMatch());
        stmt.setBoolean(9, ponto.getFaceValidada());
        stmt.setString(10, ponto.getObservacoes());
        stmt.setBoolean(11, ponto.getManual());
        
        if (ponto.getCorrigidoPorUsuarioId() != null) {
            stmt.setLong(12, ponto.getCorrigidoPorUsuarioId());
        } else {
            stmt.setNull(12, Types.BIGINT);
        }
        
        if (ponto.getDataCorrecao() != null) {
            stmt.setTimestamp(13, Timestamp.valueOf(ponto.getDataCorrecao()));
        } else {
            stmt.setNull(13, Types.TIMESTAMP);
        }
        
        stmt.setString(14, ponto.getMotivoCorrecao());
        stmt.setTimestamp(15, Timestamp.valueOf(ponto.getDataCriacao()));
    }
    
    /**
     * Mapeia ResultSet para objeto Ponto
     */
    private Ponto mapResultSetToPonto(ResultSet rs) throws SQLException {
        Ponto ponto = new Ponto();
        
        ponto.setId(rs.getLong("id"));
        ponto.setUsuarioId(rs.getLong("usuario_id"));
        ponto.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        ponto.setTipoPonto(TipoPonto.valueOf(rs.getString("tipo_ponto")));
        
        Double latitude = rs.getDouble("latitude");
        if (!rs.wasNull()) {
            ponto.setLatitude(latitude);
        }
        
        Double longitude = rs.getDouble("longitude");
        if (!rs.wasNull()) {
            ponto.setLongitude(longitude);
        }
        
        ponto.setEndereco(rs.getString("endereco"));
        
        Double precisao = rs.getDouble("precisao");
        if (!rs.wasNull()) {
            ponto.setPrecisao(precisao);
        }
        
        ponto.setFaceMatch(rs.getString("face_match"));
        ponto.setFaceValidada(rs.getBoolean("face_validada"));
        ponto.setObservacoes(rs.getString("observacoes"));
        ponto.setManual(rs.getBoolean("manual"));
        
        Long corrigidoPorUsuarioId = rs.getLong("corrigido_por_usuario_id");
        if (!rs.wasNull()) {
            ponto.setCorrigidoPorUsuarioId(corrigidoPorUsuarioId);
        }
        
        Timestamp dataCorrecao = rs.getTimestamp("data_correcao");
        if (dataCorrecao != null) {
            ponto.setDataCorrecao(dataCorrecao.toLocalDateTime());
        }
        
        ponto.setMotivoCorrecao(rs.getString("motivo_correcao"));
        ponto.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        
        return ponto;
    }
}
