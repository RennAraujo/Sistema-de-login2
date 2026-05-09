package com.iamportfolio.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Gerar token JWT
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(claims, userDetails.getUsername());
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Extrair username do token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrair data de expiraÃ§Ã£o
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrair claim especÃ­fico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrair todos os claims
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.error("Erro ao extrair claims do token: {}", e.getMessage());
            throw e;
        }
    }

    // Verificar se o token expirou
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            logger.error("Erro ao verificar expiraÃ§Ã£o do token: {}", e.getMessage());
            return true;
        }
    }

    // Validar token
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            logger.error("Token invÃ¡lido: {}", e.getMessage());
            return false;
        }
    }

    // Validar token (versÃ£o simples)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            logger.error("Assinatura JWT invÃ¡lida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT nÃ£o suportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Claims JWT vazio: {}", e.getMessage());
        }
        return false;
    }

    // Obter tempo de expiraÃ§Ã£o em segundos
    public long getExpirationTime() {
        return jwtExpirationMs / 1000;
    }

    // Gerar token com claims customizados
    public String generateTokenWithClaims(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Extrair claim especÃ­fico por nome
    public Object extractClaim(String token, String claimName) {
        return extractAllClaims(token).get(claimName);
    }
} 