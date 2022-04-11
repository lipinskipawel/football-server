package com.github.lipinskipawel.extension;

import com.github.lipinskipawel.api.QueryRegister;
import com.github.lipinskipawel.server.FootballServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class FootballServerExtension implements BeforeAllCallback, AfterAllCallback {

    FootballServerExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getElement()
                .ifPresent(element -> {
                            final var annotation = AnnotationSupport.findAnnotation(element, Application.class);
                            if (annotation.isPresent()) {
                                final var application = annotation.get();
                                final var port = application.port();
                                final var register = createRegister(application);
                                final var pool = Executors.newSingleThreadExecutor();
                                final var server = new FootballServer(new InetSocketAddress("localhost", port), register);
                                pool.submit(server);
                                getStore(context).put("server", server);
                                getStore(context).put("pool", pool);
                                getStore(context).put("register", register);
                            }
                        }
                );
    }

    private QueryRegister createRegister(final Application application) {
        try {
            return (QueryRegister) application.authModuleQueryRegister().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Could not create QueryRegister via reflection. " + this.getClass() + " has failed.", e.getCause());
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        final var server = getApplicationId(context);
        if (server != null) {
            server.stop(1000);
            final var applicationPool = getApplicationPool(context);
            if (applicationPool != null) {
                applicationPool.shutdown();
            }
        }
    }

    private static FootballServer getApplicationId(final ExtensionContext context) {
        return getStore(context).get("server", FootballServer.class);
    }

    private static ExecutorService getApplicationPool(final ExtensionContext context) {
        return getStore(context).get("poll", ExecutorService.class);
    }

    private static ExtensionContext.Store getStore(final ExtensionContext context) {
        final var namespace = ExtensionContext.Namespace.create(FootballServerExtension.class);
        return context.getStore(namespace);
    }
}
