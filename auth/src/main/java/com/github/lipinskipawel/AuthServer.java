package com.github.lipinskipawel;

import com.github.lipinskipawel.register.AuthRegister;
import com.github.lipinskipawel.register.RegisterEntrypoint;
import com.github.lipinskipawel.register.RegisterResource;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import java.net.InetSocketAddress;

import static io.javalin.Javalin.create;

/**
 * Simple wrapper class that can start the Netty HTTP server.
 */
public final class AuthServer {
    private final Javalin app = createApp();

    private Javalin createApp() {
        var app = create(conf -> {
            conf.http.defaultContentType = "application/json";
            conf.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
        })
                .routes(new RegisterResource(new AuthRegister(RegisterEntrypoint.register)));
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeHttpServer));
        return app;
    }

    public void startServer(final InetSocketAddress address) {
        app.start(address.getPort());
    }

    public void closeHttpServer() {
        app.close();
    }
}
