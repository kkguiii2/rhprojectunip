package com.shiftly.service;

import com.shiftly.model.Ponto;
import com.shiftly.model.TipoPonto;
import com.shiftly.model.Usuario;
import com.shiftly.repository.PontoRepository;
import com.shiftly.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar registros de ponto
 */
public class PontoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PontoService.class);
    
    private final PontoRepository pontoRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Configurações de horário
    private static final LocalTime HORARIO_ENTRADA_PADRAO = LocalTime.of(8, 0);
    private static final LocalTime HORARIO_SAIDA_PADRAO = LocalTime.of(17, 0);
    private static final int TOLERANCIA_MINUTOS = 15;
    
    public PontoService() {
        this.pontoRepository = new PontoRepository();
        this.usuarioRepository = new UsuarioRepository();
    }
    
    /**
     * Salva um ponto completo
     */
    public Ponto salvarPonto(Ponto ponto) {
        logger.info("Salvando ponto para usuário ID {} - Tipo: {}", ponto.getUsuarioId(), ponto.getTipoPonto());
        
        // Validações básicas
        if (ponto.getUsuarioId() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }
        
        if (ponto.getTipoPonto() == null) {
            throw new IllegalArgumentException("Tipo de ponto é obrigatório");
        }
        
        if (ponto.getDataHora() == null) {
            ponto.setDataHora(LocalDateTime.now());
        }
        
        // Salvar no repositório
        Ponto pontoSalvo = pontoRepository.save(ponto);
        
        logger.info("Ponto salvo com sucesso - ID: {}", pontoSalvo.getId());
        return pontoSalvo;
    }
    
    /**
     * Registra um ponto
     */
    public Ponto registrarPonto(Long usuarioId, TipoPonto tipoPonto, Double latitude, Double longitude, String faceMatch) {
        logger.info("Registrando ponto para usuário ID {} - Tipo: {}", usuarioId, tipoPonto);
        
        // Verifica se o usuário existe e está ativo
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().getAtivo()) {
            throw new IllegalArgumentException("Usuário não encontrado ou inativo");
        }
        
        // Valida se o tipo de ponto é válido para o momento
        validarTipoPonto(usuarioId, tipoPonto);
        
        // Cria o registro de ponto
        Ponto ponto = new Ponto();
        ponto.setUsuarioId(usuarioId);
        ponto.setDataHora(LocalDateTime.now());
        ponto.setTipoPonto(tipoPonto);
        ponto.setLatitude(latitude);
        ponto.setLongitude(longitude);
        ponto.setFaceMatch(faceMatch);
        ponto.setFaceValidada(faceMatch != null && !faceMatch.isEmpty());
        ponto.setManual(false);
        
        // TODO: Implementar validação de geolocalização
        if (latitude != null && longitude != null) {
            // Aqui seria validado se a localização está dentro da área permitida
            ponto.setEndereco(obterEnderecoDeLatLong(latitude, longitude));
        }
        
        Ponto pontoSalvo = pontoRepository.save(ponto);
        logger.info("Ponto registrado com sucesso: ID {}", pontoSalvo.getId());
        
        return pontoSalvo;
    }
    
    /**
     * Registra ponto manualmente (pelo RH)
     */
    public Ponto registrarPontoManual(Long usuarioId, LocalDateTime dataHora, TipoPonto tipoPonto, 
                                     String observacoes, Long usuarioRhId) {
        logger.info("Registrando ponto manual para usuário ID {} por RH ID {}", usuarioId, usuarioRhId);
        
        // Verifica permissões do usuário RH
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para registrar pontos manuais");
        }
        
        // Verifica se o usuário existe
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        // Cria o registro manual
        Ponto ponto = new Ponto();
        ponto.setUsuarioId(usuarioId);
        ponto.setDataHora(dataHora);
        ponto.setTipoPonto(tipoPonto);
        ponto.setObservacoes(observacoes);
        ponto.setManual(true);
        ponto.setCorrigidoPorUsuarioId(usuarioRhId);
        ponto.setDataCorrecao(LocalDateTime.now());
        ponto.setMotivoCorrecao("Registro manual pelo RH");
        
        Ponto pontoSalvo = pontoRepository.save(ponto);
        logger.info("Ponto manual registrado com sucesso: ID {}", pontoSalvo.getId());
        
        return pontoSalvo;
    }
    
    /**
     * Corrige um ponto existente
     */
    public Ponto corrigirPonto(Long pontoId, LocalDateTime novaDataHora, String motivo, Long usuarioRhId) {
        logger.info("Corrigindo ponto ID {} por usuário RH ID {}", pontoId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para corrigir pontos");
        }
        
        // Busca o ponto
        Optional<Ponto> pontoOpt = pontoRepository.findById(pontoId);
        if (pontoOpt.isEmpty()) {
            throw new IllegalArgumentException("Ponto não encontrado");
        }
        
        Ponto ponto = pontoOpt.get();
        ponto.setDataHora(novaDataHora);
        ponto.corrigir(usuarioRhId, motivo);
        
        Ponto pontoCorrigido = pontoRepository.save(ponto);
        logger.info("Ponto corrigido com sucesso: ID {}", pontoCorrigido.getId());
        
        return pontoCorrigido;
    }
    
    /**
     * Busca pontos de um usuário em uma data
     */
    public List<Ponto> buscarPontosPorUsuarioEData(Long usuarioId, LocalDate data) {
        return pontoRepository.findByUsuarioIdAndData(usuarioId, data);
    }
    
    /**
     * Busca pontos de um usuário em um período
     */
    public List<Ponto> buscarPontosPorUsuarioEPeriodo(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        return pontoRepository.findByUsuarioIdAndPeriodo(usuarioId, dataInicio, dataFim);
    }
    
    /**
     * Busca histórico de pontos de um usuário
     */
    public List<Ponto> buscarHistoricoPontos(Long usuarioId) {
        return pontoRepository.findByUsuarioId(usuarioId);
    }
    
    /**
     * Busca o último ponto de um usuário
     */
    public Optional<Ponto> buscarUltimoPonto(Long usuarioId) {
        return pontoRepository.findUltimoPontoByUsuarioId(usuarioId);
    }
    
    /**
     * Verifica qual deve ser o próximo tipo de ponto
     */
    public TipoPonto obterProximoTipoPonto(Long usuarioId) {
        Optional<Ponto> ultimoPontoOpt = buscarUltimoPonto(usuarioId);
        
        if (ultimoPontoOpt.isEmpty()) {
            return TipoPonto.ENTRADA;
        }
        
        Ponto ultimoPonto = ultimoPontoOpt.get();
        
        // Se o último ponto foi hoje, continua a sequência
        if (ultimoPonto.getDataHora().toLocalDate().equals(LocalDate.now())) {
            switch (ultimoPonto.getTipoPonto()) {
                case ENTRADA:
                    return TipoPonto.SAIDA_ALMOCO;
                case SAIDA_ALMOCO:
                    return TipoPonto.VOLTA_ALMOCO;
                case VOLTA_ALMOCO:
                    return TipoPonto.SAIDA;
                case SAIDA:
                    return TipoPonto.ENTRADA_EXTRA;
                case ENTRADA_EXTRA:
                    return TipoPonto.SAIDA_EXTRA;
                case SAIDA_EXTRA:
                    return TipoPonto.ENTRADA_EXTRA; // Permite múltiplas horas extras
            }
        }
        
        // Se o último ponto não foi hoje, inicia nova sequência
        return TipoPonto.ENTRADA;
    }
    
    /**
     * Calcula horas trabalhadas em uma data
     */
    public double calcularHorasTrabalhadas(Long usuarioId, LocalDate data) {
        return pontoRepository.calcularHorasTrabalhadasNaData(usuarioId, data);
    }
    
    /**
     * Calcula horas trabalhadas em um período
     */
    public double calcularHorasTrabalhadasNoPeriodo(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        double totalHoras = 0.0;
        
        LocalDate dataAtual = dataInicio;
        while (!dataAtual.isAfter(dataFim)) {
            totalHoras += calcularHorasTrabalhadas(usuarioId, dataAtual);
            dataAtual = dataAtual.plusDays(1);
        }
        
        return totalHoras;
    }
    
    /**
     * Calcula horas extras em uma data
     */
    public double calcularHorasExtras(Long usuarioId, LocalDate data) {
        double horasTrabalhadas = calcularHorasTrabalhadas(usuarioId, data);
        double horasRegulares = 8.0; // 8 horas por dia padrão
        
        return Math.max(0, horasTrabalhadas - horasRegulares);
    }
    
    /**
     * Calcula saldo de horas do mês
     */
    public double calcularSaldoHorasMes(Long usuarioId, int ano, int mes) {
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        
        double horasTrabalhadas = calcularHorasTrabalhadasNoPeriodo(usuarioId, inicioMes, fimMes);
        
        // Calcula dias úteis do mês (segunda a sexta)
        int diasUteis = 0;
        LocalDate dataAtual = inicioMes;
        while (!dataAtual.isAfter(fimMes)) {
            if (dataAtual.getDayOfWeek().getValue() <= 5) { // 1=segunda, 5=sexta
                diasUteis++;
            }
            dataAtual = dataAtual.plusDays(1);
        }
        
        double horasEsperadas = diasUteis * 8.0;
        return horasTrabalhadas - horasEsperadas;
    }
    
    /**
     * Verifica se há inconsistências nos pontos
     */
    public List<String> verificarInconsistencias(Long usuarioId, LocalDate data) {
        List<Ponto> pontos = buscarPontosPorUsuarioEData(usuarioId, data);
        List<String> inconsistencias = new java.util.ArrayList<>();
        
        if (pontos.isEmpty()) {
            return inconsistencias;
        }
        
        // Verifica sequência de pontos
        TipoPonto tipoEsperado = TipoPonto.ENTRADA;
        for (Ponto ponto : pontos) {
            if (ponto.getTipoPonto() != tipoEsperado) {
                inconsistencias.add("Sequência de pontos incorreta: esperado " + tipoEsperado + 
                                  " mas encontrado " + ponto.getTipoPonto() + " às " + 
                                  ponto.getDataHora().toLocalTime());
            }
            
            // Atualiza próximo tipo esperado
            tipoEsperado = getProximoTipoEsperado(ponto.getTipoPonto());
            if (tipoEsperado == null) break;
        }
        
        // Verifica intervalos entre pontos
        for (int i = 1; i < pontos.size(); i++) {
            Duration intervalo = Duration.between(pontos.get(i-1).getDataHora(), pontos.get(i).getDataHora());
            
            if (intervalo.toMinutes() < 1) {
                inconsistencias.add("Intervalo muito curto entre pontos: " + 
                                  pontos.get(i-1).getDataHora().toLocalTime() + " e " + 
                                  pontos.get(i).getDataHora().toLocalTime());
            }
        }
        
        return inconsistencias;
    }
    
    /**
     * Valida se o tipo de ponto é válido no momento
     */
    private void validarTipoPonto(Long usuarioId, TipoPonto tipoPonto) {
        TipoPonto tipoEsperado = obterProximoTipoPonto(usuarioId);
        
        if (tipoPonto != tipoEsperado && tipoPonto != TipoPonto.ENTRADA_EXTRA && tipoPonto != TipoPonto.SAIDA_EXTRA) {
            throw new IllegalArgumentException("Tipo de ponto inválido. Esperado: " + tipoEsperado);
        }
    }
    
    /**
     * Obtém endereço a partir de latitude e longitude (mock)
     */
    private String obterEnderecoDeLatLong(Double latitude, Double longitude) {
        // TODO: Implementar integração com API de geolocalização
        return String.format("Lat: %.6f, Long: %.6f", latitude, longitude);
    }
    
    /**
     * Obtém o próximo tipo de ponto esperado
     */
    private TipoPonto getProximoTipoEsperado(TipoPonto tipoAtual) {
        switch (tipoAtual) {
            case ENTRADA:
                return TipoPonto.SAIDA_ALMOCO;
            case SAIDA_ALMOCO:
                return TipoPonto.VOLTA_ALMOCO;
            case VOLTA_ALMOCO:
                return TipoPonto.SAIDA;
            case SAIDA:
                return null; // Fim do expediente normal
            case ENTRADA_EXTRA:
                return TipoPonto.SAIDA_EXTRA;
            case SAIDA_EXTRA:
                return null; // Pode ter mais horas extras
            default:
                return null;
        }
    }
    
    /**
     * Busca pontos que precisam de validação
     */
    public List<Ponto> buscarPontosSemValidacao() {
        return pontoRepository.findPontosSemValidacaoFacial();
    }
    
    /**
     * Busca pontos manuais/corrigidos
     */
    public List<Ponto> buscarPontosManuais() {
        return pontoRepository.findPontosManuais();
    }
    
    /**
     * Deleta um ponto (apenas RH)
     */
    public boolean deletarPonto(Long pontoId, Long usuarioRhId) {
        logger.info("Deletando ponto ID {} por usuário RH ID {}", pontoId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para deletar pontos");
        }
        
        boolean deletado = pontoRepository.deleteById(pontoId);
        if (deletado) {
            logger.info("Ponto deletado com sucesso: ID {}", pontoId);
        } else {
            logger.warn("Ponto não encontrado para deleção: ID {}", pontoId);
        }
        
        return deletado;
    }
}
