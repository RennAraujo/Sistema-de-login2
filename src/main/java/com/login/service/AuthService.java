package com.login.service;

import com.login.dto.*;
import com.login.model.User;
import com.login.repository.UserRepository;
import com.login.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TwoFactorService twoFactorService;

    /**
     * Registrar novo usuário
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Tentativa de registro para usuário: {}", request.getUsername());

        // Validar se as senhas conferem
        if (!request.isPasswordMatching()) {
            return AuthResponse.error("As senhas não conferem");
        }

        // Verificar se o usuário já existe
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.error("Nome de usuário já existe");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("Email já está em uso");
        }

        try {
            // Criar novo usuário
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            userRepository.save(user);

            logger.info("Usuário registrado com sucesso: {}", user.getUsername());

            // Gerar token JWT
            String token = jwtUtil.generateToken(new java.util.HashMap<>(), user.getUsername());
            
            return AuthResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.isTwoFactorEnabled()
            );

        } catch (Exception e) {
            logger.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            return AuthResponse.error("Erro interno do servidor");
        }
    }

    /**
     * Fazer login
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        logger.info("Tentativa de login para: {}", request.getUsernameOrEmail());

        try {
            // Buscar usuário
            Optional<User> optionalUser = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail());
            
            if (optionalUser.isEmpty()) {
                throw new UsernameNotFoundException("Usuário não encontrado");
            }

            User user = optionalUser.get();

            // Verificar senha
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Credenciais inválidas");
            }

            // Verificar se a conta está ativa
            if (!user.isEnabled()) {
                return AuthResponse.error("Conta desabilitada");
            }

            // Se 2FA está habilitado
            if (user.isTwoFactorEnabled()) {
                return handleTwoFactorAuth(user, request.getTwoFactorCode());
            }

            // Login sem 2FA - atualizar último login e gerar token
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(new java.util.HashMap<>(), user.getUsername());
            
            logger.info("Login realizado com sucesso para: {}", user.getUsername());
            
            return AuthResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.isTwoFactorEnabled()
            );

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            logger.warn("Falha na autenticação para: {} - {}", request.getUsernameOrEmail(), e.getMessage());
            return AuthResponse.error("Credenciais inválidas");
        } catch (Exception e) {
            logger.error("Erro no login: {}", e.getMessage(), e);
            return AuthResponse.error("Erro interno do servidor");
        }
    }

    /**
     * Configurar 2FA para um usuário
     */
    @Transactional
    public TwoFactorSetupResponse setupTwoFactor(String username) {
        logger.info("Configurando 2FA para usuário: {}", username);

        try {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (optionalUser.isEmpty()) {
                return TwoFactorSetupResponse.error("Usuário não encontrado");
            }

            User user = optionalUser.get();

            // Gerar secret
            String secret = twoFactorService.generateSecret();
            
            // Gerar QR code
            String qrCodeImage = twoFactorService.generateQrCodeImageAsBase64(secret, user.getUsername());
            String qrCodeUrl = twoFactorService.generateQrCodeUrl(secret, user.getUsername());
            
            // Gerar códigos de backup
            List<String> backupCodes = twoFactorService.generateBackupCodes();

            // Salvar no usuário (ainda não habilitado)
            user.setTwoFactorSecret(secret);
            user.setBackupCodes(twoFactorService.backupCodesToString(backupCodes));
            userRepository.save(user);

            logger.info("2FA configurado para usuário: {}", username);

            return TwoFactorSetupResponse.success(secret, qrCodeImage, qrCodeUrl, backupCodes);

        } catch (Exception e) {
            logger.error("Erro ao configurar 2FA: {}", e.getMessage(), e);
            return TwoFactorSetupResponse.error("Erro interno do servidor");
        }
    }

    /**
     * Confirmar configuração do 2FA
     */
    @Transactional
    public AuthResponse confirmTwoFactor(String username, String code) {
        logger.info("Confirmando 2FA para usuário: {}", username);

        try {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (optionalUser.isEmpty()) {
                return AuthResponse.error("Usuário não encontrado");
            }

            User user = optionalUser.get();

            if (user.getTwoFactorSecret() == null) {
                return AuthResponse.error("2FA não foi configurado");
            }

            // Verificar código
            if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
                return AuthResponse.error("Código 2FA inválido");
            }

            // Habilitar 2FA
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            logger.info("2FA confirmado e habilitado para usuário: {}", username);

            return AuthResponse.success(
                null,
                null,
                user.getUsername(),
                user.getEmail(),
                true
            );

        } catch (Exception e) {
            logger.error("Erro ao confirmar 2FA: {}", e.getMessage(), e);
            return AuthResponse.error("Erro interno do servidor");
        }
    }

    /**
     * Desabilitar 2FA
     */
    @Transactional
    public AuthResponse disableTwoFactor(String username, String code) {
        logger.info("Desabilitando 2FA para usuário: {}", username);

        try {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (optionalUser.isEmpty()) {
                return AuthResponse.error("Usuário não encontrado");
            }

            User user = optionalUser.get();

            if (!user.isTwoFactorEnabled()) {
                return AuthResponse.error("2FA não está habilitado");
            }

            // Verificar código antes de desabilitar
            if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
                return AuthResponse.error("Código 2FA inválido");
            }

            // Desabilitar 2FA
            user.setTwoFactorEnabled(false);
            user.setTwoFactorSecret(null);
            user.setBackupCodes(null);
            userRepository.save(user);

            logger.info("2FA desabilitado para usuário: {}", username);

            return AuthResponse.success(
                null,
                null,
                user.getUsername(),
                user.getEmail(),
                false
            );

        } catch (Exception e) {
            logger.error("Erro ao desabilitar 2FA: {}", e.getMessage(), e);
            return AuthResponse.error("Erro interno do servidor");
        }
    }

    /**
     * Lidar com autenticação de duas etapas
     */
    private AuthResponse handleTwoFactorAuth(User user, String twoFactorCode) {
        if (twoFactorCode == null || twoFactorCode.trim().isEmpty()) {
            return AuthResponse.requiresTwoFactor("Código de duas etapas necessário");
        }

        // Verificar código TOTP
        if (twoFactorService.verifyCode(user.getTwoFactorSecret(), twoFactorCode)) {
            // Código válido - completar login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(new java.util.HashMap<>(), user.getUsername());
            
            logger.info("Login com 2FA realizado com sucesso para: {}", user.getUsername());
            
            return AuthResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.isTwoFactorEnabled()
            );
        }

        // Verificar códigos de backup
        List<String> backupCodes = twoFactorService.stringToBackupCodes(user.getBackupCodes());
        if (twoFactorService.verifyBackupCode(twoFactorCode, backupCodes)) {
            // Código de backup válido - remover da lista e completar login
            List<String> updatedCodes = twoFactorService.removeUsedBackupCode(twoFactorCode, backupCodes);
            user.setBackupCodes(twoFactorService.backupCodesToString(updatedCodes));
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(new java.util.HashMap<>(), user.getUsername());
            
            logger.info("Login com código de backup realizado com sucesso para: {}", user.getUsername());
            
            return AuthResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.isTwoFactorEnabled()
            );
        }

        return AuthResponse.error("Código de duas etapas inválido");
    }

    /**
     * Obter informações do usuário
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Verificar se usuário existe
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
} 