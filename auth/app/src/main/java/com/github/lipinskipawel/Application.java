package com.github.lipinskipawel;

import com.github.lipinskipawel.register.AuthRegister;
import com.github.lipinskipawel.register.RegisterEntrypoint;
import com.github.lipinskipawel.routes.LoggingResource;
import com.github.lipinskipawel.routes.RegisterResource;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import static io.javalin.Javalin.create;

public final class Application {
    private static final Javalin app = createApp();

    public static void main(String[] args) {
        app.start(8090);
    }

    private static Javalin createApp() {
        var app = create(conf -> {
            conf.http.defaultContentType = "application/json";
            conf.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
        })
                .routes(new LoggingResource())
                .routes(new RegisterResource(new AuthRegister(RegisterEntrypoint.register)));
        Runtime.getRuntime().addShutdownHook(new Thread(Application::closeHttpServer));
        return app;
    }

    private static void closeHttpServer() {
        app.close();
    }
}
