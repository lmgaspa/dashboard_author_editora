package com.dianaglobal.paineldoauthorbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.dianaglobal.paineldoauthorbackend")
@EnableJpaRepositories(basePackages = "com.dianaglobal.paineldoauthorbackend")
@EnableScheduling
public class PainelDoAuthorApplication {
    public static void main(String[] args) {
        SpringApplication.run(PainelDoAuthorApplication.class, args);
    }
}
