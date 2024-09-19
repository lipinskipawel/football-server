package com.github.lipinskipawel;

import com.github.lipinskipawel.client.HttpAuthClient;
import com.github.lipinskipawel.client.HttpConfig;
import com.github.lipinskipawel.client.HttpConfig.HttpRequestConfig;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

import static com.github.lipinskipawel.HttpApplicationServer.httpServer;
import static com.github.lipinskipawel.client.HttpAuthClient.httpAuthClient;
import static java.net.URI.create;
import static java.time.Duration.ofSeconds;

public abstract class IntegrationSpec {

    private static final int PORT = 8099;
    private static final HttpApplicationServer httpServer;
    private static final Dependencies dependencies;

    public static final HttpAuthClient authClient;

    static {
        final var dataSource = dataSource(dbInstance());
        final var flyway = new Flyway(dataSource);
        flyway.migrate();
        dependencies = new Dependencies(dataSource);
        httpServer = httpServer(dependencies);
        httpServer.start(PORT);

        authClient = httpAuthClient(httpConfig());
    }

    private static HttpConfig httpConfig() {
        return new HttpConfig(
            ofSeconds(1),
            new HttpRequestConfig(create("http://localhost:%d".formatted(PORT)), ofSeconds(1)));
    }

    public static void truncateUsers() {
        dependencies.authRegister.clearAll();
        dependencies.userRepository.truncate();
    }

    private static DataSource dataSource(JdbcDatabaseContainer<?> dbInstance) {
        final var config = new PGSimpleDataSource();

        config.setUrl(dbInstance.getJdbcUrl());
        config.setUser(dbInstance.getUsername());
        config.setPassword(dbInstance.getPassword());

        return config;
    }

    private static JdbcDatabaseContainer<?> dbInstance() {
        final var postgresSQLContainer = new PostgreSQLContainer<>("postgres:16.4")
            .withDatabaseName("auth")
            .withUsername("postgres")
            .withPassword("password");
        postgresSQLContainer.start();
        return postgresSQLContainer;
    }
}
