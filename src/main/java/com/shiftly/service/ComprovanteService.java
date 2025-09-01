package com.shiftly.service;

import com.shiftly.model.Comprovante;
import com.shiftly.model.TipoComprovante;
import com.shiftly.model.Usuario;
import com.shiftly.repository.ComprovanteRepository;
import com.shiftly.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar comprovantes de pagamento
 */
public class ComprovanteService {
    
    private static final Logger logger = LoggerFactory.getLogger(ComprovanteService.class);
    
    private final ComprovanteRepository comprovanteRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Configurações de arquivo
    private static final String DIRETORIO_COMPROVANTES = "comprovantes";
    private static final long MAX_TAMANHO_ARQUIVO = 10 * 1024 * 1024; // 10MB
    private static final String[] EXTENSOES_PERMITIDAS = {".pdf", ".jpg", ".jpeg", ".png"};
    
    public ComprovanteService() {
        this.comprovanteRepository = new ComprovanteRepository();
        this.usuarioRepository = new UsuarioRepository();
        criarDiretorioComprovantes();
    }
    
    /**
     * Cria um novo comprovante
     */
    public Comprovante criarComprovante(Comprovante comprovante, Long usuarioRhId) {
        logger.info("Criando comprovante para usuário ID {} por RH ID {}", comprovante.getUsuarioId(), usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para criar comprovantes");
        }
        
        // Verifica se o usuário beneficiário existe
        if (!usuarioRepository.existsById(comprovante.getUsuarioId())) {
            throw new IllegalArgumentException("Usuário beneficiário não encontrado");
        }
        
        // Validações
        validarComprovante(comprovante);
        
        // Verifica se já existe comprovante com a mesma referência
        if (comprovanteRepository.existsByReferencia(comprovante.getReferencia())) {
            throw new IllegalArgumentException("Já existe comprovante com esta referência: " + comprovante.getReferencia());
        }
        
        // Define quem criou
        comprovante.setCriadoPorUsuarioId(usuarioRhId);
        
        // Calcula valor líquido se não foi informado
        if (comprovante.getValorLiquido() == null) {
            comprovante.calcularValorLiquido();
        }
        
        Comprovante comprovanteSalvo = comprovanteRepository.save(comprovante);
        logger.info("Comprovante criado com sucesso: ID {}", comprovanteSalvo.getId());
        
        return comprovanteSalvo;
    }
    
    /**
     * Atualiza um comprovante existente
     */
    public Comprovante atualizarComprovante(Comprovante comprovante, Long usuarioRhId) {
        logger.info("Atualizando comprovante ID {} por RH ID {}", comprovante.getId(), usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para atualizar comprovantes");
        }
        
        // Verifica se o comprovante existe
        if (!comprovanteRepository.existsById(comprovante.getId())) {
            throw new IllegalArgumentException("Comprovante não encontrado: " + comprovante.getId());
        }
        
        // Validações
        validarComprovante(comprovante);
        
        // Verifica se já existe outro comprovante com a mesma referência
        Optional<Comprovante> comprovanteComReferencia = comprovanteRepository.findByReferencia(comprovante.getReferencia());
        if (comprovanteComReferencia.isPresent() && !comprovanteComReferencia.get().getId().equals(comprovante.getId())) {
            throw new IllegalArgumentException("Já existe outro comprovante com esta referência: " + comprovante.getReferencia());
        }
        
        // Calcula valor líquido
        comprovante.calcularValorLiquido();
        
        Comprovante comprovanteAtualizado = comprovanteRepository.save(comprovante);
        logger.info("Comprovante atualizado com sucesso: ID {}", comprovanteAtualizado.getId());
        
        return comprovanteAtualizado;
    }
    
    /**
     * Anexa arquivo ao comprovante
     */
    public Comprovante anexarArquivo(Long comprovanteId, File arquivo, Long usuarioRhId) throws IOException {
        logger.info("Anexando arquivo ao comprovante ID {} por RH ID {}", comprovanteId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para anexar arquivos");
        }
        
        // Busca o comprovante
        Optional<Comprovante> comprovanteOpt = comprovanteRepository.findById(comprovanteId);
        if (comprovanteOpt.isEmpty()) {
            throw new IllegalArgumentException("Comprovante não encontrado");
        }
        
        // Valida arquivo
        validarArquivo(arquivo);
        
        // Salva arquivo
        String caminhoArquivo = salvarArquivo(arquivo, comprovanteId);
        
        // Atualiza comprovante
        Comprovante comprovante = comprovanteOpt.get();
        comprovante.setCaminhoArquivo(caminhoArquivo);
        comprovante.setNomeArquivo(arquivo.getName());
        comprovante.setTamanhoArquivo(arquivo.length());
        
        Comprovante comprovanteAtualizado = comprovanteRepository.save(comprovante);
        logger.info("Arquivo anexado com sucesso ao comprovante ID {}", comprovanteId);
        
        return comprovanteAtualizado;
    }
    
    /**
     * Busca comprovantes por usuário
     */
    public List<Comprovante> buscarComprovantesPorUsuario(Long usuarioId) {
        return comprovanteRepository.findByUsuarioId(usuarioId);
    }
    
    /**
     * Busca comprovantes por tipo
     */
    public List<Comprovante> buscarComprovantesPorTipo(TipoComprovante tipo) {
        return comprovanteRepository.findByTipoComprovante(tipo);
    }
    
    /**
     * Busca comprovantes por usuário e tipo
     */
    public List<Comprovante> buscarComprovantesPorUsuarioETipo(Long usuarioId, TipoComprovante tipo) {
        return comprovanteRepository.findByUsuarioIdAndTipo(usuarioId, tipo);
    }
    
    /**
     * Busca comprovantes em um período
     */
    public List<Comprovante> buscarComprovantesNoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return comprovanteRepository.findByPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Busca comprovantes por usuário em um período
     */
    public List<Comprovante> buscarComprovantesPorUsuarioEPeriodo(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        return comprovanteRepository.findByUsuarioIdAndPeriodo(usuarioId, dataInicio, dataFim);
    }
    
    /**
     * Busca últimos comprovantes de um usuário
     */
    public List<Comprovante> buscarUltimosComprovantes(Long usuarioId, int limite) {
        return comprovanteRepository.findUltimosComprovantesByUsuarioId(usuarioId, limite);
    }
    
    /**
     * Calcula valor total pago a um usuário
     */
    public Double calcularValorTotalPago(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        return comprovanteRepository.calcularValorTotalPago(usuarioId, dataInicio, dataFim);
    }
    
    /**
     * Gera relatório de pagamentos
     */
    public java.util.Map<String, Object> relatorioPageamentos(LocalDate dataInicio, LocalDate dataFim) {
        List<Comprovante> comprovantes = buscarComprovantesNoPeriodo(dataInicio, dataFim);
        
        java.util.Map<String, Object> relatorio = new java.util.HashMap<>();
        java.util.Map<TipoComprovante, Double> porTipo = new java.util.HashMap<>();
        java.util.Map<String, Double> porDepartamento = new java.util.HashMap<>();
        
        double valorTotal = 0.0;
        
        for (Comprovante comprovante : comprovantes) {
            valorTotal += comprovante.getValorLiquido();
            
            // Por tipo
            porTipo.merge(comprovante.getTipoComprovante(), comprovante.getValorLiquido(), Double::sum);
            
            // Por departamento
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(comprovante.getUsuarioId());
            if (usuarioOpt.isPresent() && usuarioOpt.get().getDepartamento() != null) {
                String departamento = usuarioOpt.get().getDepartamento();
                porDepartamento.merge(departamento, comprovante.getValorLiquido(), Double::sum);
            }
        }
        
        relatorio.put("totalComprovantes", comprovantes.size());
        relatorio.put("valorTotal", valorTotal);
        relatorio.put("porTipo", porTipo);
        relatorio.put("porDepartamento", porDepartamento);
        
        return relatorio;
    }
    
    /**
     * Busca comprovante por ID
     */
    public Optional<Comprovante> buscarPorId(Long id) {
        return comprovanteRepository.findById(id);
    }
    
    /**
     * Busca comprovante por referência
     */
    public Optional<Comprovante> buscarPorReferencia(String referencia) {
        return comprovanteRepository.findByReferencia(referencia);
    }
    
    /**
     * Deleta um comprovante
     */
    public boolean deletarComprovante(Long comprovanteId, Long usuarioRhId) {
        logger.info("Deletando comprovante ID {} por RH ID {}", comprovanteId, usuarioRhId);
        
        // Verifica permissões
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para deletar comprovantes");
        }
        
        // Busca o comprovante para deletar arquivo se existir
        Optional<Comprovante> comprovanteOpt = comprovanteRepository.findById(comprovanteId);
        if (comprovanteOpt.isPresent()) {
            Comprovante comprovante = comprovanteOpt.get();
            
            // Deleta arquivo se existir
            if (comprovante.temArquivo()) {
                try {
                    Files.deleteIfExists(Paths.get(comprovante.getCaminhoArquivo()));
                    logger.info("Arquivo do comprovante deletado: {}", comprovante.getCaminhoArquivo());
                } catch (IOException e) {
                    logger.warn("Erro ao deletar arquivo do comprovante: {}", e.getMessage());
                }
            }
        }
        
        boolean deletado = comprovanteRepository.deleteById(comprovanteId);
        if (deletado) {
            logger.info("Comprovante deletado com sucesso: ID {}", comprovanteId);
        } else {
            logger.warn("Comprovante não encontrado para deleção: ID {}", comprovanteId);
        }
        
        return deletado;
    }
    
    /**
     * Obtém caminho do arquivo do comprovante
     */
    public String obterCaminhoArquivo(Long comprovanteId) {
        Optional<Comprovante> comprovanteOpt = comprovanteRepository.findById(comprovanteId);
        if (comprovanteOpt.isPresent() && comprovanteOpt.get().temArquivo()) {
            return comprovanteOpt.get().getCaminhoArquivo();
        }
        return null;
    }
    
    /**
     * Valida dados do comprovante
     */
    private void validarComprovante(Comprovante comprovante) {
        if (comprovante == null) {
            throw new IllegalArgumentException("Comprovante não pode ser nulo");
        }
        
        if (comprovante.getUsuarioId() == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        
        if (comprovante.getTipoComprovante() == null) {
            throw new IllegalArgumentException("Tipo de comprovante é obrigatório");
        }
        
        if (comprovante.getReferencia() == null || comprovante.getReferencia().trim().isEmpty()) {
            throw new IllegalArgumentException("Referência é obrigatória");
        }
        
        if (comprovante.getDataEmissao() == null) {
            throw new IllegalArgumentException("Data de emissão é obrigatória");
        }
        
        if (comprovante.getPeriodoInicio() == null) {
            throw new IllegalArgumentException("Período inicial é obrigatório");
        }
        
        if (comprovante.getPeriodoFim() == null) {
            throw new IllegalArgumentException("Período final é obrigatório");
        }
        
        if (comprovante.getPeriodoFim().isBefore(comprovante.getPeriodoInicio())) {
            throw new IllegalArgumentException("Período final não pode ser anterior ao inicial");
        }
        
        if (comprovante.getValorBruto() == null || comprovante.getValorBruto() <= 0) {
            throw new IllegalArgumentException("Valor bruto deve ser positivo");
        }
        
        if (comprovante.getValorLiquido() != null && comprovante.getValorLiquido() <= 0) {
            throw new IllegalArgumentException("Valor líquido deve ser positivo");
        }
    }
    
    /**
     * Valida arquivo anexado
     */
    private void validarArquivo(File arquivo) {
        if (arquivo == null || !arquivo.exists()) {
            throw new IllegalArgumentException("Arquivo não existe");
        }
        
        if (arquivo.length() > MAX_TAMANHO_ARQUIVO) {
            throw new IllegalArgumentException("Arquivo muito grande. Máximo: " + (MAX_TAMANHO_ARQUIVO / 1024 / 1024) + "MB");
        }
        
        String nomeArquivo = arquivo.getName().toLowerCase();
        boolean extensaoValida = false;
        for (String extensao : EXTENSOES_PERMITIDAS) {
            if (nomeArquivo.endsWith(extensao)) {
                extensaoValida = true;
                break;
            }
        }
        
        if (!extensaoValida) {
            throw new IllegalArgumentException("Extensão não permitida. Permitidas: " + String.join(", ", EXTENSOES_PERMITIDAS));
        }
    }
    
    /**
     * Salva arquivo no sistema
     */
    private String salvarArquivo(File arquivo, Long comprovanteId) throws IOException {
        String nomeArquivo = String.format("%d_%s_%s", 
                                          comprovanteId,
                                          LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                                          arquivo.getName());
        
        Path caminhoDestino = Paths.get(DIRETORIO_COMPROVANTES, nomeArquivo);
        Files.copy(arquivo.toPath(), caminhoDestino, StandardCopyOption.REPLACE_EXISTING);
        
        return caminhoDestino.toString();
    }
    
    /**
     * Cria diretório para comprovantes se não existir
     */
    private void criarDiretorioComprovantes() {
        try {
            Path diretorio = Paths.get(DIRETORIO_COMPROVANTES);
            if (!Files.exists(diretorio)) {
                Files.createDirectories(diretorio);
                logger.info("Diretório de comprovantes criado: {}", diretorio.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Erro ao criar diretório de comprovantes: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Conta total de comprovantes
     */
    public long contarComprovantes() {
        return comprovanteRepository.count();
    }
    
    /**
     * Lista todos os comprovantes
     */
    public List<Comprovante> listarTodos() {
        return comprovanteRepository.findAll();
    }
}
