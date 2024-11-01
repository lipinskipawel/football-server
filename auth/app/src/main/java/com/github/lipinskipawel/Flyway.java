package com.github.lipinskipawel;

import javax.sql.DataSource;

final class Flyway {
    private final DataSource dataSource;

    Flyway(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrate() {
        final var flyway = org.flywaydb.core.Flyway.configure()
            .dataSource(dataSource)
            .schemas("public")
            .load();
        flyway.migrate();
    }
}
