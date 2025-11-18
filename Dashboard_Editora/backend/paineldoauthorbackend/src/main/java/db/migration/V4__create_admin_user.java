package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class V4__create_admin_user extends BaseJavaMigration {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    @Override
    public void migrate(Context context) throws Exception {
        // Lê variáveis de ambiente - obrigatórias para segurança
        String adminUsername = getRequiredEnv("ADMIN_USERNAME");
        String adminEmail = getRequiredEnv("ADMIN_EMAIL");
        String adminPassword = getRequiredEnv("ADMIN_PASSWORD");
        String adminId = getOptionalEnv("ADMIN_ID", "admin-1");
        
        // Validações
        validateUsername(adminUsername);
        validateEmail(adminEmail);
        validatePassword(adminPassword);
        validateAdminId(adminId);
        
        // Normaliza email (lowercase)
        String normalizedEmail = adminEmail.trim().toLowerCase();
        
        // Gera hash da senha
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hashedPassword = encoder.encode(adminPassword);
        
        Connection connection = context.getConnection();
        
        // Verifica se o tipo da coluna id é UUID ou VARCHAR
        // Se for UUID, usa UUID.randomUUID(). Se for VARCHAR, usa adminId diretamente
        String checkColumnTypeSql = """
            SELECT data_type 
            FROM information_schema.columns 
            WHERE table_name = 'users' AND column_name = 'id'
            """;
        
        String idType = "UUID"; // default
        try (var checkStmt = connection.createStatement();
             var rs = checkStmt.executeQuery(checkColumnTypeSql)) {
            if (rs.next()) {
                idType = rs.getString("data_type");
            }
        }
        
        String sql = """
            INSERT INTO users (id, name, email, password, email_confirmed, auth_provider, role)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (email) DO NOTHING
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Se o tipo é UUID, gera um UUID temporário (V6 vai converter depois)
            // Se o tipo é VARCHAR, usa o adminId diretamente
            if ("uuid".equalsIgnoreCase(idType)) {
                stmt.setObject(1, UUID.randomUUID());
            } else {
                stmt.setString(1, adminId);
            }
            stmt.setString(2, adminUsername.trim());
            stmt.setString(3, normalizedEmail);
            stmt.setString(4, hashedPassword);
            stmt.setBoolean(5, true);
            stmt.setString(6, "LOCAL");
            stmt.setString(7, "ADMIN");
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                // Usuário já existe, não é erro
                System.out.println("[V4] Admin user already exists, skipping creation");
            } else {
                System.out.println("[V4] Admin user created successfully with ID: " + adminId);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create admin user: " + e.getMessage(), e);
        }
    }
    
    private String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                String.format("Environment variable %s is required but not set", key)
            );
        }
        return value;
    }
    
    private String getOptionalEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
    
    private void validateUsername(String username) {
        if (username == null || username.trim().isBlank()) {
            throw new IllegalArgumentException("Admin username cannot be empty");
        }
        if (username.trim().length() < 3) {
            throw new IllegalArgumentException("Admin username must be at least 3 characters");
        }
        if (username.trim().length() > 100) {
            throw new IllegalArgumentException("Admin username must be at most 100 characters");
        }
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Admin email cannot be empty");
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid admin email format: " + email);
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Admin password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Admin password must be at least 8 characters");
        }
    }
    
    private void validateAdminId(String adminId) {
        if (adminId == null || adminId.trim().isBlank()) {
            throw new IllegalArgumentException("Admin ID cannot be empty");
        }
        String trimmed = adminId.trim();
        if (!trimmed.startsWith("admin-")) {
            throw new IllegalArgumentException("Admin ID must start with 'admin-': " + adminId);
        }
        if (trimmed.length() > 20) {
            throw new IllegalArgumentException("Admin ID must be at most 20 characters: " + adminId);
        }
    }
}

