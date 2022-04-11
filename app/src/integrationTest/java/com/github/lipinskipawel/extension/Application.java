package com.github.lipinskipawel.extension;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is intended to be used on every integration test.
 * When test class is marked with {@link Application} annotation then the
 * {@link com.github.lipinskipawel.server.FootballServer} will be started and available for test. Along with starting
 * the web socket server there is a need to provide the auth module dependency. This dependency is configured by default
 * but the clients can provide its own implementation by setting {@link #authModuleQueryRegister()} parameter.
 */
@ExtendWith(FootballServerExtension.class)
@ExtendWith(AuthModuleResolver.class)
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Application {

    /**
     * This method is required to configure the port for {@link com.github.lipinskipawel.server.FootballServer}.
     *
     * @return the port of the {@link com.github.lipinskipawel.server.FootballServer}
     */
    int port();

    /**
     * The class will be created via reflection and used as a mock for auth module.
     *
     * @return class to be used in integration tests
     */
    Class<?> authModuleQueryRegister() default TestRegister.class;
}
