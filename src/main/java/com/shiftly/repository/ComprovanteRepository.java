package com.shiftly.repository;

import com.shiftly.model.Comprovante;
import com.shiftly.model.TipoComprovante;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar comprovantes de pagamento
 */
public class ComprovanteRepository extends BaseRepository<Comprovante, Long> {
    
    @Override
    public Optional<Comprovante> findById(Long id) {
        if (id == null) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE id = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToComprovante(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    @Override
    public List<Comprovante> findAll() {
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes ORDER BY data_emissao DESC
                """;
            
            List<Comprovante> comprovantes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    comprovantes.add(mapResultSetToComprovante(rs));
                }
            }
            return comprovantes;
        });
    }
    
    @Override
    public Comprovante save(Comprovante comprovante) {
        if (comprovante == null) {
            throw new IllegalArgumentException("Comprovante não pode ser nulo");
        }
        
        return executeWithConnection(conn -> {
            if (comprovante.getId() == null) {
                return insert(conn, comprovante);
            } else {
                return update(conn, comprovante);
            }
        });
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id == null) return false;
        
        return executeWithConnection(conn -> {
            String sql = "DELETE FROM comprovantes WHERE id = ?";
            
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
            String sql = "SELECT 1 FROM comprovantes WHERE id = ?";
            
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
            String sql = "SELECT COUNT(*) FROM comprovantes";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        });
    }
    
    /**
     * Busca comprovantes por usuário
     */
    public List<Comprovante> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE usuario_id = ? ORDER BY data_emissao DESC
                """;
            
            List<Comprovante> comprovantes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        comprovantes.add(mapResultSetToComprovante(rs));
                    }
                }
            }
            return comprovantes;
        });
    }
    
    /**
     * Busca comprovantes por tipo
     */
    public List<Comprovante> findByTipoComprovante(TipoComprovante tipo) {
        if (tipo == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE tipo_comprovante = ? ORDER BY data_emissao DESC
                """;
            
            List<Comprovante> comprovantes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, tipo.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        comprovantes.add(mapResultSetToComprovante(rs));
                    }
                }
            }
            return comprovantes;
        });
    }
    
    /**
     * Busca comprovantes por usuário e tipo
     */
    public List<Comprovante> findByUsuarioIdAndTipo(Long usuarioId, TipoComprovante tipo) {
        if (usuarioId == null || tipo == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE usuario_id = ? AND tipo_comprovante = ? ORDER BY data_emissao DESC
                """;
            
            List<Comprovante> comprovantes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setString(2, tipo.name());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        comprovantes.add(mapResultSetToComprovante(rs));
                    }
                }
            }
            return comprovantes;
        });
    }
    
    /**
     * Busca comprovantes em um período
     */
    public List<Comprovante> findByPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE data_emissao BETWEEN ? AND ? ORDER BY data_emissao DESC
                """;
            
            List<Comprovante> comprovantes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(dataInicio));
                stmt.setDate(2, Date.valueOf(dataFim));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        comprovantes.add(mapResultSetToComprovante(rs));
                    }
                }
            }
            return comprovantes;
        });
    }
    
    /**
     * Busca comprovantes por usuário em um período
     */
    public List<Comprovante> findByUsuarioIdAndPeriodo(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        if (usuarioId == null || dataInicio == null || dataFim == null) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE usuario_id = ? AND data_emissao BETWEEN ? AND ? ORDER BY data_emissao DESC
                """;
            
            List<Comprovante> comprovantes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setDate(2, Date.valueOf(dataInicio));
                stmt.setDate(3, Date.valueOf(dataFim));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        comprovantes.add(mapResultSetToComprovante(rs));
                    }
                }
            }
            return comprovantes;
        });
    }
    
    /**
     * Busca comprovante por referência
     */
    public Optional<Comprovante> findByReferencia(String referencia) {
        if (referencia == null || referencia.trim().isEmpty()) return Optional.empty();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE LOWER(referencia) = LOWER(?)
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, referencia.trim());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToComprovante(rs));
                    }
                }
            }
            return Optional.empty();
        });
    }
    
    /**
     * Verifica se já existe comprovante com a mesma referência
     */
    public boolean existsByReferencia(String referencia) {
        if (referencia == null || referencia.trim().isEmpty()) return false;
        
        return executeWithConnection(conn -> {
            String sql = "SELECT 1 FROM comprovantes WHERE LOWER(referencia) = LOWER(?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, referencia.trim());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
    
    /**
     * Busca últimos comprovantes por usuário
     */
    public List<Comprovante> findUltimosComprovantesByUsuarioId(Long usuarioId, int limite) {
        if (usuarioId == null || limite <= 0) return new ArrayList<>();
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT id, usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                       valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                       outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                       caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id
                FROM comprovantes WHERE usuario_id = ? ORDER BY data_emissao DESC LIMIT ?
                """;
            
            List<Comprovante> comprovantes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setInt(2, limite);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        comprovantes.add(mapResultSetToComprovante(rs));
                    }
                }
            }
            return comprovantes;
        });
    }
    
    /**
     * Calcula valor total pago a um usuário em um período
     */
    public Double calcularValorTotalPago(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        if (usuarioId == null || dataInicio == null || dataFim == null) return 0.0;
        
        return executeWithConnection(conn -> {
            String sql = """
                SELECT COALESCE(SUM(valor_liquido), 0) as total_pago
                FROM comprovantes 
                WHERE usuario_id = ? AND data_emissao BETWEEN ? AND ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, usuarioId);
                stmt.setDate(2, Date.valueOf(dataInicio));
                stmt.setDate(3, Date.valueOf(dataFim));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getDouble("total_pago") : 0.0;
                }
            }
        });
    }
    
    /**
     * Insere um novo comprovante
     */
    private Comprovante insert(Connection conn, Comprovante comprovante) throws SQLException {
        String sql = """
            INSERT INTO comprovantes (usuario_id, tipo_comprovante, referencia, data_emissao, periodo_inicio, periodo_fim,
                                    valor_bruto, valor_descontos, valor_liquido, salario_base, horas_extras, adicional_noturno,
                                    outros_proventos, inss, irrf, vale_transporte, vale_refeicao, outros_descontos,
                                    caminho_arquivo, nome_arquivo, tamanho_arquivo, data_criacao, data_atualizacao, criado_por_usuario_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setComprovanteParameters(stmt, comprovante);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir comprovante");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comprovante.setId(generatedKeys.getLong(1));
                }
            }
        }
        
        return comprovante;
    }
    
    /**
     * Atualiza um comprovante existente
     */
    private Comprovante update(Connection conn, Comprovante comprovante) throws SQLException {
        String sql = """
            UPDATE comprovantes SET usuario_id = ?, tipo_comprovante = ?, referencia = ?, data_emissao = ?,
                                  periodo_inicio = ?, periodo_fim = ?, valor_bruto = ?, valor_descontos = ?,
                                  valor_liquido = ?, salario_base = ?, horas_extras = ?, adicional_noturno = ?,
                                  outros_proventos = ?, inss = ?, irrf = ?, vale_transporte = ?, vale_refeicao = ?,
                                  outros_descontos = ?, caminho_arquivo = ?, nome_arquivo = ?, tamanho_arquivo = ?,
                                  data_atualizacao = ?, criado_por_usuario_id = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setComprovanteParameters(stmt, comprovante);
            stmt.setLong(24, comprovante.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Comprovante não encontrado para atualização");
            }
        }
        
        return comprovante;
    }
    
    /**
     * Define os parâmetros do comprovante no PreparedStatement
     */
    private void setComprovanteParameters(PreparedStatement stmt, Comprovante comprovante) throws SQLException {
        stmt.setLong(1, comprovante.getUsuarioId());
        stmt.setString(2, comprovante.getTipoComprovante().name());
        stmt.setString(3, comprovante.getReferencia());
        stmt.setDate(4, Date.valueOf(comprovante.getDataEmissao()));
        stmt.setDate(5, Date.valueOf(comprovante.getPeriodoInicio()));
        stmt.setDate(6, Date.valueOf(comprovante.getPeriodoFim()));
        stmt.setDouble(7, comprovante.getValorBruto());
        stmt.setDouble(8, comprovante.getValorDescontos());
        stmt.setDouble(9, comprovante.getValorLiquido());
        
        // Parâmetros opcionais
        setOptionalDouble(stmt, 10, comprovante.getSalarioBase());
        setOptionalDouble(stmt, 11, comprovante.getHorasExtras());
        setOptionalDouble(stmt, 12, comprovante.getAdicionalNoturno());
        setOptionalDouble(stmt, 13, comprovante.getOutrosProventos());
        setOptionalDouble(stmt, 14, comprovante.getInss());
        setOptionalDouble(stmt, 15, comprovante.getIrrf());
        setOptionalDouble(stmt, 16, comprovante.getValeTransporte());
        setOptionalDouble(stmt, 17, comprovante.getValeRefeicao());
        setOptionalDouble(stmt, 18, comprovante.getOutrosDescontos());
        
        stmt.setString(19, comprovante.getCaminhoArquivo());
        stmt.setString(20, comprovante.getNomeArquivo());
        
        if (comprovante.getTamanhoArquivo() != null) {
            stmt.setLong(21, comprovante.getTamanhoArquivo());
        } else {
            stmt.setNull(21, Types.BIGINT);
        }
        
        stmt.setTimestamp(22, Timestamp.valueOf(comprovante.getDataCriacao()));
        stmt.setTimestamp(23, Timestamp.valueOf(comprovante.getDataAtualizacao()));
        
        if (comprovante.getCriadoPorUsuarioId() != null) {
            stmt.setLong(24, comprovante.getCriadoPorUsuarioId());
        } else {
            stmt.setNull(24, Types.BIGINT);
        }
    }
    
    /**
     * Define um valor Double opcional no PreparedStatement
     */
    private void setOptionalDouble(PreparedStatement stmt, int index, Double value) throws SQLException {
        if (value != null) {
            stmt.setDouble(index, value);
        } else {
            stmt.setNull(index, Types.DECIMAL);
        }
    }
    
    /**
     * Mapeia ResultSet para objeto Comprovante
     */
    private Comprovante mapResultSetToComprovante(ResultSet rs) throws SQLException {
        Comprovante comprovante = new Comprovante();
        
        comprovante.setId(rs.getLong("id"));
        comprovante.setUsuarioId(rs.getLong("usuario_id"));
        comprovante.setTipoComprovante(TipoComprovante.valueOf(rs.getString("tipo_comprovante")));
        comprovante.setReferencia(rs.getString("referencia"));
        comprovante.setDataEmissao(rs.getDate("data_emissao").toLocalDate());
        comprovante.setPeriodoInicio(rs.getDate("periodo_inicio").toLocalDate());
        comprovante.setPeriodoFim(rs.getDate("periodo_fim").toLocalDate());
        comprovante.setValorBruto(rs.getDouble("valor_bruto"));
        comprovante.setValorDescontos(rs.getDouble("valor_descontos"));
        comprovante.setValorLiquido(rs.getDouble("valor_liquido"));
        
        // Campos opcionais
        setOptionalDoubleFromRS(rs, "salario_base", comprovante::setSalarioBase);
        setOptionalDoubleFromRS(rs, "horas_extras", comprovante::setHorasExtras);
        setOptionalDoubleFromRS(rs, "adicional_noturno", comprovante::setAdicionalNoturno);
        setOptionalDoubleFromRS(rs, "outros_proventos", comprovante::setOutrosProventos);
        setOptionalDoubleFromRS(rs, "inss", comprovante::setInss);
        setOptionalDoubleFromRS(rs, "irrf", comprovante::setIrrf);
        setOptionalDoubleFromRS(rs, "vale_transporte", comprovante::setValeTransporte);
        setOptionalDoubleFromRS(rs, "vale_refeicao", comprovante::setValeRefeicao);
        setOptionalDoubleFromRS(rs, "outros_descontos", comprovante::setOutrosDescontos);
        
        comprovante.setCaminhoArquivo(rs.getString("caminho_arquivo"));
        comprovante.setNomeArquivo(rs.getString("nome_arquivo"));
        
        Long tamanhoArquivo = rs.getLong("tamanho_arquivo");
        if (!rs.wasNull()) {
            comprovante.setTamanhoArquivo(tamanhoArquivo);
        }
        
        comprovante.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        comprovante.setDataAtualizacao(rs.getTimestamp("data_atualizacao").toLocalDateTime());
        
        Long criadoPorUsuarioId = rs.getLong("criado_por_usuario_id");
        if (!rs.wasNull()) {
            comprovante.setCriadoPorUsuarioId(criadoPorUsuarioId);
        }
        
        return comprovante;
    }
    
    /**
     * Interface funcional para setar valores Double opcionais
     */
    @FunctionalInterface
    private interface DoubleSetter {
        void accept(Double value);
    }
    
    /**
     * Define um valor Double opcional do ResultSet
     */
    private void setOptionalDoubleFromRS(ResultSet rs, String columnName, DoubleSetter setter) throws SQLException {
        Double value = rs.getDouble(columnName);
        if (!rs.wasNull()) {
            setter.accept(value);
        }
    }
}
