package com.iamportfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class IamPortfolioApplication {

    private static final Logger logger = LoggerFactory.getLogger(IamPortfolioApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(IamPortfolioApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = env.getProperty("server.ssl.key-store") != null ? "https" : "http";
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("Could not determine host address", e);
        }

        logger.info("""

            ----------------------------------------------------------
                IAM Portfolio started
                Application: {}
                Profile(s):  {}
                Local:       {}://localhost:{}{}
                External:    {}://{}:{}{}
                Swagger UI:  {}://localhost:{}{}/swagger-ui.html
            ----------------------------------------------------------
            """,
            env.getProperty("spring.application.name"),
            env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles(),
            protocol, serverPort, contextPath,
            protocol, hostAddress, serverPort, contextPath,
            protocol, serverPort, contextPath
        );
    }
}
