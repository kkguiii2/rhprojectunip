package com.shiftly.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitário para carregar configurações da aplicação
 */
public class ConfigUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static Properties properties;
    
    static {
        loadProperties();
    }
    
    /**
     * Carrega as propriedades do arquivo de configuração
     */
    private static void loadProperties() {
        properties = new Properties();
        
        // Força o uso do arquivo correto do projeto Shiftly
        try (InputStream inputStream = ConfigUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Configurações carregadas com sucesso");
                
                // Log das configurações carregadas para debug
                logger.info("=== CONFIGURAÇÕES CARREGADAS ===");
                properties.forEach((key, value) -> {
                    if (key.toString().toLowerCase().contains("password")) {
                        logger.info("{} = [HIDDEN]", key);
                    } else {
                        logger.info("{} = {}", key, value);
                    }
                });
                logger.info("=== FIM DAS CONFIGURAÇÕES ===");
                
            } else {
                logger.warn("Arquivo application.properties não encontrado, usando valores padrão");
            }
            
        } catch (IOException e) {
            logger.error("Erro ao carregar configurações: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtém uma propriedade como string
     */
    public static String getString(String key) {
        String value = properties.getProperty(key);
        logger.debug("ConfigUtil.getString({}) = {}", key, value);
        return value;
    }
    
    /**
     * Obtém uma propriedade como string com valor padrão
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Obtém uma propriedade como inteiro
     */
    public static int getInt(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Erro ao converter propriedade {} para inteiro: {}", key, e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Obtém uma propriedade como long
     */
    public static long getLong(String key, long defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Erro ao converter propriedade {} para long: {}", key, e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Obtém uma propriedade como double
     */
    public static double getDouble(String key, double defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Erro ao converter propriedade {} para double: {}", key, e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Obtém uma propriedade como boolean
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Verifica se uma propriedade existe
     */
    public static boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Lista todas as propriedades (para debug)
     */
    public static void printAllProperties() {
        logger.debug("=== Configurações da Aplicação ===");
        properties.forEach((key, value) -> {
            // Não exibe senhas nos logs
            if (key.toString().toLowerCase().contains("password")) {
                logger.debug("{} = [HIDDEN]", key);
            } else {
                logger.debug("{} = {}", key, value);
            }
        });
        logger.debug("=== Fim das Configurações ===");
    }
}
