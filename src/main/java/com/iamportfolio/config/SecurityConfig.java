package com.iamportfolio.config;

import com.iamportfolio.auth.security.CustomUserDetailsService;
import com.iamportfolio.auth.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // DelegatingPasswordEncoder understands prefixes like {bcrypt}, {noop},
        // {pbkdf2} — required so Spring Authorization Server can verify
        // client_secrets stored with the {noop}/{bcrypt} format. New user
        // passwords are still hashed with bcrypt by default.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Stateful for OAuth2 authorization_code flows; the JWT filter still
        // works because it short-circuits on a valid Bearer header.
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints pÃºblicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/index.html").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()
                // Custom OAuth2 consent screen — must be reachable by the
                // authenticated user after the AS redirects them here.
                .requestMatchers("/consent").authenticated()
                // SAML IdP: metadata is public; SSO requires authentication
                .requestMatchers("/saml2/idp/metadata").permitAll()
                .requestMatchers("/saml2/idp/sso").authenticated()
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                // Audit log: only auditors or admins
                .requestMatchers("/api/audit/**").hasAnyAuthority("audit:read", "ROLE_ADMIN", "ROLE_AUDITOR")
                // Identity admin: read for identity:read, write for identity:write
                .requestMatchers("GET", "/api/identity/**").hasAnyAuthority("identity:read", "identity:write", "ROLE_ADMIN", "ROLE_IDENTITY_MANAGER")
                .requestMatchers("/api/identity/**").hasAnyAuthority("identity:write", "ROLE_ADMIN", "ROLE_IDENTITY_MANAGER")
                // OAuth2 client management
                .requestMatchers("/api/oauth2/clients/**").hasAnyAuthority("oauth2:client:manage", "ROLE_ADMIN")
                // SAML SP registry management
                .requestMatchers("/api/saml/service-providers/**").hasAnyAuthority("saml:sp:manage", "ROLE_ADMIN")
                // SCIM 2.0 — discovery is anonymous (RFC 7644 §4); resource ops require scim:provision scope
                .requestMatchers("/scim/v2/ServiceProviderConfig", "/scim/v2/Schemas", "/scim/v2/ResourceTypes").permitAll()
                .requestMatchers("/scim/v2/**").hasAnyAuthority("SCOPE_scim:provision", "scim:provision", "ROLE_ADMIN")
                // AI assistant: any authenticated user
                .requestMatchers("/api/ai/**").authenticated()
                .requestMatchers("/assistant.html").permitAll()
                // Governance (SoD rules + violations + access reviews)
                .requestMatchers("/api/governance/**").hasAnyAuthority("governance:manage", "ROLE_ADMIN")
                // Actuator probes are public; metrics + sensitive details require admin
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            // Spring Authorization Server-issued JWTs (RS256, JWKS) for /scim/v2/** etc.
            .oauth2ResourceServer(rs -> rs.jwt(org.springframework.security.config.Customizer.withDefaults()))
            // Legacy JJWT bearer for /api/auth-issued sessions; runs first so it
            // can short-circuit on its own header before the resource-server filter.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir origens especÃ­ficas (desenvolvimento)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // MÃ©todos permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciais
        configuration.setAllowCredentials(true);
        
        // Configurar para todos os caminhos
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 