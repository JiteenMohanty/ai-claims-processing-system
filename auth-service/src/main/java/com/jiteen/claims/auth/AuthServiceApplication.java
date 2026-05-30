package com.jiteen.claims.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Auth Service application.
 *
 * <p>Bootstraps the Spring Application Context and starts the embedded web server.</p>
 *
 * @author Jiteen Mohanty
 * @since 1.0
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}