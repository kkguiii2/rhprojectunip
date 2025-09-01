package com.shiftly.repository;

import com.shiftly.model.HorasExtras;
import com.shiftly.model.StatusHorasExtras;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar horas extras
 */
public class HorasExtrasRepository extends BaseRepository<HorasExtras, Long> {
    
    @Override
    public Optional<HorasExtras> findById(Long id) {
        if (id == null) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE id = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    @Override
    public List<HorasExtras> findAll() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras ORDER BY data_solicitacao DESC
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    horasExtrasList.add(mapResultSetToHorasExtras(rs));
                }
            }
            return horasExtrasList;
        });
    }
    
    @Override
    public HorasExtras save(HorasExtras horasExtras) {
        if (horasExtras == null) {
            throw new IllegalArgumentException("Horas extras não pode ser nulo");
        }
        
        return executeWithConnection(conn -> {
            if (horasExtras.getId() == null) {
                return insert(conn, horasExtras);
            } else {
                return update(conn, horasExtras);
            }
        });
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = "DELETE FROM horas_extras WHERE id = ?";
            
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
            String sql = "SELECT 1 FROM horas_extras WHERE id = ?";
            
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
            String sql = "SELECT COUNT(*) FROM horas_extras";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        });
    }
    
    /**
     * Busca horas extras por usuário
     */
    public List<HorasExtras> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE usuario_id = ? ORDER BY data DESC
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        horasExtrasList.add(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return horasExtrasList;
        });
    }
    
    /**
     * Busca horas extras por status
     */
    public List<HorasExtras> findByStatus(StatusHorasExtras status) {
        if (status == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE status = ? ORDER BY data_solicitacao DESC
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        horasExtrasList.add(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return horasExtrasList;
        });
    }
    
    /**
     * Busca horas extras pendentes
     */
    public List<HorasExtras> findHorasExtrasPendentes() {
        return findByStatus(StatusHorasExtras.PENDENTE);
    }
    
    /**
     * Busca horas extras aprovadas
     */
    public List<HorasExtras> findHorasExtrasAprovadas() {
        return findByStatus(StatusHorasExtras.APROVADA);
    }
    
    /**
     * Busca horas extras por usuário e status
     */
    public List<HorasExtras> findByUsuarioIdAndStatus(Long usuarioId, StatusHorasExtras status) {
        if (usuarioId == null || status == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE usuario_id = ? AND status = ? ORDER BY data DESC
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setString(2, status.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        horasExtrasList.add(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return horasExtrasList;
        });
    }
    
    /**
     * Busca horas extras em um período
     */
    public List<HorasExtras> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE data BETWEEN ? AND ? ORDER BY data DESC
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(dataInicio));
                stmt.setDate(2, Date.valueOf(dataFim));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        horasExtrasList.add(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return horasExtrasList;
        });
    }
    
    /**
     * Busca horas extras por usuário em um período
     */
    public List<HorasExtras> findByUsuarioIdAndPeriodo(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        if (usuarioId == null || dataInicio == null || dataFim == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE usuario_id = ? AND data BETWEEN ? AND ? ORDER BY data DESC
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setDate(2, Date.valueOf(dataInicio));
                stmt.setDate(3, Date.valueOf(dataFim));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        horasExtrasList.add(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return horasExtrasList;
        });
    }
    
    /**
     * Calcula total de horas extras por usuário no mês
     */
    public Double calcularTotalHorasExtrasNoMes(Long usuarioId, int ano, int mes) {
        if (usuarioId == null) return 0.0;
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT COALESCE(SUM(horas), 0) as total_horas
                FROM horas_extras 
                WHERE usuario_id = ? AND status = 'APROVADA' 
                AND YEAR(data) = ? AND MONTH(data) = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setInt(2, ano);
                stmt.setInt(3, mes);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getDouble("total_horas") : 0.0;
                }
            }
        });
    }
    
    /**
     * Busca horas extras não pagas
     */
    public List<HorasExtras> findHorasExtrasNaoPagas() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE status = 'APROVADA' AND pago = ? ORDER BY data
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, false);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        horasExtrasList.add(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return horasExtrasList;
        });
    }
    
    /**
     * Busca horas extras não pagas por usuário
     */
    public List<HorasExtras> findHorasExtrasNaoPagasByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                       aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                       data_solicitacao, data_atualizacao
                FROM horas_extras WHERE usuario_id = ? AND status = 'APROVADA' AND pago = ? ORDER BY data
                """;
            
            List<HorasExtras> horasExtrasList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setBoolean(2, false);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        horasExtrasList.add(mapResultSetToHorasExtras(rs));
                    }
                }
            }
            return horasExtrasList;
        });
    }
    
    /**
     * Calcula valor total a pagar em horas extras
     */
    public Double calcularValorTotalAPagar(Long usuarioId, Double valorHora) {
        if (usuarioId == null || valorHora == null) return 0.0;
        
        List<HorasExtras> horasNaoPagas = findHorasExtrasNaoPagasByUsuarioId(usuarioId);
        return horasNaoPagas.stream()
                .mapToDouble(h -> h.calcularValor(valorHora))
                .sum();
    }
    
    /**
     * Insere novas horas extras
     */
    private HorasExtras insert(Connection conn, HorasExtras horasExtras) throws SQLException {
        String sql = """
            INSERT INTO horas_extras (usuario_id, data, horas, status, descricao, justificativa, motivo_recusa,
                                    aprovado_por_usuario_id, data_aprovacao, pago, data_pagamento, valor_pago,
                                    data_solicitacao, data_atualizacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setHorasExtrasParameters(stmt, horasExtras);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir horas extras");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    horasExtras.setId(generatedKeys.getLong(1));
                }
            }
        }
        
        return horasExtras;
    }
    
    /**
     * Atualiza horas extras existentes
     */
    private HorasExtras update(Connection conn, HorasExtras horasExtras) throws SQLException {
        String sql = """
            UPDATE horas_extras SET usuario_id = ?, data = ?, horas = ?, status = ?, descricao = ?,
                                  justificativa = ?, motivo_recusa = ?, aprovado_por_usuario_id = ?,
                                  data_aprovacao = ?, pago = ?, data_pagamento = ?, valor_pago = ?, data_atualizacao = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setHorasExtrasParameters(stmt, horasExtras);
            stmt.setLong(14, horasExtras.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Horas extras não encontradas para atualização");
            }
        }
        
        return horasExtras;
    }
    
    /**
     * Define os parâmetros das horas extras no PreparedStatement
     */
    private void setHorasExtrasParameters(PreparedStatement stmt, HorasExtras horasExtras) throws SQLException {
        stmt.setLong(1, horasExtras.getUsuarioId());
        stmt.setDate(2, Date.valueOf(horasExtras.getData()));
        stmt.setDouble(3, horasExtras.getHoras());
        stmt.setString(4, horasExtras.getStatus().name());
        stmt.setString(5, horasExtras.getDescricao());
        stmt.setString(6, horasExtras.getJustificativa());
        stmt.setString(7, horasExtras.getMotivoRecusa());
        
        if (horasExtras.getAprovadoPorUsuarioId() != null) {
            stmt.setLong(8, horasExtras.getAprovadoPorUsuarioId());
        } else {
            stmt.setNull(8, Types.BIGINT);
        }
        
        if (horasExtras.getDataAprovacao() != null) {
            stmt.setTimestamp(9, Timestamp.valueOf(horasExtras.getDataAprovacao()));
        } else {
            stmt.setNull(9, Types.TIMESTAMP);
        }
        
        stmt.setBoolean(10, horasExtras.getPago());
        
        if (horasExtras.getDataPagamento() != null) {
            stmt.setDate(11, Date.valueOf(horasExtras.getDataPagamento()));
        } else {
            stmt.setNull(11, Types.DATE);
        }
        
        if (horasExtras.getValorPago() != null) {
            stmt.setDouble(12, horasExtras.getValorPago());
        } else {
            stmt.setNull(12, Types.DECIMAL);
        }
        
        stmt.setTimestamp(13, Timestamp.valueOf(horasExtras.getDataSolicitacao()));
        stmt.setTimestamp(14, Timestamp.valueOf(horasExtras.getDataAtualizacao()));
    }
    
    /**
     * Mapeia ResultSet para objeto HorasExtras
     */
    private HorasExtras mapResultSetToHorasExtras(ResultSet rs) throws SQLException {
        HorasExtras horasExtras = new HorasExtras();
        
        horasExtras.setId(rs.getLong("id"));
        horasExtras.setUsuarioId(rs.getLong("usuario_id"));
        horasExtras.setData(rs.getDate("data").toLocalDate());
        horasExtras.setHoras(rs.getDouble("horas"));
        horasExtras.setStatus(StatusHorasExtras.valueOf(rs.getString("status")));
        horasExtras.setDescricao(rs.getString("descricao"));
        horasExtras.setJustificativa(rs.getString("justificativa"));
        horasExtras.setMotivoRecusa(rs.getString("motivo_recusa"));
        
        Long aprovadoPorUsuarioId = rs.getLong("aprovado_por_usuario_id");
        if (!rs.wasNull()) {
            horasExtras.setAprovadoPorUsuarioId(aprovadoPorUsuarioId);
        }
        
        Timestamp dataAprovacao = rs.getTimestamp("data_aprovacao");
        if (dataAprovacao != null) {
            horasExtras.setDataAprovacao(dataAprovacao.toLocalDateTime());
        }
        
        horasExtras.setPago(rs.getBoolean("pago"));
        
        Date dataPagamento = rs.getDate("data_pagamento");
        if (dataPagamento != null) {
            horasExtras.setDataPagamento(dataPagamento.toLocalDate());
        }
        
        Double valorPago = rs.getDouble("valor_pago");
        if (!rs.wasNull()) {
            horasExtras.setValorPago(valorPago);
        }
        
        horasExtras.setDataSolicitacao(rs.getTimestamp("data_solicitacao").toLocalDateTime());
        horasExtras.setDataAtualizacao(rs.getTimestamp("data_atualizacao").toLocalDateTime());
        
        return horasExtras;
    }
}
