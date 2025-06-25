package com.login.repository;

import com.login.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário por nome de usuário
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca usuário por email
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuário por nome de usuário ou email
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Verifica se existe usuário com o nome de usuário
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se existe usuário com o email
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuários ativos
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    java.util.List<User> findActiveUsers();

    /**
     * Busca usuários com 2FA habilitado
     */
    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = true")
    java.util.List<User> findUsersWithTwoFactorEnabled();

    /**
     * Conta usuários ativos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();
} 