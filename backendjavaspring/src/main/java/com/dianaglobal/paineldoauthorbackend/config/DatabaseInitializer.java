package com.dianaglobal.paineldoauthorbackend.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Order(-100)
public class DatabaseInitializer {

    private final Environment environment;

    public DatabaseInitializer(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource(DataSourceProperties properties) {
        // Cria o banco antes de criar o DataSource
        createDatabaseIfNotExists();
        
        // Retorna o DataSource padrão do Spring Boot
        return properties.initializeDataSourceBuilder().build();
    }

    private void createDatabaseIfNotExists() {
        try {
            // Detectar se está rodando no Heroku
            // Heroku já cria o banco automaticamente, não precisamos verificar
            boolean isHeroku = System.getenv("DYNO") != null || 
                             System.getenv("DATABASE_URL") != null ||
                             (environment.getProperty("spring.datasource.url") != null && 
                              environment.getProperty("spring.datasource.url").contains("amazonaws.com"));
            
            if (isHeroku) {
                log.info("🚀 Detectado Heroku/Cloud - banco já existe, pulando verificação");
                return;
            }
            
            // Resolve as variáveis de ambiente diretamente usando o Environment
            // O Spring Boot resolve automaticamente ${VAR_NAME} do YAML
            String datasourceUrl = environment.getProperty("spring.datasource.url", 
                "jdbc:postgresql://localhost:5432/paineldoauthorbackend");
            String datasourceUsername = environment.getProperty("spring.datasource.username");
            String datasourcePassword = environment.getProperty("spring.datasource.password");
            
            // Se ainda não foi resolvido, tenta ler diretamente das variáveis de ambiente
            if (datasourceUsername == null || datasourceUsername.isEmpty() || datasourceUsername.startsWith("${")) {
                datasourceUsername = System.getenv("DATABASE_USERNAME");
                if (datasourceUsername == null || datasourceUsername.isEmpty()) {
                    datasourceUsername = "postgres"; // Fallback
                }
            }
            
            if (datasourcePassword == null || datasourcePassword.isEmpty() || datasourcePassword.startsWith("${")) {
                datasourcePassword = System.getenv("DATABASE_PASSWORD");
                if (datasourcePassword == null) {
                    datasourcePassword = ""; // Fallback
                }
            }

            String dbName = extractDatabaseName(datasourceUrl);
            if (dbName.isEmpty()) {
                log.warn("Não foi possível extrair o nome do banco de dados da URL: {}", datasourceUrl);
                return;
            }

            // Conecta ao banco 'postgres' (banco padrão) para criar o banco se necessário
            String postgresUrl = datasourceUrl.replace("/" + dbName, "/postgres");
            
            log.info("🔍 Verificando se o banco de dados '{}' existe...", dbName);
            
            try (Connection conn = DriverManager.getConnection(postgresUrl, datasourceUsername, datasourcePassword);
                 Statement stmt = conn.createStatement()) {
                
                // Verifica se o banco existe
                String checkDb = "SELECT 1 FROM pg_database WHERE datname = '" + dbName.replace("'", "''") + "'";
                boolean databaseExists;
                try (ResultSet rs = stmt.executeQuery(checkDb)) {
                    databaseExists = rs.next();
                }
                
                if (!databaseExists) {
                    log.info("📦 Banco de dados '{}' não existe. Criando...", dbName);
                    // Cria o banco de dados
                    String createDb = "CREATE DATABASE \"" + dbName + "\"";
                    stmt.executeUpdate(createDb);
                    log.info("✅ Banco de dados '{}' criado com sucesso!", dbName);
                } else {
                    log.info("✅ Banco de dados '{}' já existe.", dbName);
                }
            }
        } catch (java.sql.SQLException e) {
            log.warn("⚠️  Erro ao verificar/criar banco de dados: {}", e.getMessage());
            // Não lança exceção para não bloquear a aplicação se o banco já existir
        }
    }

    private String extractDatabaseName(String url) {
        // Formato: jdbc:postgresql://localhost:5432/nome_do_banco
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        }

        String dbPart = url.substring(lastSlash + 1);
        int questionMark = dbPart.indexOf('?');
        if (questionMark != -1) {
            dbPart = dbPart.substring(0, questionMark);
        }

        return dbPart.trim();
    }
}

