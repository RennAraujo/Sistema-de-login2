package com.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class SistemaLoginSeguroApplication {

    private static final Logger logger = LoggerFactory.getLogger(SistemaLoginSeguroApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SistemaLoginSeguroApplication.class);
        Environment env = app.run(args).getEnvironment();
        
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "";
        }
        
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("Não foi possível determinar o endereço IP do host", e);
        }

        logger.info("""
            
            ----------------------------------------------------------
            	Sistema de Login Seguro iniciado com sucesso!
            	Aplicação:  {}
            	Perfil(s):  {}
            	Local:      {}://localhost:{}{}
            	Externo:    {}://{}:{}{}
            	H2 Console: {}://localhost:{}{}/h2-console
            	Endpoints de teste: {}://localhost:{}{}/api/test/instructions
            ----------------------------------------------------------
            """,
            env.getProperty("spring.application.name"),
            env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles(),
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            protocol,
            serverPort,
            contextPath,
            protocol,
            serverPort,
            contextPath
        );
    }
} 