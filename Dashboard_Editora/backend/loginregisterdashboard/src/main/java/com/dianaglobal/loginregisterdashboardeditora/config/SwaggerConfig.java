package com.dianaglobal.loginregisterdashboardeditora.config;

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
                        .title("Dashboard Author Editora – API")
                        .version("1.0.0")
                        .description("Public authentication and password recovery endpoints, with Role, Admin and User management.")
                )
                .servers(List.of(
                         // new Server().url("https://dianagloballoginregister-52599bd07634.herokuapp.com").description("Production"),
                        new Server().url("http://localhost:8080").description("Local Dev")
                ));
    }
}
