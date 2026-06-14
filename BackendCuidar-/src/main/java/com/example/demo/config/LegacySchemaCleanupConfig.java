package com.example.demo.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LegacySchemaCleanupConfig {

    private final JdbcTemplate jdbcTemplate;

    public LegacySchemaCleanupConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void removerEstruturaLegada() {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS cuidador DROP COLUMN IF EXISTS instituicao_id");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS idoso DROP COLUMN IF EXISTS instituicao_id");
        jdbcTemplate.execute("DROP TABLE IF EXISTS instituicao CASCADE");
    }
}
