package com.iamportfolio.auth.service;

import com.iamportfolio.auth.dto.*;
import com.iamportfolio.identity.model.LifecycleState;
import com.iamportfolio.identity.model.User;
import com.iamportfolio.identity.repository.UserRepository;
import com.iamportfolio.auth.jwt.JwtUtil;
import com.iamportfolio.auth.twofactor.TwoFactorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.identity.require-approval:false}")
    private boolean requireApproval;

    /**
     * Registrar novo usuÃ¡rio
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Tentativa de registro para usuÃ¡rio: {}", request.getUsername());

        // Validar se as senhas conferem
        if (!request.isPasswordMatching()) {
            return AuthResponse.error("As senhas nÃ£o conferem");
        }

        // Verificar se o usuÃ¡rio jÃ¡ existe
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.error("Nome de usuÃ¡rio jÃ¡ existe");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("Email jÃ¡ estÃ¡ em uso");
        }

        try {
            // Criar novo usuÃ¡rio
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setLifecycleState(requireApproval ? LifecycleState.PENDING_APPROVAL : LifecycleState.ACTIVE);

            userRepository.save(user);

            logger.info("UsuÃ¡rio registrado com sucesso: {}", user.getUsername());

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
            logger.error("Erro ao registrar usuÃ¡rio: {}", e.getMessage(), e);
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
            // Buscar usuÃ¡rio
            Optional<User> optionalUser = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail());
            
            if (optionalUser.isEmpty()) {
                throw new UsernameNotFoundException("UsuÃ¡rio nÃ£o encontrado");
            }

            User user = optionalUser.get();

            // Verificar senha
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Credenciais invÃ¡lidas");
            }

            // Verificar se a conta esta ativa (legacy + lifecycle state)
            if (!user.isEnabled()) {
                return AuthResponse.error("Conta desabilitada");
            }
            if (user.getLifecycleState() != LifecycleState.ACTIVE) {
                logger.warn("Login bloqueado para {} - estado de ciclo de vida: {}",
                        user.getUsername(), user.getLifecycleState());
                return AuthResponse.error("Conta nao esta ativa (" + user.getLifecycleState() + ")");
            }

            // Se 2FA estÃ¡ habilitado
            if (user.isTwoFactorEnabled()) {
                return handleTwoFactorAuth(user, request.getTwoFactorCode());
            }

            // Login sem 2FA - atualizar Ãºltimo login e gerar token
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
            logger.warn("Falha na autenticaÃ§Ã£o para: {} - {}", request.getUsernameOrEmail(), e.getMessage());
            return AuthResponse.error("Credenciais invÃ¡lidas");
        } catch (Exception e) {
            logger.error("Erro no login: {}", e.getMessage(), e);
            return AuthResponse.error("Erro interno do servidor");
        }
    }

    /**
     * Configurar 2FA para um usuÃ¡rio
     */
    @Transactional
    public TwoFactorSetupResponse setupTwoFactor(String username) {
        logger.info("Configurando 2FA para usuÃ¡rio: {}", username);

        try {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (optionalUser.isEmpty()) {
                return TwoFactorSetupResponse.error("UsuÃ¡rio nÃ£o encontrado");
            }

            User user = optionalUser.get();

            // Gerar secret
            String secret = twoFactorService.generateSecret();
            
            // Gerar QR code
            String qrCodeImage = twoFactorService.generateQrCodeImageAsBase64(secret, user.getUsername());
            String qrCodeUrl = twoFactorService.generateQrCodeUrl(secret, user.getUsername());
            
            // Gerar cÃ³digos de backup
            List<String> backupCodes = twoFactorService.generateBackupCodes();

            // Salvar no usuÃ¡rio (ainda nÃ£o habilitado)
            user.setTwoFactorSecret(secret);
            user.setBackupCodes(twoFactorService.backupCodesToString(backupCodes));
            userRepository.save(user);

            logger.info("2FA configurado para usuÃ¡rio: {}", username);

            return TwoFactorSetupResponse.success(secret, qrCodeImage, qrCodeUrl, backupCodes);

        } catch (Exception e) {
            logger.error("Erro ao configurar 2FA: {}", e.getMessage(), e);
            return TwoFactorSetupResponse.error("Erro interno do servidor");
        }
    }

    /**
     * Confirmar configuraÃ§Ã£o do 2FA
     */
    @Transactional
    public AuthResponse confirmTwoFactor(String username, String code) {
        logger.info("Confirmando 2FA para usuÃ¡rio: {}", username);

        try {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (optionalUser.isEmpty()) {
                return AuthResponse.error("UsuÃ¡rio nÃ£o encontrado");
            }

            User user = optionalUser.get();

            if (user.getTwoFactorSecret() == null) {
                return AuthResponse.error("2FA nÃ£o foi configurado");
            }

            // Verificar cÃ³digo
            if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
                return AuthResponse.error("CÃ³digo 2FA invÃ¡lido");
            }

            // Habilitar 2FA
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            logger.info("2FA confirmado e habilitado para usuÃ¡rio: {}", username);

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
        logger.info("Desabilitando 2FA para usuÃ¡rio: {}", username);

        try {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            
            if (optionalUser.isEmpty()) {
                return AuthResponse.error("UsuÃ¡rio nÃ£o encontrado");
            }

            User user = optionalUser.get();

            if (!user.isTwoFactorEnabled()) {
                return AuthResponse.error("2FA nÃ£o estÃ¡ habilitado");
            }

            // Verificar cÃ³digo antes de desabilitar
            if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
                return AuthResponse.error("CÃ³digo 2FA invÃ¡lido");
            }

            // Desabilitar 2FA
            user.setTwoFactorEnabled(false);
            user.setTwoFactorSecret(null);
            user.setBackupCodes(null);
            userRepository.save(user);

            logger.info("2FA desabilitado para usuÃ¡rio: {}", username);

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
     * Lidar com autenticaÃ§Ã£o de duas etapas
     */
    private AuthResponse handleTwoFactorAuth(User user, String twoFactorCode) {
        if (twoFactorCode == null || twoFactorCode.trim().isEmpty()) {
            return AuthResponse.requiresTwoFactor("CÃ³digo de duas etapas necessÃ¡rio");
        }

        // Verificar cÃ³digo TOTP
        if (twoFactorService.verifyCode(user.getTwoFactorSecret(), twoFactorCode)) {
            // CÃ³digo vÃ¡lido - completar login
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

        // Verificar cÃ³digos de backup
        List<String> backupCodes = twoFactorService.stringToBackupCodes(user.getBackupCodes());
        if (twoFactorService.verifyBackupCode(twoFactorCode, backupCodes)) {
            // CÃ³digo de backup vÃ¡lido - remover da lista e completar login
            List<String> updatedCodes = twoFactorService.removeUsedBackupCode(twoFactorCode, backupCodes);
            user.setBackupCodes(twoFactorService.backupCodesToString(updatedCodes));
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(new java.util.HashMap<>(), user.getUsername());
            
            logger.info("Login com cÃ³digo de backup realizado com sucesso para: {}", user.getUsername());
            
            return AuthResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.isTwoFactorEnabled()
            );
        }

        return AuthResponse.error("CÃ³digo de duas etapas invÃ¡lido");
    }

    /**
     * Obter informaÃ§Ãµes do usuÃ¡rio
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Verificar se usuÃ¡rio existe
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
} 