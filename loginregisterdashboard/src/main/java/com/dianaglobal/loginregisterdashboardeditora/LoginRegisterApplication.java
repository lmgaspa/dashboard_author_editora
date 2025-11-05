package com.dianaglobal.loginregisterdashboardeditora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.dianaglobal.loginregisterdashboardeditora")
@EnableJpaRepositories(basePackages = "com.dianaglobal.loginregisterdashboardeditora")
public class LoginRegisterApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoginRegisterApplication.class, args);
    }
}
