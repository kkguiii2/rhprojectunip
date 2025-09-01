package com.shiftly.service;

import com.shiftly.model.HorasExtras;
import com.shiftly.model.StatusHorasExtras;
import com.shiftly.model.Usuario;
import com.shiftly.repository.HorasExtrasRepository;
import com.shiftly.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar horas extras
 */
public class HorasExtrasService {
    
    private static final Logger logger = LoggerFactory.getLogger(HorasExtrasService.class);
    
    private final HorasExtrasRepository horasExtrasRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Configurações de horas extras
    private static final double MAX_HORAS_EXTRAS_DIA = 4.0;
    private static final double MAX_HORAS_EXTRAS_MES = 60.0;
    private static final double ADICIONAL_HORAS_EXTRAS = 0.5; // 50% de adicional
    
    public HorasExtrasService() {
        this.horasExtrasRepository = new HorasExtrasRepository();
        this.usuarioRepository = new UsuarioRepository();
    }
    
    /**
     * Registra horas extras
     */
    public HorasExtras registrarHorasExtras(Long usuarioId, LocalDate data, Double horas, 
                                          String descricao, String justificativa) {
        logger.info("Registrando horas extras para usuário ID {} - Data: {} - Horas: {}", usuarioId, data, horas);
        
        // Verifica se o usuário existe e está ativo
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getAtivo()) {
            throw new IllegalArgumentException("Usuário não encontrado ou inativo");
        }
        
        // Validações
        validarHorasExtras(usuarioId, data, horas);
        
        // Cria o registro
        HorasExtras horasExtras = new HorasExtras(usuarioId, data, horas, descricao, justificativa);
        
        HorasExtras horasExtrasSalvas = horasExtrasRepository.save(horasExtras);
        logger.info("Horas extras registradas com sucesso: ID {}", horasExtrasSalvas.getId());
        
        return horasExtrasSalvas;
    }
    
    /**
     * Aprova horas extras
     */
    public HorasExtras aprovarHorasExtras(Long horasExtrasId, Long usuarioRhId) {
        logger.info("Aprovando horas extras ID {} por usuário RH ID {}", horasExtrasId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para aprovar horas extras");
        }
        
        // Busca as horas extras
        Optional<HorasExtras> horasExtrasOpt = horasExtrasRepository.findById(horasExtrasId);
        if (horasExtrasOpt.isEmpty()) {
            throw new IllegalArgumentException("Horas extras não encontradas");
        }
        
        HorasExtras horasExtras = horasExtrasOpt.get();
        
        // Verifica se pode ser aprovada
        if (!horasExtras.isPendente()) {
            throw new IllegalStateException("Apenas horas extras pendentes podem ser aprovadas");
        }
        
        // Aprova
        horasExtras.aprovar(usuarioRhId);
        
        HorasExtras horasExtrasAprovadas = horasExtrasRepository.save(horasExtras);
        logger.info("Horas extras aprovadas com sucesso: ID {}", horasExtrasAprovadas.getId());
        
        return horasExtrasAprovadas;
    }
    
    /**
     * Recusa horas extras
     */
    public HorasExtras recusarHorasExtras(Long horasExtrasId, String motivo, Long usuarioRhId) {
        logger.info("Recusando horas extras ID {} por usuário RH ID {}", horasExtrasId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para recusar horas extras");
        }
        
        // Busca as horas extras
        Optional<HorasExtras> horasExtrasOpt = horasExtrasRepository.findById(horasExtrasId);
        if (horasExtrasOpt.isEmpty()) {
            throw new IllegalArgumentException("Horas extras não encontradas");
        }
        
        HorasExtras horasExtras = horasExtrasOpt.get();
        
        // Verifica se pode ser recusada
        if (!horasExtras.isPendente()) {
            throw new IllegalStateException("Apenas horas extras pendentes podem ser recusadas");
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da recusa é obrigatório");
        }
        
        // Recusa
        horasExtras.recusar(usuarioRhId, motivo);
        
        HorasExtras horasExtrasRecusadas = horasExtrasRepository.save(horasExtras);
        logger.info("Horas extras recusadas com sucesso: ID {}", horasExtrasRecusadas.getId());
        
        return horasExtrasRecusadas;
    }
    
    /**
     * Marca horas extras como pagas
     */
    public HorasExtras marcarComoPago(Long horasExtrasId, Double valorPago, Long usuarioRhId) {
        logger.info("Marcando horas extras ID {} como pagas por usuário RH ID {}", horasExtrasId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para marcar pagamentos");
        }
        
        // Busca as horas extras
        Optional<HorasExtras> horasExtrasOpt = horasExtrasRepository.findById(horasExtrasId);
        if (horasExtrasOpt.isEmpty()) {
            throw new IllegalArgumentException("Horas extras não encontradas");
        }
        
        HorasExtras horasExtras = horasExtrasOpt.get();
        
        // Verifica se pode ser marcada como paga
        if (!horasExtras.isAprovada()) {
            throw new IllegalStateException("Apenas horas extras aprovadas podem ser marcadas como pagas");
        }
        
        if (horasExtras.isPaga()) {
            throw new IllegalStateException("Horas extras já foram pagas");
        }
        
        // Marca como paga
        horasExtras.marcarComoPago(valorPago);
        
        HorasExtras horasExtrasPagas = horasExtrasRepository.save(horasExtras);
        logger.info("Horas extras marcadas como pagas: ID {}", horasExtrasPagas.getId());
        
        return horasExtrasPagas;
    }
    
    /**
     * Edita horas extras pendentes
     */
    public HorasExtras editarHorasExtras(Long horasExtrasId, LocalDate novaData, Double novasHoras,
                                        String novaDescricao, String novaJustificativa, Long usuarioId) {
        logger.info("Editando horas extras ID {} por usuário ID {}", horasExtrasId, usuarioId);
        
        // Busca as horas extras
        Optional<HorasExtras> horasExtrasOpt = horasExtrasRepository.findById(horasExtrasId);
        if (horasExtrasOpt.isEmpty()) {
            throw new IllegalArgumentException("Horas extras não encontradas");
        }
        
        HorasExtras horasExtras = horasExtrasOpt.get();
        
        // Verifica se é o próprio usuário
        if (!horasExtras.getUsuarioId().equals(usuarioId)) {
            throw new SecurityException("Usuário não pode editar horas extras de outro usuário");
        }
        
        // Verifica se pode ser editada
        if (!horasExtras.getStatus().permiteEdicao()) {
            throw new IllegalStateException("Horas extras não podem ser editadas neste status: " + horasExtras.getStatus());
        }
        
        // Valida novos dados
        validarHorasExtras(usuarioId, novaData, novasHoras, horasExtrasId);
        
        // Atualiza
        horasExtras.setData(novaData);
        horasExtras.setHoras(novasHoras);
        horasExtras.setDescricao(novaDescricao);
        horasExtras.setJustificativa(novaJustificativa);
        
        HorasExtras horasExtrasEditadas = horasExtrasRepository.save(horasExtras);
        logger.info("Horas extras editadas com sucesso: ID {}", horasExtrasEditadas.getId());
        
        return horasExtrasEditadas;
    }
    
    /**
     * Busca horas extras por usuário
     */
    public List<HorasExtras> buscarHorasExtrasPorUsuario(Long usuarioId) {
        return horasExtrasRepository.findByUsuarioId(usuarioId);
    }
    
    /**
     * Busca horas extras pendentes
     */
    public List<HorasExtras> buscarHorasExtrasPendentes() {
        return horasExtrasRepository.findHorasExtrasPendentes();
    }
    
    /**
     * Busca horas extras aprovadas
     */
    public List<HorasExtras> buscarHorasExtrasAprovadas() {
        return horasExtrasRepository.findHorasExtrasAprovadas();
    }
    
    /**
     * Busca horas extras não pagas
     */
    public List<HorasExtras> buscarHorasExtrasNaoPagas() {
        return horasExtrasRepository.findHorasExtrasNaoPagas();
    }
    
    /**
     * Busca horas extras por usuário e período
     */
    public List<HorasExtras> buscarHorasExtrasPorUsuarioEPeriodo(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        return horasExtrasRepository.findByUsuarioIdAndPeriodo(usuarioId, dataInicio, dataFim);
    }
    
    /**
     * Calcula saldo de horas extras de um usuário
     */
    public Double calcularSaldoHorasExtras(Long usuarioId) {
        List<HorasExtras> horasExtrasAprovadas = horasExtrasRepository.findByUsuarioIdAndStatus(usuarioId, StatusHorasExtras.APROVADA);
        
        double totalAprovadas = horasExtrasAprovadas.stream()
                .mapToDouble(HorasExtras::getHoras)
                .sum();
        
        double totalPagas = horasExtrasAprovadas.stream()
                .filter(HorasExtras::isPaga)
                .mapToDouble(HorasExtras::getHoras)
                .sum();
        
        return totalAprovadas - totalPagas;
    }
    
    /**
     * Calcula valor a receber em horas extras
     */
    public Double calcularValorAReceber(Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getSalario() == null) {
            return 0.0;
        }
        
        Double salario = usuarioOpt.get().getSalario();
        Double valorHora = salario / 220.0; // 220 horas mensais padrão
        
        return horasExtrasRepository.calcularValorTotalAPagar(usuarioId, valorHora);
    }
    
    /**
     * Relatório de horas extras por mês
     */
    public Double calcularTotalHorasExtrasNoMes(Long usuarioId, int ano, int mes) {
        return horasExtrasRepository.calcularTotalHorasExtrasNoMes(usuarioId, ano, mes);
    }
    
    /**
     * Relatório geral de horas extras
     */
    public java.util.Map<String, Object> relatorioHorasExtras() {
        java.util.Map<String, Object> relatorio = new java.util.HashMap<>();
        
        List<HorasExtras> pendentes = buscarHorasExtrasPendentes();
        List<HorasExtras> aprovadas = buscarHorasExtrasAprovadas();
        List<HorasExtras> naoPagas = buscarHorasExtrasNaoPagas();
        
        relatorio.put("totalPendentes", pendentes.size());
        relatorio.put("totalAprovadas", aprovadas.size());
        relatorio.put("totalNaoPagas", naoPagas.size());
        
        double horasPendentes = pendentes.stream().mapToDouble(HorasExtras::getHoras).sum();
        double horasAprovadas = aprovadas.stream().mapToDouble(HorasExtras::getHoras).sum();
        double horasNaoPagas = naoPagas.stream().mapToDouble(HorasExtras::getHoras).sum();
        
        relatorio.put("horasPendentes", horasPendentes);
        relatorio.put("horasAprovadas", horasAprovadas);
        relatorio.put("horasNaoPagas", horasNaoPagas);
        
        return relatorio;
    }
    
    /**
     * Valida registro de horas extras
     */
    private void validarHorasExtras(Long usuarioId, LocalDate data, Double horas) {
        validarHorasExtras(usuarioId, data, horas, null);
    }
    
    /**
     * Valida registro de horas extras com exclusão de ID
     */
    private void validarHorasExtras(Long usuarioId, LocalDate data, Double horas, Long horasExtrasIdExcluir) {
        if (data == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        
        if (horas == null || horas <= 0) {
            throw new IllegalArgumentException("Quantidade de horas deve ser positiva");
        }
        
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Não é possível registrar horas extras para datas futuras");
        }
        
        if (data.isBefore(LocalDate.now().minusDays(7))) {
            throw new IllegalArgumentException("Horas extras devem ser registradas em até 7 dias");
        }
        
        // Verifica limite diário
        if (horas > MAX_HORAS_EXTRAS_DIA) {
            throw new IllegalArgumentException("Limite diário de horas extras excedido. Máximo: " + MAX_HORAS_EXTRAS_DIA + " horas");
        }
        
        // Verifica se já existe registro para a data (excluindo o próprio se for edição)
        List<HorasExtras> horasExtrasDaData = horasExtrasRepository.findByUsuarioIdAndPeriodo(usuarioId, data, data);
        if (horasExtrasIdExcluir != null) {
            horasExtrasDaData = horasExtrasDaData.stream()
                    .filter(h -> !h.getId().equals(horasExtrasIdExcluir))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        if (!horasExtrasDaData.isEmpty()) {
            throw new IllegalArgumentException("Já existe registro de horas extras para esta data");
        }
        
        // Verifica limite mensal
        Double totalMes = horasExtrasRepository.calcularTotalHorasExtrasNoMes(usuarioId, data.getYear(), data.getMonthValue());
        if (totalMes + horas > MAX_HORAS_EXTRAS_MES) {
            throw new IllegalArgumentException("Limite mensal de horas extras excedido. Disponível: " + 
                                             (MAX_HORAS_EXTRAS_MES - totalMes) + " horas");
        }
    }
    
    /**
     * Conta horas extras pendentes
     */
    public long contarHorasExtrasPendentes() {
        return buscarHorasExtrasPendentes().size();
    }
    
    /**
     * Busca por ID
     */
    public Optional<HorasExtras> buscarPorId(Long id) {
        return horasExtrasRepository.findById(id);
    }
    
    /**
     * Deleta horas extras (apenas se pendente)
     */
    public boolean deletarHorasExtras(Long horasExtrasId, Long usuarioId) {
        logger.info("Deletando horas extras ID {} por usuário ID {}", horasExtrasId, usuarioId);
        
        // Busca as horas extras
        Optional<HorasExtras> horasExtrasOpt = horasExtrasRepository.findById(horasExtrasId);
        if (horasExtrasOpt.isEmpty()) {
            throw new IllegalArgumentException("Horas extras não encontradas");
        }
        
        HorasExtras horasExtras = horasExtrasOpt.get();
        
        // Verifica se é o próprio usuário ou RH
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        if (!horasExtras.getUsuarioId().equals(usuarioId) && !usuario.isRH()) {
            throw new SecurityException("Usuário não tem permissão para deletar estas horas extras");
        }
        
        // Verifica se pode ser deletada
        if (!horasExtras.getStatus().permiteEdicao() && !usuario.isRH()) {
            throw new IllegalStateException("Horas extras não podem ser deletadas neste status: " + horasExtras.getStatus());
        }
        
        boolean deletado = horasExtrasRepository.deleteById(horasExtrasId);
        if (deletado) {
            logger.info("Horas extras deletadas com sucesso: ID {}", horasExtrasId);
        } else {
            logger.warn("Horas extras não encontradas para deleção: ID {}", horasExtrasId);
        }
        
        return deletado;
    }
}
