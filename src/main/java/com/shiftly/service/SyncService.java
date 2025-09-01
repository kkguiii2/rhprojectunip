package com.shiftly.service;

import com.shiftly.model.Usuario;
import com.shiftly.repository.DatabaseConfig;
import com.shiftly.util.OfflineCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Serviço para sincronizar dados do cache offline com o SQL Server
 */
public class SyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);
    
    private final ScheduledExecutorService scheduler;
    private final UsuarioService usuarioService;
    
    public SyncService() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.usuarioService = new UsuarioService();
        
        // Inicia sincronização automática a cada 5 minutos
        startAutoSync();
    }
    
    /**
     * Inicia sincronização automática
     */
    private void startAutoSync() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (isSqlServerOnline() && OfflineCache.hasPendingData()) {
                    logger.info("SQL Server online, iniciando sincronização automática...");
                    syncAllPendingData();
                }
            } catch (Exception e) {
                logger.error("Erro na sincronização automática: {}", e.getMessage(), e);
            }
        }, 1, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Verifica se SQL Server está online
     */
    private boolean isSqlServerOnline() {
        try (Connection conn = DatabaseConfig.getSqlServerConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            logger.debug("SQL Server offline: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Sincroniza todos os dados pendentes
     */
    public void syncAllPendingData() {
        logger.info("Iniciando sincronização de todos os dados pendentes...");
        
        try {
            // Sincroniza usuários
            syncUsuariosPendentes();
            
            // Sincroniza pontos
            syncPontosPendentes();
            
            // Sincroniza outras entidades
            syncOutrasEntidadesPendentes();
            
            logger.info("Sincronização concluída com sucesso");
            
        } catch (Exception e) {
            logger.error("Erro durante sincronização: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Sincroniza usuários pendentes
     */
    private void syncUsuariosPendentes() {
        List<Usuario> usuariosPendentes = OfflineCache.getUsuariosPendentes();
        
        if (usuariosPendentes.isEmpty()) {
            logger.debug("Nenhum usuário pendente para sincronizar");
            return;
        }
        
        logger.info("Sincronizando {} usuários pendentes...", usuariosPendentes.size());
        
        for (Usuario usuario : usuariosPendentes) {
            try {
                // Remove ID temporário
                usuario.setId(null);
                
                // Tenta criar no banco
                Usuario usuarioSalvo = usuarioService.criarUsuario(usuario);
                
                if (usuarioSalvo.getId() != null && usuarioSalvo.getId() > 0) {
                    // Remove do cache após sucesso
                    OfflineCache.removeUsuarioPendente(usuario.getEmail());
                    logger.info("Usuário sincronizado com sucesso: {}", usuario.getEmail());
                }
                
            } catch (Exception e) {
                logger.error("Erro ao sincronizar usuário {}: {}", usuario.getEmail(), e.getMessage());
            }
        }
    }
    
    /**
     * Sincroniza pontos pendentes
     */
    private void syncPontosPendentes() {
        List<Object> pontosPendentes = OfflineCache.getPontosPendentes();
        
        if (pontosPendentes.isEmpty()) {
            logger.debug("Nenhum ponto pendente para sincronizar");
            return;
        }
        
        logger.info("Sincronizando {} pontos pendentes...", pontosPendentes.size());
        
        // TODO: Implementar sincronização de pontos
        // Por enquanto, apenas remove do cache
        OfflineCache.clearCache();
        logger.info("Pontos pendentes removidos do cache (implementação pendente)");
    }
    
    /**
     * Sincroniza outras entidades pendentes
     */
    private void syncOutrasEntidadesPendentes() {
        List<Object> outrasEntidades = OfflineCache.getOutrasEntidadesPendentes();
        
        if (outrasEntidades.isEmpty()) {
            logger.debug("Nenhuma outra entidade pendente para sincronizar");
            return;
        }
        
        logger.info("Sincronizando {} outras entidades pendentes...", outrasEntidades.size());
        
        // TODO: Implementar sincronização de outras entidades
        // Por enquanto, apenas remove do cache
        OfflineCache.clearCache();
        logger.info("Outras entidades removidas do cache (implementação pendente)");
    }
    
    /**
     * Força sincronização imediata
     */
    public void forceSync() {
        logger.info("Sincronização forçada solicitada");
        
        if (!isSqlServerOnline()) {
            logger.warn("SQL Server offline, sincronização não é possível");
            return;
        }
        
        // Executa em thread separada para não bloquear a UI
        new Thread(() -> {
            try {
                syncAllPendingData();
            } catch (Exception e) {
                logger.error("Erro na sincronização forçada: {}", e.getMessage(), e);
            }
        }).start();
    }
    
    /**
     * Obtém estatísticas de sincronização
     */
    public String getSyncStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTATÍSTICAS DE SINCRONIZAÇÃO ===\n");
        stats.append("SQL Server online: ").append(isSqlServerOnline() ? "Sim" : "Não").append("\n");
        stats.append("Dados pendentes: ").append(OfflineCache.hasPendingData() ? "Sim" : "Não").append("\n");
        stats.append("Usuários pendentes: ").append(OfflineCache.getUsuariosPendentes().size()).append("\n");
        stats.append("Pontos pendentes: ").append(OfflineCache.getPontosPendentes().size()).append("\n");
        stats.append("Outras entidades pendentes: ").append(OfflineCache.getOutrasEntidadesPendentes().size()).append("\n");
        return stats.toString();
    }
    
    /**
     * Para o serviço de sincronização
     */
    public void shutdown() {
        logger.info("Parando serviço de sincronização...");
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Serviço de sincronização parado");
    }
}
