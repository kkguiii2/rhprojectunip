package com.shiftly.util;

import com.shiftly.model.TipoUsuario;
import com.shiftly.model.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário para geração e validação de tokens JWT
 */
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    // Chave secreta para assinar os tokens (em produção, deve vir de configuração externa)
    private static final String SECRET_KEY = "ShiftlySecretKeyForJWT2024!ComplexAndSecureKey123456789";
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    // Configurações de expiração
    private static final long EXPIRATION_TIME_MS = 8 * 60 * 60 * 1000; // 8 horas
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 dias
    
    // Claims personalizados
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_TYPE = "userType";
    private static final String CLAIM_USER_NAME = "userName";
    private static final String CLAIM_DEPARTMENT = "department";
    
    /**
     * Gera token JWT para um usuário
     */
    public static String generateToken(Usuario usuario) {
        logger.info("Gerando token JWT para usuário: {}", usuario.getEmail());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, usuario.getId());
        claims.put(CLAIM_USER_TYPE, usuario.getTipoUsuario().name());
        claims.put(CLAIM_USER_NAME, usuario.getNome());
        claims.put(CLAIM_DEPARTMENT, usuario.getDepartamento());
        
        return createToken(claims, usuario.getEmail(), EXPIRATION_TIME_MS);
    }
    
    /**
     * Gera refresh token para um usuário
     */
    public static String generateRefreshToken(Usuario usuario) {
        logger.info("Gerando refresh token para usuário: {}", usuario.getEmail());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, usuario.getId());
        claims.put("tokenType", "refresh");
        
        return createToken(claims, usuario.getEmail(), REFRESH_TOKEN_EXPIRATION_MS);
    }
    
    /**
     * Cria um token JWT
     */
    private static String createToken(Map<String, Object> claims, String subject, long expirationTimeMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTimeMs);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setIssuer("Shiftly")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Extrai o email (subject) do token
     */
    public static String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrai o ID do usuário do token
     */
    public static Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get(CLAIM_USER_ID);
        
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        
        return null;
    }
    
    /**
     * Extrai o tipo de usuário do token
     */
    public static TipoUsuario extractUserType(String token) {
        Claims claims = extractAllClaims(token);
        String userType = (String) claims.get(CLAIM_USER_TYPE);
        
        try {
            return TipoUsuario.valueOf(userType);
        } catch (IllegalArgumentException e) {
            logger.warn("Tipo de usuário inválido no token: {}", userType);
            return null;
        }
    }
    
    /**
     * Extrai o nome do usuário do token
     */
    public static String extractUserName(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get(CLAIM_USER_NAME);
    }
    
    /**
     * Extrai o departamento do usuário do token
     */
    public static String extractDepartment(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get(CLAIM_DEPARTMENT);
    }
    
    /**
     * Extrai a data de expiração do token
     */
    public static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrai uma claim específica do token
     */
    public static <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrai todas as claims do token
     */
    private static Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado: {}", e.getMessage());
            throw new SecurityException("Token expirado");
        } catch (UnsupportedJwtException e) {
            logger.warn("Token JWT não suportado: {}", e.getMessage());
            throw new SecurityException("Token não suportado");
        } catch (MalformedJwtException e) {
            logger.warn("Token JWT malformado: {}", e.getMessage());
            throw new SecurityException("Token malformado");
        } catch (SecurityException e) {
            logger.warn("Falha na validação da assinatura JWT: {}", e.getMessage());
            throw new SecurityException("Assinatura inválida");
        } catch (IllegalArgumentException e) {
            logger.warn("Token JWT vazio ou nulo: {}", e.getMessage());
            throw new SecurityException("Token inválido");
        }
    }
    
    /**
     * Verifica se o token está expirado
     */
    public static boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (SecurityException e) {
            return true; // Se houver erro, considera como expirado
        }
    }
    
    /**
     * Valida o token para um usuário específico
     */
    public static boolean validateToken(String token, Usuario usuario) {
        try {
            final String email = extractEmail(token);
            final Long userId = extractUserId(token);
            
            return email.equals(usuario.getEmail()) && 
                   userId.equals(usuario.getId()) && 
                   !isTokenExpired(token) &&
                   usuario.getAtivo();
                   
        } catch (SecurityException e) {
            logger.warn("Validação de token falhou para usuário {}: {}", usuario.getEmail(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida se o token é válido (sem verificar usuário específico)
     */
    public static boolean isValidToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isTokenExpired(token) && claims.get(CLAIM_USER_ID) != null;
        } catch (SecurityException e) {
            logger.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica se o usuário tem permissão de RH pelo token
     */
    public static boolean hasRhPermission(String token) {
        try {
            TipoUsuario tipoUsuario = extractUserType(token);
            return tipoUsuario == TipoUsuario.RH || tipoUsuario == TipoUsuario.ADMIN;
        } catch (SecurityException e) {
            return false;
        }
    }
    
    /**
     * Verifica se o usuário tem permissão de Admin pelo token
     */
    public static boolean hasAdminPermission(String token) {
        try {
            TipoUsuario tipoUsuario = extractUserType(token);
            return tipoUsuario == TipoUsuario.ADMIN;
        } catch (SecurityException e) {
            return false;
        }
    }
    
    /**
     * Obtém o tempo restante até a expiração do token (em minutos)
     */
    public static long getTokenRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            
            if (expiration.before(now)) {
                return 0; // Token já expirado
            }
            
            return (expiration.getTime() - now.getTime()) / (60 * 1000); // Retorna em minutos
        } catch (SecurityException e) {
            return 0;
        }
    }
    
    /**
     * Verifica se o token precisa ser renovado (se está próximo da expiração)
     */
    public static boolean needsRefresh(String token) {
        long remainingTime = getTokenRemainingTime(token);
        return remainingTime > 0 && remainingTime < 30; // Renova se faltam menos de 30 minutos
    }
    
    /**
     * Cria objeto UserInfo a partir do token
     */
    public static UserInfo extractUserInfo(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            UserInfo userInfo = new UserInfo();
            userInfo.setId(extractUserId(token));
            userInfo.setEmail(extractEmail(token));
            userInfo.setNome(extractUserName(token));
            userInfo.setTipoUsuario(extractUserType(token));
            userInfo.setDepartamento(extractDepartment(token));
            
            return userInfo;
        } catch (SecurityException e) {
            throw new SecurityException("Não foi possível extrair informações do usuário do token");
        }
    }
    
    /**
     * Classe para transportar informações do usuário extraídas do token
     */
    public static class UserInfo {
        private Long id;
        private String email;
        private String nome;
        private TipoUsuario tipoUsuario;
        private String departamento;
        
        // Getters e Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getNome() {
            return nome;
        }
        
        public void setNome(String nome) {
            this.nome = nome;
        }
        
        public TipoUsuario getTipoUsuario() {
            return tipoUsuario;
        }
        
        public void setTipoUsuario(TipoUsuario tipoUsuario) {
            this.tipoUsuario = tipoUsuario;
        }
        
        public String getDepartamento() {
            return departamento;
        }
        
        public void setDepartamento(String departamento) {
            this.departamento = departamento;
        }
        
        public boolean isRh() {
            return tipoUsuario == TipoUsuario.RH || tipoUsuario == TipoUsuario.ADMIN;
        }
        
        public boolean isAdmin() {
            return tipoUsuario == TipoUsuario.ADMIN;
        }
        
        @Override
        public String toString() {
            return "UserInfo{" +
                   "id=" + id +
                   ", email='" + email + '\'' +
                   ", nome='" + nome + '\'' +
                   ", tipoUsuario=" + tipoUsuario +
                   ", departamento='" + departamento + '\'' +
                   '}';
        }
    }
}
