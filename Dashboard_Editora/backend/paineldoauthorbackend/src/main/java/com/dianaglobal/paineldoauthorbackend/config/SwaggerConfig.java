package com.dianaglobal.paineldoauthorbackend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Painel do Autor Backend – API")
                        .version("1.0.0")
                        .description("API completa para gestão de autores, métricas, pagamentos e emails. Endpoints públicos de autenticação e recuperação de senha, com gerenciamento de Roles, Admin e Usuários.")
                )
                .servers(List.of(
                         // new Server().url("https://dianaglobalpaineldoauthor-52599bd07634.herokuapp.com").description("Production"),
                        new Server().url("http://localhost:8080").description("Local Dev")
                ));
    }
}
