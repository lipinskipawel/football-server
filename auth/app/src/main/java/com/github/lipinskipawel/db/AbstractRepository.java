package com.github.lipinskipawel.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.jooq.SQLDialect.POSTGRES;

abstract class AbstractRepository {
    final DSLContext db;

    AbstractRepository() {
        final var connection = getConnection();
        db = DSL.using(connection, POSTGRES);
    }

    private Connection getConnection() {
        try {
            return dataSource().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSource dataSource() {
        final var config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://localhost:5432/auth");
        config.setUsername("rw_user");
        config.setPassword("password");
        config.addDataSourceProperty("minimumIdle", "5");
        config.addDataSourceProperty("maximumPoolSize", "25");

        return new HikariDataSource(config);
    }
}
