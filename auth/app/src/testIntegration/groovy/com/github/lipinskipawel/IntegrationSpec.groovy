package com.github.lipinskipawel

import com.github.lipinskipawel.client.HttpAuthClient
import com.github.lipinskipawel.client.HttpConfig
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

import static com.github.lipinskipawel.HttpApplicationServer.httpServer
import static com.github.lipinskipawel.client.HttpAuthClient.httpAuthClient
import static java.net.URI.create
import static java.time.Duration.ofSeconds

abstract class IntegrationSpec extends Specification {

    private static final int PORT = 8099
    private static final HttpApplicationServer httpServer
    private static final Dependencies dependencies

    public static final HttpAuthClient authClient

    static {
        def dataSource = dataSource(dbInstance())
        def flyway = new Flyway(dataSource)
        flyway.migrate()
        dependencies = new Dependencies(dataSource)
        httpServer = httpServer(dependencies)
        httpServer.start(PORT)

        authClient = httpAuthClient(httpConfig())
    }

    static void truncateUsers() {
        dependencies.authRegister.clearAll()
        dependencies.userRepository.truncate()
    }

    private static def httpConfig() {
        return new HttpConfig(
            ofSeconds(1),
            new HttpConfig.HttpRequestConfig(create("http://localhost:%d".formatted(PORT)), ofSeconds(1)))
    }

    private static def dataSource(JdbcDatabaseContainer<?> dbInstance) {
        def config = new PGSimpleDataSource()

        config.setUrl(dbInstance.getJdbcUrl())
        config.setUser(dbInstance.getUsername())
        config.setPassword(dbInstance.getPassword())

        return config
    }

    private static def dbInstance() {
        def postgresSQLContainer = new PostgreSQLContainer<>("postgres:16.4")
            .withDatabaseName("auth")
            .withUsername("postgres")
            .withPassword("password")
        postgresSQLContainer.start()
        return postgresSQLContainer
    }
}
