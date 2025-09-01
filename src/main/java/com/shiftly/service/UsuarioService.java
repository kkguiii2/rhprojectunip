package com.shiftly.service;

import com.shiftly.model.TipoUsuario;
import com.shiftly.model.Usuario;
import com.shiftly.repository.UsuarioRepository;
import com.shiftly.util.OfflineCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar usuários
 */
public class UsuarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Autentica um usuário
     */
    public Optional<Usuario> autenticar(String email, String senha) {
        logger.info("Tentativa de autenticação para email: {}", email);
        
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                if (!usuario.getAtivo()) {
                    logger.warn("Tentativa de login de usuário inativo: {}", email);
                    return Optional.empty();
                }
                
                if (passwordEncoder.matches(senha, usuario.getSenha())) {
                    logger.info("Autenticação bem-sucedida para: {}", email);
                    return Optional.of(usuario);
                } else {
                    logger.warn("Senha incorreta para: {}", email);
                }
            } else {
                logger.warn("Usuário não encontrado: {}", email);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao autenticar usuário {}: {}", email, e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Cria um novo usuário
     */
    public Usuario criarUsuario(Usuario usuario) {
        logger.info("Criando novo usuário: {}", usuario.getEmail());
        
        // Validações
        validarUsuario(usuario);
        
        try {
            // Verifica se email já existe
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new IllegalArgumentException("Email já está em uso: " + usuario.getEmail());
            }
            
            // Verifica se CPF já existe
            if (usuarioRepository.existsByCpf(usuario.getCpf())) {
                throw new IllegalArgumentException("CPF já está em uso: " + usuario.getCpf());
            }
            
            // Criptografa a senha
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            
            // Define dados padrão
            usuario.setAtivo(true);
            usuario.setDataCriacao(LocalDateTime.now());
            usuario.setDataAtualizacao(LocalDateTime.now());
            
            Usuario usuarioSalvo = usuarioRepository.save(usuario);
            logger.info("Usuário criado com sucesso no banco: ID {}", usuarioSalvo.getId());
            
            return usuarioSalvo;
            
        } catch (Exception e) {
            logger.warn("Erro ao criar usuário no banco, adicionando ao cache offline: {}", e.getMessage());
            
            // Adiciona ao cache offline
            OfflineCache.addUsuarioPendente(usuario);
            
            // Retorna o usuário com ID temporário
            usuario.setId(-1L); // ID negativo indica que está no cache
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            usuario.setAtivo(true);
            usuario.setDataCriacao(LocalDateTime.now());
            usuario.setDataAtualizacao(LocalDateTime.now());
            
            logger.info("Usuário adicionado ao cache offline: {}", usuario.getEmail());
            return usuario;
        }
    }
    
    /**
     * Atualiza um usuário existente
     */
    public Usuario atualizarUsuario(Usuario usuario) {
        logger.info("Atualizando usuário: ID {}", usuario.getId());
        
        // Verifica se o usuário existe
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new IllegalArgumentException("Usuário não encontrado: " + usuario.getId());
        }
        
        // Validações
        validarUsuario(usuario);
        
        // Verifica se email já existe (exceto para o próprio usuário)
        Optional<Usuario> usuarioComEmail = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Email já está em uso: " + usuario.getEmail());
        }
        
        // Verifica se CPF já existe (exceto para o próprio usuário)
        Optional<Usuario> usuarioComCpf = usuarioRepository.findByCpf(usuario.getCpf());
        if (usuarioComCpf.isPresent() && !usuarioComCpf.get().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("CPF já está em uso: " + usuario.getCpf());
        }
        
        usuario.setDataAtualizacao(LocalDateTime.now());
        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        
        logger.info("Usuário atualizado com sucesso: ID {}", usuarioAtualizado.getId());
        return usuarioAtualizado;
    }
    
    /**
     * Busca usuário por ID
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Busca usuário por email
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    /**
     * Busca usuário por CPF
     */
    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }
    
    /**
     * Lista todos os usuários ativos
     */
    public List<Usuario> listarUsuariosAtivos() {
        return usuarioRepository.findAllAtivos();
    }
    
    /**
     * Lista usuários por tipo
     */
    public List<Usuario> listarPorTipo(TipoUsuario tipo) {
        return usuarioRepository.findByTipoUsuario(tipo);
    }
    
    /**
     * Lista usuários por departamento
     */
    public List<Usuario> listarPorDepartamento(String departamento) {
        return usuarioRepository.findByDepartamento(departamento);
    }
    
    /**
     * Desativa um usuário
     */
    public boolean desativarUsuario(Long id) {
        logger.info("Desativando usuário: ID {}", id);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setAtivo(false);
            usuario.setDataAtualizacao(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            logger.info("Usuário desativado com sucesso: ID {}", id);
            return true;
        }
        
        logger.warn("Usuário não encontrado para desativação: ID {}", id);
        return false;
    }
    
    /**
     * Reativa um usuário
     */
    public boolean reativarUsuario(Long id) {
        logger.info("Reativando usuário: ID {}", id);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setAtivo(true);
            usuario.setDataAtualizacao(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            logger.info("Usuário reativado com sucesso: ID {}", id);
            return true;
        }
        
        logger.warn("Usuário não encontrado para reativação: ID {}", id);
        return false;
    }
    
    /**
     * Altera a senha de um usuário
     */
    public boolean alterarSenha(Long id, String senhaAtual, String novaSenha) {
        logger.info("Alterando senha do usuário: ID {}", id);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Verifica senha atual
            if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
                logger.warn("Senha atual incorreta para usuário: ID {}", id);
                return false;
            }
            
            // Valida nova senha
            if (novaSenha == null || novaSenha.length() < 6) {
                throw new IllegalArgumentException("Nova senha deve ter pelo menos 6 caracteres");
            }
            
            // Atualiza senha
            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuario.setDataAtualizacao(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            logger.info("Senha alterada com sucesso para usuário: ID {}", id);
            return true;
        }
        
        logger.warn("Usuário não encontrado para alteração de senha: ID {}", id);
        return false;
    }
    
    /**
     * Redefine senha de um usuário (apenas RH/Admin)
     */
    public String redefinirSenha(Long id, Long usuarioRhId) {
        logger.info("Redefinindo senha do usuário: ID {} por usuário RH: ID {}", id, usuarioRhId);
        
        // Verifica se o usuário RH tem permissão
        Optional<Usuario> usuarioRhOpt = usuarioRepository.findById(usuarioRhId);
        if (usuarioRhOpt.isEmpty() || !usuarioRhOpt.get().isRH()) {
            throw new SecurityException("Usuário não tem permissão para redefinir senhas");
        }
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Gera nova senha temporária
            String novaSenha = gerarSenhaTemporaria();
            
            // Atualiza senha
            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuario.setDataAtualizacao(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            logger.info("Senha redefinida com sucesso para usuário: ID {}", id);
            return novaSenha;
        }
        
        throw new IllegalArgumentException("Usuário não encontrado: " + id);
    }
    
    /**
     * Atualiza dados de reconhecimento facial
     */
    public boolean atualizarFaceEncoding(Long id, String faceEncoding) {
        logger.info("Atualizando face encoding do usuário: ID {}", id);
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setFaceEncoding(faceEncoding);
            usuario.setDataAtualizacao(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            logger.info("Face encoding atualizado com sucesso para usuário: ID {}", id);
            return true;
        }
        
        logger.warn("Usuário não encontrado para atualizar face encoding: ID {}", id);
        return false;
    }
    
    /**
     * Valida dados do usuário
     */
    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        
        if (usuario.getCpf() == null || usuario.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        
        if (usuario.getCpf().length() != 11) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos");
        }
        
        if (usuario.getTipoUsuario() == null) {
            throw new IllegalArgumentException("Tipo de usuário é obrigatório");
        }
        
        // Valida email
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email inválido");
        }
        
        // Valida CPF (formato básico)
        if (!usuario.getCpf().matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF deve conter apenas números");
        }
    }
    
    /**
     * Gera uma senha temporária
     */
    private String gerarSenhaTemporaria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder senha = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * caracteres.length());
            senha.append(caracteres.charAt(index));
        }
        
        return senha.toString();
    }
    
    /**
     * Conta total de usuários ativos
     */
    public long contarUsuariosAtivos() {
        return usuarioRepository.count();
    }
    
    /**
     * Conta usuários por tipo
     */
    public long contarUsuariosPorTipo(TipoUsuario tipo) {
        return usuarioRepository.findByTipoUsuario(tipo).size();
    }
}
