package com.iamportfolio.repository;

import com.iamportfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuÃ¡rio por nome de usuÃ¡rio
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca usuÃ¡rio por email
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuÃ¡rio por nome de usuÃ¡rio ou email
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Verifica se existe usuÃ¡rio com o nome de usuÃ¡rio
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se existe usuÃ¡rio com o email
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuÃ¡rios ativos
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    java.util.List<User> findActiveUsers();

    /**
     * Busca usuÃ¡rios com 2FA habilitado
     */
    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = true")
    java.util.List<User> findUsersWithTwoFactorEnabled();

    /**
     * Conta usuÃ¡rios ativos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();
} 