package com.shiftly.service;

import com.shiftly.model.Usuario;
import com.shiftly.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service para gerenciar autenticação e autorização
 */
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UsuarioService usuarioService;
    
    // Cache de sessões ativas (em produção, usar Redis ou similar)
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    // Configurações de segurança
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;
    
    // Tracking de tentativas de login
    private final Map<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();
    
    public AuthService() {
        this.usuarioService = new UsuarioService();
    }
    
    /**
     * Realiza o login do usuário
     */
    public AuthResult login(String email, String senha) {
        logger.info("Tentativa de login para: {}", email);
        
        // Verifica se a conta está bloqueada
        if (isAccountLocked(email)) {
            logger.warn("Conta bloqueada por excesso de tentativas: {}", email);
            return AuthResult.failure("Conta temporariamente bloqueada. Tente novamente em " + 
                                    LOCKOUT_DURATION_MINUTES + " minutos.");
        }
        
        // Tenta autenticar
        Optional<Usuario> usuarioOpt = usuarioService.autenticar(email, senha);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Limpa tentativas anteriores
            clearLoginAttempts(email);
            
            // Gera tokens
            String accessToken = JwtUtil.generateToken(usuario);
            String refreshToken = JwtUtil.generateRefreshToken(usuario);
            
            // Cria sessão
            SessionInfo session = new SessionInfo(usuario.getId(), accessToken, refreshToken);
            activeSessions.put(accessToken, session);
            
            logger.info("Login bem-sucedido para: {}", email);
            return AuthResult.success(accessToken, refreshToken, usuario);
            
        } else {
            // Registra tentativa de login falhada
            recordFailedLoginAttempt(email);
            
            logger.warn("Login falhado para: {}", email);
            return AuthResult.failure("Email ou senha incorretos");
        }
    }
    
    /**
     * Realiza o logout do usuário
     */
    public boolean logout(String accessToken) {
        logger.info("Realizando logout");
        
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }
        
        // Remove sessão ativa
        SessionInfo session = activeSessions.remove(accessToken);
        
        if (session != null) {
            logger.info("Logout realizado para usuário ID: {}", session.getUserId());
            return true;
        }
        
        return false;
    }
    
    /**
     * Renova o token de acesso usando refresh token
     */
    public AuthResult refreshToken(String refreshToken) {
        logger.info("Renovando token de acesso");
        
        try {
            // Valida refresh token
            if (!JwtUtil.isValidToken(refreshToken)) {
                logger.warn("Refresh token inválido");
                return AuthResult.failure("Refresh token inválido");
            }
            
            // Extrai informações do refresh token
            Long userId = JwtUtil.extractUserId(refreshToken);
            String email = JwtUtil.extractEmail(refreshToken);
            
            // Busca usuário
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            if (usuarioOpt.isEmpty() || !usuarioOpt.get().getAtivo()) {
                logger.warn("Usuário não encontrado ou inativo para refresh: {}", userId);
                return AuthResult.failure("Usuário inválido");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verifica se o email ainda é o mesmo
            if (!usuario.getEmail().equals(email)) {
                logger.warn("Email do usuário foi alterado, refresh token inválido: {}", userId);
                return AuthResult.failure("Token inválido");
            }
            
            // Gera novos tokens
            String newAccessToken = JwtUtil.generateToken(usuario);
            String newRefreshToken = JwtUtil.generateRefreshToken(usuario);
            
            // Atualiza sessão
            SessionInfo session = new SessionInfo(usuario.getId(), newAccessToken, newRefreshToken);
            activeSessions.put(newAccessToken, session);
            
            logger.info("Token renovado com sucesso para usuário: {}", email);
            return AuthResult.success(newAccessToken, newRefreshToken, usuario);
            
        } catch (SecurityException e) {
            logger.warn("Erro ao renovar token: {}", e.getMessage());
            return AuthResult.failure("Erro ao renovar token");
        }
    }
    
    /**
     * Valida se o token é válido e o usuário tem acesso
     */
    public boolean validateToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Verifica se o token é válido
            if (!JwtUtil.isValidToken(accessToken)) {
                return false;
            }
            
            // Verifica se existe sessão ativa
            SessionInfo session = activeSessions.get(accessToken);
            if (session == null) {
                return false;
            }
            
            // Verifica se o usuário ainda está ativo
            Long userId = JwtUtil.extractUserId(accessToken);
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            
            return usuarioOpt.isPresent() && usuarioOpt.get().getAtivo();
            
        } catch (SecurityException e) {
            logger.warn("Erro na validação do token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtém informações do usuário a partir do token
     */
    public Optional<JwtUtil.UserInfo> getUserInfo(String accessToken) {
        try {
            if (validateToken(accessToken)) {
                return Optional.of(JwtUtil.extractUserInfo(accessToken));
            }
        } catch (SecurityException e) {
            logger.warn("Erro ao extrair informações do usuário: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Verifica se o usuário tem permissão de RH
     */
    public boolean hasRhPermission(String accessToken) {
        return validateToken(accessToken) && JwtUtil.hasRhPermission(accessToken);
    }
    
    /**
     * Verifica se o usuário tem permissão de Admin
     */
    public boolean hasAdminPermission(String accessToken) {
        return validateToken(accessToken) && JwtUtil.hasAdminPermission(accessToken);
    }
    
    /**
     * Verifica se o token precisa ser renovado
     */
    public boolean tokenNeedsRefresh(String accessToken) {
        return validateToken(accessToken) && JwtUtil.needsRefresh(accessToken);
    }
    
    /**
     * Invalida todas as sessões de um usuário
     */
    public void invalidateAllUserSessions(Long userId) {
        logger.info("Invalidando todas as sessões do usuário ID: {}", userId);
        
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getUserId().equals(userId)
        );
    }
    
    /**
     * Lista sessões ativas (para administradores)
     */
    public Map<String, Object> getActiveSessionsInfo() {
        Map<String, Object> info = new HashMap<>();
        
        int totalSessions = activeSessions.size();
        Map<String, Integer> sessionsByUserType = new HashMap<>();
        
        for (SessionInfo session : activeSessions.values()) {
            try {
                JwtUtil.UserInfo userInfo = JwtUtil.extractUserInfo(session.getAccessToken());
                String userType = userInfo.getTipoUsuario().name();
                sessionsByUserType.merge(userType, 1, Integer::sum);
            } catch (Exception e) {
                // Ignora sessões com tokens inválidos
            }
        }
        
        info.put("totalActiveSessions", totalSessions);
        info.put("sessionsByUserType", sessionsByUserType);
        
        return info;
    }
    
    /**
     * Limpa sessões expiradas
     */
    public void cleanupExpiredSessions() {
        logger.debug("Limpando sessões expiradas");
        
        activeSessions.entrySet().removeIf(entry -> {
            try {
                return JwtUtil.isTokenExpired(entry.getKey());
            } catch (SecurityException e) {
                return true; // Remove tokens inválidos
            }
        });
        
        // Limpa tentativas de login antigas
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        loginAttempts.entrySet().removeIf(entry -> 
            entry.getValue().getLastAttempt().isBefore(cutoff)
        );
    }
    
    /**
     * Verifica se a conta está bloqueada por excesso de tentativas
     */
    private boolean isAccountLocked(String email) {
        LoginAttemptInfo attemptInfo = loginAttempts.get(email.toLowerCase());
        
        if (attemptInfo == null) {
            return false;
        }
        
        // Verifica se ainda está no período de bloqueio
        if (attemptInfo.getAttempts() >= MAX_LOGIN_ATTEMPTS) {
            LocalDateTime lockoutEnd = attemptInfo.getLastAttempt().plusMinutes(LOCKOUT_DURATION_MINUTES);
            return LocalDateTime.now().isBefore(lockoutEnd);
        }
        
        return false;
    }
    
    /**
     * Registra tentativa de login falhada
     */
    private void recordFailedLoginAttempt(String email) {
        String emailKey = email.toLowerCase();
        LoginAttemptInfo attemptInfo = loginAttempts.get(emailKey);
        
        if (attemptInfo == null) {
            attemptInfo = new LoginAttemptInfo();
            loginAttempts.put(emailKey, attemptInfo);
        }
        
        attemptInfo.incrementAttempts();
        attemptInfo.setLastAttempt(LocalDateTime.now());
    }
    
    /**
     * Limpa tentativas de login para um email
     */
    private void clearLoginAttempts(String email) {
        loginAttempts.remove(email.toLowerCase());
    }
    
    /**
     * Classe para armazenar informações de sessão
     */
    private static class SessionInfo {
        private final Long userId;
        private final String accessToken;
        private final String refreshToken;
        private final LocalDateTime createdAt;
        
        public SessionInfo(Long userId, String accessToken, String refreshToken) {
            this.userId = userId;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.createdAt = LocalDateTime.now();
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
    
    /**
     * Classe para tracking de tentativas de login
     */
    private static class LoginAttemptInfo {
        private int attempts = 0;
        private LocalDateTime lastAttempt;
        
        public void incrementAttempts() {
            this.attempts++;
        }
        
        public int getAttempts() {
            return attempts;
        }
        
        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }
        
        public void setLastAttempt(LocalDateTime lastAttempt) {
            this.lastAttempt = lastAttempt;
        }
    }
    
    /**
     * Resultado de operações de autenticação
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String accessToken;
        private final String refreshToken;
        private final Usuario usuario;
        
        private AuthResult(boolean success, String message, String accessToken, String refreshToken, Usuario usuario) {
            this.success = success;
            this.message = message;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.usuario = usuario;
        }
        
        public static AuthResult success(String accessToken, String refreshToken, Usuario usuario) {
            return new AuthResult(true, "Autenticação realizada com sucesso", accessToken, refreshToken, usuario);
        }
        
        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null, null, null);
        }
        
        // Getters
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public Usuario getUsuario() {
            return usuario;
        }
    }
}
