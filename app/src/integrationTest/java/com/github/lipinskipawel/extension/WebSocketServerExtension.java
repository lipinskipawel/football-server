package com.github.lipinskipawel.extension;

import com.github.lipinskipawel.server.WebSocketServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class WebSocketServerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
    private AuthModuleFacade register;

    WebSocketServerExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getElement()
                .ifPresent(element -> {
                            final var annotation = AnnotationSupport.findAnnotation(element, Application.class);
                            if (annotation.isPresent()) {
                                final var application = annotation.get();
                                final var port = application.port();
                                final var pool = Executors.newSingleThreadExecutor();
                                register = createRegister(application);
                                final var server = new WebSocketServer();
                                pool.submit(() -> server.start(
                                        new InetSocketAddress("localhost", port),
                                        register
                                ));
                                waitOneSecond();
                                getStore(context).put("pool", pool);
                                getStore(context).put("server", server);
                                getStore(context).put("register", register);
                            }
                        }
                );
    }

    private AuthModuleFacade createRegister(final Application application) {
        try {
            return (AuthModuleFacade) application.authModuleQueryRegister().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Could not create QueryRegister via reflection. " + this.getClass() + " has failed.", e.getCause());
        }
    }

    /**
     * This is workaround. Not sure why it has to be. Probably bad netty understanding.
     */
    private void waitOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        getApplicationId(context).stop();
        getPoolId(context).shutdown();
    }

    /**
     * Executes after beforeAll so the register is already created.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        register.clearAll();
    }

    private static WebSocketServer getApplicationId(final ExtensionContext context) {
        return getStore(context).get("server", WebSocketServer.class);
    }

    private static ExecutorService getPoolId(final ExtensionContext context) {
        return getStore(context).get("pool", ExecutorService.class);
    }

    private static ExtensionContext.Store getStore(final ExtensionContext context) {
        final var namespace = ExtensionContext.Namespace.create(WebSocketServerExtension.class);
        return context.getStore(namespace);
    }
}
