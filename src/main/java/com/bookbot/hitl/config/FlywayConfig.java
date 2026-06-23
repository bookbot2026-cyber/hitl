package com.bookbot.hitl.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;

@Configuration
public class FlywayConfig {

    private final DataSource dataSource;

    public FlywayConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migrate() {
        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
