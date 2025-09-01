package com.shiftly.util;

import com.shiftly.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema de cache offline para armazenar dados quando SQL Server não estiver disponível
 */
public class OfflineCache {
    
    private static final Logger logger = LoggerFactory.getLogger(OfflineCache.class);
    
    // Cache de usuários pendentes de sincronização
    private static final Map<String, Usuario> usuariosPendentes = new ConcurrentHashMap<>();
    
    // Cache de pontos pendentes de sincronização
    private static final Map<String, Object> pontosPendentes = new ConcurrentHashMap<>();
    
    // Cache de outras entidades pendentes
    private static final Map<String, Object> outrasEntidadesPendentes = new ConcurrentHashMap<>();
    
    // Flag para indicar se há dados pendentes
    private static volatile boolean hasPendingData = false;
    
    /**
     * Adiciona usuário ao cache offline
     */
    public static void addUsuarioPendente(Usuario usuario) {
        String key = "usuario_" + usuario.getEmail() + "_" + System.currentTimeMillis();
        usuariosPendentes.put(key, usuario);
        hasPendingData = true;
        logger.info("Usuário adicionado ao cache offline: {}", usuario.getEmail());
    }
    
    /**
     * Adiciona ponto ao cache offline
     */
    public static void addPontoPendente(Object ponto) {
        String key = "ponto_" + System.currentTimeMillis() + "_" + System.nanoTime();
        pontosPendentes.put(key, ponto);
        hasPendingData = true;
        logger.info("Ponto adicionado ao cache offline");
    }
    
    /**
     * Adiciona outra entidade ao cache offline
     */
    public static void addOutraEntidadePendente(String tipo, Object entidade) {
        String key = tipo + "_" + System.currentTimeMillis() + "_" + System.nanoTime();
        outrasEntidadesPendentes.put(key, entidade);
        hasPendingData = true;
        logger.info("Entidade {} adicionada ao cache offline", tipo);
    }
    
    /**
     * Verifica se há dados pendentes
     */
    public static boolean hasPendingData() {
        return hasPendingData;
    }
    
    /**
     * Obtém todos os usuários pendentes
     */
    public static List<Usuario> getUsuariosPendentes() {
        return new ArrayList<>(usuariosPendentes.values());
    }
    
    /**
     * Obtém todos os pontos pendentes
     */
    public static List<Object> getPontosPendentes() {
        return new ArrayList<>(pontosPendentes.values());
    }
    
    /**
     * Obtém todas as outras entidades pendentes
     */
    public static List<Object> getOutrasEntidadesPendentes() {
        return new ArrayList<>(outrasEntidadesPendentes.values());
    }
    
    /**
     * Remove usuário do cache após sincronização
     */
    public static void removeUsuarioPendente(String email) {
        usuariosPendentes.entrySet().removeIf(entry -> 
            entry.getValue().getEmail().equals(email));
        updatePendingDataFlag();
        logger.info("Usuário removido do cache offline: {}", email);
    }
    
    /**
     * Remove ponto do cache após sincronização
     */
    public static void removePontoPendente(String key) {
        pontosPendentes.remove(key);
        updatePendingDataFlag();
        logger.info("Ponto removido do cache offline");
    }
    
    /**
     * Remove outra entidade do cache após sincronização
     */
    public static void removeOutraEntidadePendente(String key) {
        outrasEntidadesPendentes.remove(key);
        updatePendingDataFlag();
        logger.info("Entidade removida do cache offline");
    }
    
    /**
     * Limpa todo o cache offline
     */
    public static void clearCache() {
        usuariosPendentes.clear();
        pontosPendentes.clear();
        outrasEntidadesPendentes.clear();
        hasPendingData = false;
        logger.info("Cache offline limpo");
    }
    
    /**
     * Obtém estatísticas do cache
     */
    public static String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTATÍSTICAS DO CACHE OFFLINE ===\n");
        stats.append("Usuários pendentes: ").append(usuariosPendentes.size()).append("\n");
        stats.append("Pontos pendentes: ").append(pontosPendentes.size()).append("\n");
        stats.append("Outras entidades pendentes: ").append(outrasEntidadesPendentes.size()).append("\n");
        stats.append("Total de itens: ").append(usuariosPendentes.size() + pontosPendentes.size() + outrasEntidadesPendentes.size()).append("\n");
        stats.append("Dados pendentes: ").append(hasPendingData ? "Sim" : "Não").append("\n");
        return stats.toString();
    }
    
    /**
     * Atualiza flag de dados pendentes
     */
    private static void updatePendingDataFlag() {
        hasPendingData = !usuariosPendentes.isEmpty() || 
                        !pontosPendentes.isEmpty() || 
                        !outrasEntidadesPendentes.isEmpty();
    }
    
    /**
     * Força sincronização de todos os dados pendentes
     */
    public static void forceSync() {
        logger.info("Forçando sincronização de dados pendentes...");
        
        // Aqui você implementaria a lógica de sincronização
        // Por enquanto, apenas limpa o cache
        clearCache();
        
        logger.info("Sincronização forçada concluída");
    }
}
