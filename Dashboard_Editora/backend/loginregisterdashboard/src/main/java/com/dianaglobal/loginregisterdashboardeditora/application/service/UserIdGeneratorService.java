package com.dianaglobal.loginregisterdashboardeditora.application.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdGeneratorService {

    private final JdbcTemplate jdbcTemplate;
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^user-(\\d+)$");

    /**
     * Gera o próximo ID de usuário no formato "user-X"
     * Busca o maior número existente e incrementa 1
     */
    public String generateNextUserId() {
        try {
            // Busca todos os IDs que começam com "user-"
            var userIds = jdbcTemplate.queryForList(
                "SELECT id FROM users WHERE id LIKE 'user-%' ORDER BY id DESC",
                String.class
            );

            int maxNumber = 0;
            
            // Encontra o maior número
            for (String userId : userIds) {
                Matcher matcher = USER_ID_PATTERN.matcher(userId);
                if (matcher.matches()) {
                    int number = Integer.parseInt(matcher.group(1));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                }
            }

            // Próximo número
            int nextNumber = maxNumber + 1;
            String newId = "user-" + nextNumber;
            
            log.info("Generated new user ID: {}", newId);
            return newId;
            
        } catch (DataAccessException | NumberFormatException e) {
            log.error("Error generating user ID, defaulting to user-1", e);
            // Se der erro, começa do 1
            return "user-1";
        }
    }

    /**
     * Gera ID para admin (sempre "admin-1")
     */
    public String generateAdminId() {
        return "admin-1";
    }
}

