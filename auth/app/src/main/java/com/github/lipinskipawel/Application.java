package com.github.lipinskipawel;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import static com.github.lipinskipawel.HttpApplicationServer.httpServer;

public final class Application {

    public static void main(String[] args) {
        final var dataSource = dataSource();
        final var database = new Flyway(dataSource);
        database.migrate();
        final var dependencies = new Dependencies(dataSource);
        final var app = httpServer(dependencies);
        app.start(8090);
    }

    private static DataSource dataSource() {
        final var config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://localhost:5432/auth");
        config.setUsername("rw_user");
        config.setPassword("password");
        config.addDataSourceProperty("minimumIdle", "5");
        config.addDataSourceProperty("maximumPoolSize", "25");

        return new HikariDataSource(config);
    }
}
