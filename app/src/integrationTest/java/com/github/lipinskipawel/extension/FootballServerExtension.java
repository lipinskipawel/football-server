package com.github.lipinskipawel.extension;

import com.github.lipinskipawel.server.FootballServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

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
                                final var port = annotation.get().port();
                                final var pool = Executors.newSingleThreadExecutor();
                                final var server = new FootballServer(new InetSocketAddress("localhost", port));
                                pool.submit(server);
                                getStore(context).put("server", server);
                                getStore(context).put("pool", pool);
                            }
                        }
                );
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
        final var namespace = ExtensionContext.Namespace.create(FootballServerExtension.class, context.getUniqueId());
        return context.getStore(namespace);
    }
}
