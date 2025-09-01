package com.shiftly.service;

import com.shiftly.model.Ferias;
import com.shiftly.model.StatusFerias;
import com.shiftly.model.Usuario;
import com.shiftly.repository.FeriasRepository;
import com.shiftly.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar férias
 */
public class FeriasService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeriasService.class);
    
    private final FeriasRepository feriasRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Configurações de férias
    private static final int DIAS_FERIAS_ANO = 30;
    private static final int ANTECEDENCIA_MINIMA_DIAS = 30;
    
    public FeriasService() {
        this.feriasRepository = new FeriasRepository();
        this.usuarioRepository = new UsuarioRepository();
    }
    
    /**
     * Solicita férias
     */
    public Ferias solicitarFerias(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, String observacoes) {
        logger.info("Solicitando férias para usuário ID {} - Período: {} a {}", usuarioId, dataInicio, dataFim);
        
        // Verifica se o usuário existe e está ativo
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getAtivo()) {
            throw new IllegalArgumentException("Usuário não encontrado ou inativo");
        }
        
        // Validações
        validarSolicitacaoFerias(usuarioId, dataInicio, dataFim);
        
        // Cria a solicitação
        Ferias ferias = new Ferias(usuarioId, dataInicio, dataFim, observacoes);
        
        Ferias feriasSalvas = feriasRepository.save(ferias);
        logger.info("Férias solicitadas com sucesso: ID {}", feriasSalvas.getId());
        
        return feriasSalvas;
    }
    
    /**
     * Aprova férias
     */
    public Ferias aprovarFerias(Long feriasId, Long usuarioRhId) {
        logger.info("Aprovando férias ID {} por usuário RH ID {}", feriasId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para aprovar férias");
        }
        
        // Busca as férias
        Optional<Ferias> feriasOpt = feriasRepository.findById(feriasId);
        if (feriasOpt.isEmpty()) {
            throw new IllegalArgumentException("Férias não encontradas");
        }
        
        Ferias ferias = feriasOpt.get();
        
        // Verifica se pode ser aprovada
        if (!ferias.isPendente()) {
            throw new IllegalStateException("Apenas férias pendentes podem ser aprovadas");
        }
        
        // Verifica conflitos
        if (feriasRepository.temConflitoFerias(ferias.getUsuarioId(), ferias.getDataInicio(), 
                                             ferias.getDataFim(), ferias.getId())) {
            throw new IllegalStateException("Conflito com outras férias já aprovadas");
        }
        
        // Aprova
        ferias.aprovar(usuarioRhId);
        
        Ferias feriasAprovadas = feriasRepository.save(ferias);
        logger.info("Férias aprovadas com sucesso: ID {}", feriasAprovadas.getId());
        
        return feriasAprovadas;
    }
    
    /**
     * Recusa férias
     */
    public Ferias recusarFerias(Long feriasId, String motivo, Long usuarioRhId) {
        logger.info("Recusando férias ID {} por usuário RH ID {}", feriasId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para recusar férias");
        }
        
        // Busca as férias
        Optional<Ferias> feriasOpt = feriasRepository.findById(feriasId);
        if (feriasOpt.isEmpty()) {
            throw new IllegalArgumentException("Férias não encontradas");
        }
        
        Ferias ferias = feriasOpt.get();
        
        // Verifica se pode ser recusada
        if (!ferias.isPendente()) {
            throw new IllegalStateException("Apenas férias pendentes podem ser recusadas");
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da recusa é obrigatório");
        }
        
        // Recusa
        ferias.recusar(usuarioRhId, motivo);
        
        Ferias feriasRecusadas = feriasRepository.save(ferias);
        logger.info("Férias recusadas com sucesso: ID {}", feriasRecusadas.getId());
        
        return feriasRecusadas;
    }
    
    /**
     * Cancela férias
     */
    public Ferias cancelarFerias(Long feriasId, Long usuarioId) {
        logger.info("Cancelando férias ID {} por usuário ID {}", feriasId, usuarioId);
        
        // Busca as férias
        Optional<Ferias> feriasOpt = feriasRepository.findById(feriasId);
        if (feriasOpt.isEmpty()) {
            throw new IllegalArgumentException("Férias não encontradas");
        }
        
        Ferias ferias = feriasOpt.get();
        
        // Verifica se é o próprio usuário ou RH
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        if (!ferias.getUsuarioId().equals(usuarioId) && !usuario.isRH()) {
            throw new SecurityException("Usuário não tem permissão para cancelar estas férias");
        }
        
        // Verifica se pode ser cancelada
        if (!ferias.getStatus().permiteCancelamento()) {
            throw new IllegalStateException("Férias não podem ser canceladas neste status: " + ferias.getStatus());
        }
        
        // Cancela
        ferias.cancelar();
        
        Ferias feriasCanceladas = feriasRepository.save(ferias);
        logger.info("Férias canceladas com sucesso: ID {}", feriasCanceladas.getId());
        
        return feriasCanceladas;
    }
    
    /**
     * Edita férias pendentes
     */
    public Ferias editarFerias(Long feriasId, LocalDate novaDataInicio, LocalDate novaDataFim, 
                              String novasObservacoes, Long usuarioId) {
        logger.info("Editando férias ID {} por usuário ID {}", feriasId, usuarioId);
        
        // Busca as férias
        Optional<Ferias> feriasOpt = feriasRepository.findById(feriasId);
        if (feriasOpt.isEmpty()) {
            throw new IllegalArgumentException("Férias não encontradas");
        }
        
        Ferias ferias = feriasOpt.get();
        
        // Verifica se é o próprio usuário
        if (!ferias.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("Usuário não pode editar férias de outro usuário");
        }
        
        // Verifica se pode ser editada
        if (!ferias.getStatus().permiteEdicao()) {
            throw new IllegalStateException("Férias não podem ser editadas neste status: " + ferias.getStatus());
        }
        
        // Valida novas datas
        validarSolicitacaoFerias(usuarioId, novaDataInicio, novaDataFim, feriasId);
        
        // Atualiza
        ferias.setDataInicio(novaDataInicio);
        ferias.setDataFim(novaDataFim);
        ferias.setObservacoes(novasObservacoes);
        
        Ferias feriasEditadas = feriasRepository.save(ferias);
        logger.info("Férias editadas com sucesso: ID {}", feriasEditadas.getId());
        
        return feriasEditadas;
    }
    
    /**
     * Busca férias por usuário
     */
    public List<Ferias> buscarFeriasPorUsuario(Long usuarioId) {
        return feriasRepository.findByUsuarioId(usuarioId);
    }
    
    /**
     * Busca férias pendentes de aprovação
     */
    public List<Ferias> buscarFeriasPendentes() {
        return feriasRepository.findFeriasPendentes();
    }
    
    /**
     * Busca férias aprovadas
     */
    public List<Ferias> buscarFeriasAprovadas() {
        return feriasRepository.findFeriasAprovadas();
    }
    
    /**
     * Busca férias por status
     */
    public List<Ferias> buscarFeriasPorStatus(StatusFerias status) {
        return feriasRepository.findByStatus(status);
    }
    
    /**
     * Busca férias em um período
     */
    public List<Ferias> buscarFeriasNoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return feriasRepository.findFeriasNoPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Calcula saldo de férias de um usuário
     */
    public int calcularSaldoFerias(Long usuarioId) {
        // Busca usuário para ver data de admissão
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return 0;
        }
        
        Usuario usuario = usuarioOpt.get();
        if (usuario.getDataAdmissao() == null) {
            return 0;
        }
        
        LocalDate dataAdmissao = usuario.getDataAdmissao().toLocalDate();
        LocalDate hoje = LocalDate.now();
        
        // Calcula anos completos de trabalho
        int anosCompletos = hoje.getYear() - dataAdmissao.getYear();
        if (hoje.getDayOfYear() < dataAdmissao.getDayOfYear()) {
            anosCompletos--;
        }
        
        if (anosCompletos <= 0) {
            return 0; // Ainda não completou um ano
        }
        
        // Direito total (30 dias por ano)
        int direitoTotal = anosCompletos * DIAS_FERIAS_ANO;
        
        // Dias já utilizados (férias aprovadas)
        int diasUtilizados = feriasRepository.contarDiasFeriasNoAno(usuarioId, hoje.getYear());
        
        return direitoTotal - diasUtilizados;
    }
    
    /**
     * Verifica se usuário pode solicitar férias no período
     */
    public boolean podesolicitarFerias(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        try {
            validarSolicitacaoFerias(usuarioId, dataInicio, dataFim);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Busca férias que vencem em breve
     */
    public List<Ferias> buscarFeriasVencendoEm(int dias) {
        return feriasRepository.findFeriasVencendoEm(dias);
    }
    
    /**
     * Relatório de férias por departamento
     */
    public java.util.Map<String, Long> relatorioFeriasPorDepartamento() {
        List<Ferias> todasFerias = feriasRepository.findAll();
        java.util.Map<String, Long> relatorio = new java.util.HashMap<>();
        
        for (Ferias ferias : todasFerias) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(ferias.getUsuarioId());
            if (usuarioOpt.isPresent()) {
                String departamento = usuarioOpt.get().getDepartamento();
                if (departamento != null) {
                    relatorio.merge(departamento, 1L, Long::sum);
                }
            }
        }
        
        return relatorio;
    }
    
    /**
     * Valida solicitação de férias
     */
    private void validarSolicitacaoFerias(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        validarSolicitacaoFerias(usuarioId, dataInicio, dataFim, null);
    }
    
    /**
     * Valida solicitação de férias com exclusão de ID
     */
    private void validarSolicitacaoFerias(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, Long feriasIdExcluir) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início");
        }
        
        if (dataInicio.isBefore(LocalDate.now().plusDays(ANTECEDENCIA_MINIMA_DIAS))) {
            throw new IllegalArgumentException("Férias devem ser solicitadas com pelo menos " + 
                                             ANTECEDENCIA_MINIMA_DIAS + " dias de antecedência");
        }
        
        // Verifica duração máxima (30 dias)
        if (dataInicio.until(dataFim).getDays() + 1 > DIAS_FERIAS_ANO) {
            throw new IllegalArgumentException("Período de férias não pode exceder " + DIAS_FERIAS_ANO + " dias");
        }
        
        // Verifica conflitos
        if (feriasRepository.temConflitoFerias(usuarioId, dataInicio, dataFim, feriasIdExcluir)) {
            throw new IllegalArgumentException("Conflito com outras férias já solicitadas ou aprovadas");
        }
        
        // Verifica saldo disponível
        int saldoFerias = calcularSaldoFerias(usuarioId);
        int diasSolicitados = (int) (dataInicio.until(dataFim).getDays() + 1);
        
        if (diasSolicitados > saldoFerias) {
            throw new IllegalArgumentException("Saldo insuficiente de férias. Disponível: " + saldoFerias + 
                                             " dias, solicitado: " + diasSolicitados + " dias");
        }
    }
    
    /**
     * Conta total de solicitações pendentes
     */
    public long contarFeriasPendentes() {
        return feriasRepository.findFeriasPendentes().size();
    }
    
    /**
     * Busca por ID
     */
    public Optional<Ferias> buscarPorId(Long id) {
        return feriasRepository.findById(id);
    }
}
