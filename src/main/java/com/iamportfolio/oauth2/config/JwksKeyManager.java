package com.iamportfolio.oauth2.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Provides the RSA 2048 signing key for OIDC ID tokens / JWT access tokens.
 * <p>
 * Generated in-memory at boot — the JWK rotates on every restart, which is
 * fine for development and acceptable for a portfolio: real deployments
 * should bind-mount a JKS or use a managed KMS so clients can cache the JWKS
 * across restarts and so token signatures survive rolling updates.
 */
@Configuration
public class JwksKeyManager {

    private static final Logger logger = LoggerFactory.getLogger(JwksKeyManager.class);

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();
        RSAKey rsa = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())
                .build();
        logger.warn("Generated ephemeral RSA 2048 JWK (kid={}); rotates on restart. " +
                "Bind-mount a keystore in production.", rsa.getKeyID());
        return new ImmutableJWKSet<>(new JWKSet(rsa));
    }
}
